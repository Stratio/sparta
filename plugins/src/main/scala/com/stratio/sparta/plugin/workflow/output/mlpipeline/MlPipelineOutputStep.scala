/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline

import java.io.{Serializable => JSerializable}

import com.stratio.intelligence.mlmodelrepository.client.MlModelsRepositoryClient
import com.stratio.intelligence.mlmodelrepository.client.dtos.requests.UploadModelRequestData
import com.stratio.intelligence.mlmodelrepository.client.dtos.responses.UploadModelResponse
import com.stratio.sparta.core.constants.SdkConstants
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.helpers.SSLHelper
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.enumerations.{MlPipelineSaveMode, MlPipelineSerializationLibs}
import com.stratio.sparta.plugin.workflow.output.mlpipeline.deserialization._
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.apache.spark.ml.param._
import org.apache.spark.ml.{Pipeline, PipelineModel, PipelineStage}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}


class MlPipelineOutputStep(
                            name: String,
                            xDSession: XDSession,
                            properties: Map[String, JSerializable]
                          ) extends OutputStep(name, xDSession, properties) {

  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer() + BooleanToString

  lazy val outputMode: Option[MlPipelineSaveMode.Value] =
    Try(MlPipelineSaveMode.withName(properties.getString("output.mode").toUpperCase)).toOption

  // · Hdfs
  lazy val pathToSave: Option[String] = properties.getString("path", None).notBlank

  // · Ml-model-repository
  lazy val validateConnectionMlModelrep: Boolean = properties.getBoolean("validateMlModelRep", default = false)
  lazy val externalMlModelRepositoryUrl: Option[String] = properties.getString(SdkConstants.ModelRepositoryUrl, None).notBlank
  lazy val externalMlModelRepositoryModelName: Option[String] = properties.getString("mlmodelrepModelName", None)
  lazy val externalMlModelRepositoryTmpDir: String = properties.getString("mlmodelrepModelTmpDir", "/tmp")
  lazy val serializationLib: MlPipelineSerializationLibs.Value =
    MlPipelineSerializationLibs.withName(properties.getString("serializationLib", "SPARK_AND_MLEAP").toUpperCase)

  // => Pipeline related
  // · Pipeline Json descriptor --> deserialized into Array[PipelineStageDescriptor]
  lazy val pipelineJson: Option[String] = properties.getString("pipeline", None)
  lazy val pipelineDescriptor: Try[Array[PipelineStageDescriptor]] = getPipelineDescriptor
  // · SparkMl Pipeline (built using Array[PipelineStageDescriptor])
  lazy val pipeline: Try[Pipeline] = getPipelineFromDescriptor(pipelineDescriptor.get)

  def mlModelRepClient: Try[MlModelsRepositoryClient] = Try {
    MlPipelineOutputStep.getMlRepositoryClient(
      xDSession,
      externalMlModelRepositoryUrl.getOrElse(throw new Exception(ValidationErrorMessages.errorUrlMessage))
    )
  }

  override def cleanUp(options: Map[String, String]): Unit =
    externalMlModelRepositoryUrl.foreach(url => MlPipelineOutputStep.removeMlRepositoryClient(url))

  /**
    * Validates the options and the pipeline construction process
    */
  //noinspection ScalaStyle
  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    def addValidationError(validation: ErrorValidations, error: String): ErrorValidations =
      ErrorValidations(valid = false, messages = validation.messages :+ WorkflowValidationMessage(error, name))

    var validation: ErrorValidations = ErrorValidations(valid = true, messages = Seq.empty)

    // => Save mode
    if (outputMode.isEmpty) {
      // - Non defined
      validation = addValidationError(validation, ValidationErrorMessages.invalidSaveMode)
    } else {
      // => Filesystem mode - path not defined
      if (((outputMode.get == MlPipelineSaveMode.FILESYSTEM) || (outputMode.get == MlPipelineSaveMode.BOTH)) && pathToSave.isEmpty)
        validation = addValidationError(validation, ValidationErrorMessages.nonDefinedPath)

      // => Ml-Model-Repository
      if ((outputMode.get == MlPipelineSaveMode.MODELREP) || (outputMode.get == MlPipelineSaveMode.BOTH)) {
        if (externalMlModelRepositoryUrl.isEmpty || externalMlModelRepositoryModelName.isEmpty) {
          validation = addValidationError(validation, ValidationErrorMessages.nonDefinedMlRepoConnection)
        } else {
          // · If validate Ml-Model-repository external connection during creation time is enabled
          validation = if (validateConnectionMlModelrep) {
            mlModelRepClient match {
              case Success(client) =>
                client.checkIfModelExists(externalMlModelRepositoryModelName.get) match {
                  case Success(true) => addValidationError(validation, ValidationErrorMessages.mlModelRepModelAlreadyExistError)
                  case Success(false) => validation
                  case Failure(e) => addValidationError(validation, s"${ValidationErrorMessages.mlModelRepConnectionError} ${e.getMessage}")
                }
              case Failure(e) => addValidationError(validation, s"${ValidationErrorMessages.mlModelRepConnectionError} ${e.getMessage}")
            }
          } else validation
        }
      }
    }

    // · Non provided pipeline Json descriptor
    if (pipelineJson.isEmpty)
      validation = addValidationError(validation, ValidationErrorMessages.emptyJsonPipelineDescriptor)

    // · Error de-serializing pipeline Json descriptor
    if (pipelineJson.isDefined && pipelineDescriptor.isFailure) {
      val error = pipelineDescriptor match {
        case Failure(f) => f
      }
      validation = addValidationError(validation,
        ValidationErrorMessages.invalidJsonFormatPipelineDescriptor + s" ${error.getMessage}")
    }

    // · Error building SparkML Pipeline instance
    if (pipelineDescriptor.isSuccess && pipeline.isFailure) {
      val errors: Seq[WorkflowValidationMessage] = pipeline match {
        case Failure(f) => f.getMessage.split("\\n").map(m => WorkflowValidationMessage(s"· $m", name)).toSeq
      }
      validation = ErrorValidations(
        valid = false,
        messages = (validation.messages :+ WorkflowValidationMessage(
          ValidationErrorMessages.errorBuildingPipelineInstance, name)) ++ errors
      )
    }

    validation
  }

  /**
    * Save function: constructs a Spark ML Pipeline object using input information
    *
    * @param dataFrame The dataFrame to save
    * @param saveMode  The sparta save mode selected
    * @param options   Options to save the data (partitionBy, primaryKey ... )
    */
  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    // · Getting built pipeline
    val builtPipeline = pipeline.getOrElse(throw new Exception(ValidationErrorMessages.errorBuildingPipelineInstance))

    // · Validating schemas
    var currentSchema = dataFrame.schema
    builtPipeline.getStages.foreach { stage =>
      Try {
        currentSchema = stage.transformSchema(currentSchema)
      } match {
        case Failure(e) =>
          throw new Exception(s"Schema error on ${stage.getClass.getSimpleName}@${stage.uid}: ${e.getMessage}")
        case _ => None
      }
    }

    // · Training pipeline
    val pipelineModel: PipelineModel = builtPipeline.fit(dataFrame)

    // · Save model to path
    if ((outputMode.get == MlPipelineSaveMode.FILESYSTEM) || (outputMode.get == MlPipelineSaveMode.BOTH))
      pathToSave.foreach { path =>
        if (saveMode == SaveModeEnum.Overwrite)
          pipelineModel.write.overwrite().save(path)
        else pipelineModel.save(path)
      }

    // · Save model into an external ml model repository
    if ((outputMode.get == MlPipelineSaveMode.MODELREP) || (outputMode.get == MlPipelineSaveMode.BOTH)) {
      // - Creating metadata dto
      val modelInfoDto = UploadModelRequestData(
        modelName = externalMlModelRepositoryModelName.get,
        user = "sparta",
        notebook = "sparta",
        modelDescription = "",
        framework = "spark",
        additionalInfo = ""
      )
      // - Executing uploading request
      val response: Try[UploadModelResponse] =
        mlModelRepClient.get.uploadSparkModel(modelInfoDto, dataFrame, pipelineModel, serializationLib.toString)

      response match {
        case Success(_) => None
        case Failure(e) => throw e
      }
    }
  }

  /**
    * Deserialize the pipeline descriptor in Json format provided in input properties map
    *
    * @return An array of PipelineStageDescriptor instances
    */
  def getPipelineDescriptor: Try[Array[PipelineStageDescriptor]] = Try {
    // Getting pipeline descriptor object
    read[Array[PipelineStageDescriptor]](
      pipelineJson.getOrElse(throw new Exception("The pipeline JSON descriptor is not provided.")))
  }

  /**
    * Builds the pipeline object using the array of PipelineStageDescriptor instances
    *
    * @param pipelineDescriptor == array of PipelineStageDescriptor instances
    * @return Pipeline instance
    */
  def getPipelineFromDescriptor(pipelineDescriptor: Array[PipelineStageDescriptor]): Try[Pipeline] = {

    // · Traversing the array of PipelineStageDescriptor for constructing an array of SparkML PipelineStages
    val stages: Array[Try[PipelineStage]] = for (stageDescriptor <- pipelineDescriptor) yield {
      Try {
        // · Instantiate SparkML PipelineStage class
        val stage = Try {
          assert(stageDescriptor.className.startsWith("org.apache.spark.ml"))
          Class.forName(stageDescriptor.className).getConstructor(classOf[String]).newInstance(stageDescriptor.uid)
        }.getOrElse(throw new Exception(
          s"Error instantiating PipelineStage '${stageDescriptor.name}@id(${stageDescriptor.uid})': " +
            s"invalid 'className=${stageDescriptor.className}'"))

        // · Set parameters of SparkML PipelineStage instance
        val parameterValidator: Seq[Try[Params]] = stageDescriptor.properties.map { case (paramName, paramValue) => Try {
          // - Getting parameter from PipelineStage using its name
          val paramToSet: Param[Any] = Try(stage.asInstanceOf[Params].getParam(paramName)
          ).getOrElse(throw new Exception(
            s"PipelineStage '${stageDescriptor.name}@id(${stageDescriptor.uid})' " +
              s"don't have a parameter named '$paramName'."))
          // - Getting value of parameter decoding the string value set in PipelineStageDescriptor
          Try {
            val valueToSet = MlPipelineDeserializationUtils.decodeParamValue(paramToSet, paramValue)
            stage.asInstanceOf[Params].set(paramToSet, valueToSet.get)
          }.getOrElse(throw new Exception(
            s"Parameter '$paramName' of PipelineStage " +
              s"'${stageDescriptor.name}@id(${stageDescriptor.uid})' has an invalid value " +
              s"(it must be a ${MlPipelineDeserializationUtils.decodeParamValue(paramToSet).get})."
          ))
        }
        }.toSeq

        Try(parameterValidator.map(_.get)).getOrElse(
          throw new Exception(parameterValidator.collect { case Failure(t) => t }.map(_.getMessage).mkString("\n"))
        )

        stage.asInstanceOf[PipelineStage]
      }
    }

    val validatedStages: Either[Array[Throwable], Pipeline] = Try(
      Right(new Pipeline().setStages(stages.map(_.get)))).getOrElse(Left(stages.collect { case Failure(t) => t }))

    validatedStages match {
      case Left(errors) => Failure(new Exception(errors.map(_.getMessage).mkString("\n")))
      case Right(pipeline) => Success(pipeline)
    }
  }
}

object MlPipelineOutputStep {

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