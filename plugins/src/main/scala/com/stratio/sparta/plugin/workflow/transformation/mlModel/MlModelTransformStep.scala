/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.mlModel

import java.io.{Serializable => JSerializable}
import scala.util.{Failure, Success, Try}

import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.linalg.UdtConversions
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import com.stratio.intelligence.mlmodelrepository.client.MlModelsRepositoryClient
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.constants.SdkConstants
import com.stratio.sparta.core.helpers.{SSLHelper, SdkSchemaHelper}
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import com.stratio.sparta.plugin.enumerations.MlModelSelectionType
import com.stratio.sparta.plugin.helper.SchemaHelper.{getSchemaFromSessionOrModelOrRdd, parserInputSchema}
import com.stratio.sparta.plugin.helper.SparkStepHelper

abstract class MlModelTransformStep[Underlying[Row]](
                                                      name: String,
                                                      outputOptions: OutputOptions,
                                                      transformationStepsManagement: TransformationStepManagement,
                                                      ssc: Option[StreamingContext],
                                                      xDSession: XDSession,
                                                      properties: Map[String, JSerializable]
                                                    )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val ErrorModelMessage = s"It's mandatory to specify the model"
  lazy val ErrorUrlMessage = "It's mandatory to specify the model repository URL"
  lazy val ErrorPathMessage = "It's mandatory to specify the model repository path"
  lazy val ErrorClientMessage = s"Error creating the repository client"
  lazy val ErrorExistsMessage = s"The model not exists in the repository"

  lazy val modelSelection = MlModelSelectionType.withName(properties.getString("selectMlType", "ML_MODEL_URL").toUpperCase)
  lazy val modelName = properties.getString("model", None).notBlank
  lazy val modelRepositoryUrl = properties.getString(SdkConstants.ModelRepositoryUrl, None).notBlank
  lazy val modelRepositoryPath = properties.getString("path", None).notBlank

  def modelRepositoryClient: Try[MlModelsRepositoryClient] = Try {
    MlModelTransformStep.getMlRepositoryClient(
      xDSession,
      modelRepositoryUrl.getOrElse(throw new Exception(ErrorUrlMessage))
    )
  }

  //scalastyle:off
  def requirements(): Unit = {
    if (modelSelection.equals(MlModelSelectionType.ML_MODEL_URL)) {
      require(modelName.nonEmpty, ErrorModelMessage)
      require(modelRepositoryUrl.nonEmpty, ErrorUrlMessage)
      modelRepositoryClient match {
        case Success(client) =>
          require(client.checkIfModelExists(modelName.get).toOption.forall(modelExists => modelExists), ErrorExistsMessage)
        case Failure(e) =>
          throw new IllegalArgumentException(s"$ErrorClientMessage.${e.getLocalizedMessage}")
      }
    }
    else
      require(modelRepositoryPath.nonEmpty, ErrorUrlMessage)
  }

  override def cleanUp(options: Map[String, String]): Unit =
    modelRepositoryUrl.foreach(url => MlModelTransformStep.removeMlRepositoryClient(url))

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name))

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ WorkflowValidationMessage(s"The input schema from step" +
              s" ${input.stepName} is not valid.", name))
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"The input table name ${is.stepName}" +
            s" is not valid.", name))
      }
    }

    if (modelSelection.equals(MlModelSelectionType.ML_MODEL_URL) && modelName.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(ErrorModelMessage, name)
      )

    if (modelSelection.equals(MlModelSelectionType.ML_MODEL_URL) && modelRepositoryUrl.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(ErrorUrlMessage, name)
      )

    if (modelSelection.equals(MlModelSelectionType.ML_MODEL_PATH) && modelRepositoryPath.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(ErrorUrlMessage, name)
      )

    if (modelSelection.equals(MlModelSelectionType.ML_MODEL_URL))
      modelRepositoryClient match {
        case Success(client) =>
          if (client.checkIfModelExists(modelName.get).toOption.exists(modelExists => !modelExists))
            validation = ErrorValidations(
              valid = false,
              messages = validation.messages :+ WorkflowValidationMessage(ErrorExistsMessage, name)
            )
        case Failure(e) =>
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ WorkflowValidationMessage(s"$ErrorClientMessage.${e.getLocalizedMessage}", name)
          )
          throw new RuntimeException(s"$ErrorClientMessage.${e.getLocalizedMessage}")
      }

    validation
  }

  //scalastyle:off
  def executeMlModel(stepName: String, stepData: RDD[Row]): (RDD[Row], Option[StructType], Option[StructType]) = {
    var resultSchema: Option[StructType] = None
    var inputSchema: Option[StructType] = None

    Try {
      var executeModel = true
      inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, stepName, inputsModel, stepData)
      val inputDataFrame = inputSchema match {
        case Some(schema) =>
          val df = xDSession.createDataFrame(stepData, schema)
          df.createOrReplaceTempView(stepName)
          df
        case None =>
          executeModel = false
          val df = xDSession.createDataFrame(stepData, StructType(Nil))
          df.createOrReplaceTempView(stepName)
          df
      }

      if (executeModel) {
        val sparkModel = if (modelSelection.equals(MlModelSelectionType.ML_MODEL_URL))
          modelRepositoryClient.get.getSparkPipelineModel(modelName.get) match {
            case Success(pipelineModel) =>
              pipelineModel
            case Failure(e) =>
              throw new RuntimeException("Error obtaining pipeline model from intelligence repository", e)
          }
        else
          PipelineModel.load(modelRepositoryPath.get)

        val df = sparkModel.transform(inputDataFrame)
        val newSchema = UdtConversions.newSchema(df.schema)
        val newRdd = df.rdd.map { row =>
          UdtConversions.convertRow(row, newSchema)
        }
        val newDf = xDSession.createDataFrame(newRdd, newSchema)

        newDf.createOrReplaceTempView(name)
        resultSchema = Option(newSchema)
        newDf.rdd
      } else {
        resultSchema = Option(StructType(Nil))
        stepData.filter(_ => false)
      }
    } match {
      case Success(sqlResult) =>
        (sqlResult, resultSchema, inputSchema)
      case Failure(e) =>
        (SparkStepHelper.failRDDWithException(stepData, e), resultSchema, inputSchema)
    }
  }
}

object MlModelTransformStep {

  private val mlModelsRepositoryClients = scala.collection.mutable.Map[String, MlModelsRepositoryClient]()

  def getMlRepositoryClient(xDSession: XDSession, url: String): MlModelsRepositoryClient = {
    mlModelsRepositoryClients.getOrElseUpdate(url, {
      if (url.contains("https"))
        new MlModelsRepositoryClient(xDSession, url, SSLHelper.getSSLContextV2(withHttps = true))
      else new MlModelsRepositoryClient(xDSession, url)
    })
  }

  def removeMlRepositoryClient(url: String): Unit = mlModelsRepositoryClients.remove(url)
}