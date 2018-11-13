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
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.enumerations.{MlPipelineSaveMode, MlPipelineSerializationLibs}
import com.stratio.sparta.plugin.workflow.output.mlpipeline.deserialization._
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.{PipelineGraphValidator, ValidationErrorMessages}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, PipelineGraph}
import org.apache.spark.ml.param._
import org.apache.spark.ml.{Pipeline, PipelineModel, PipelineStage}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}
import MlPipelineDeserializationUtils.{okParam, decodeParamValue }

class MlPipelineOutputStep(
                            name: String,
                            xDSession: XDSession,
                            properties: Map[String, JSerializable]
                          ) extends OutputStep(name, xDSession, properties) with SpartaSerializer {

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
  // Pipeline Graph --> represents arcs and nodes as they comes form the fron editor
  lazy val pipelineGraph: Try[PipelineGraph] = getPipelineGraph
  // Pipeline descriptor --> Descriptor object that can be easily converted into a SparkML Pipeline
  lazy val pipelineDescriptor: Try[Seq[PipelineStageDescriptor]] = getPipelineDescriptor
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
    //Regular expression used to verify valid Model Names
    val mlModelNameRegExpr =
      """^(([a-z0-9]|[a-z0-9][a-z0-9\-]*[a-z0-9])\.)*([a-z0-9]|[a-z0-9][a-z0-9\-]*[a-z0-9])|(\.|\.\.)$""".r

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
        if (externalMlModelRepositoryModelName.isEmpty) {
          validation = addValidationError(validation, ValidationErrorMessages.mlModelModelName)
        } else {
          externalMlModelRepositoryModelName.get match {
            //check if the model name is valid //TODO this check should be done also in Front
            case mlModelNameRegExpr(_*) => None
            case _ => validation = addValidationError(validation, ValidationErrorMessages.mlModelRepModelInvalidModelName)
          }
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
    if (pipelineJson.isDefined && pipelineGraph.isFailure) {
      val error = pipelineGraph match {
        case Failure(f) => f
      }
      validation = addValidationError(validation,
        ValidationErrorMessages.invalidJsonFormatPipelineGraphDescriptor + s" ${error.getMessage}")
    }

    //pipeline graph is defined? (check is it has no nodes)
    if (pipelineGraph.isSuccess && pipelineGraph.get.nodes.isEmpty) {
      validation = addValidationError(validation, ValidationErrorMessages.emptyJsonPipelineDescriptor)
    }

    //validate the PipelineGraph
    if (pipelineGraph.isSuccess && !pipelineGraph.get.nodes.isEmpty && pipelineDescriptor.isFailure) {
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
          throw new Exception(ValidationErrorMessages.schemaError(stage.getClass.getSimpleName, stage.uid) + s"${e.getMessage}")
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
    * Deserialize the pipeline graph descriptor in Json format provided in input properties map
    *
    * @return An instance of PipelineGraph
    */
  def getPipelineGraph: Try[PipelineGraph] = Try {
    // Getting pipeline descriptor object
    // unescape JSON first
    read[PipelineGraph](pipelineJson.getOrElse(throw new Exception("The pipeline graph JSON descriptor is not provided.")))
  }

  /**
    * Validate the Graph Pipeline provided
    *
    * @return A Try of Seq[NodeGraph] with the ordered nodes if pipeline is valid
    *         or an error if the pipeline is not valid
    */
  def getValidOrderedPipelineGraph(aiPipelineGraph: Try[PipelineGraph]): Try[Seq[NodeGraph]] =
    aiPipelineGraph flatMap {
      new PipelineGraphValidator(_).validate
    }

  /**
    * Deserialize the AI pipeline descriptor in Json format provided in input properties map
    *
    * @return An array of PipelineStageDescriptor instances
    */
  def getPipelineDescriptor(): Try[Seq[PipelineStageDescriptor]] = Try {
    // Convert to pipeline descriptor object
    val validationResult: Try[Seq[NodeGraph]] = getValidOrderedPipelineGraph(pipelineGraph)
    validationResult match {
      case Success(stages) => for (e <- stages)
        yield PipelineStageDescriptor(
          e.classPrettyName,
          e.name,
          e.className,
          e.configuration
        )
      case Failure(t) => throw new Exception(t.getMessage)
    }
  }

  /**
    * Builds the pipeline object using the array of PipelineStageDescriptor instances
    *
    * @param pipelineDescriptor == array of PipelineStageDescriptor instances
    * @return Pipeline instance
    */
  def getPipelineFromDescriptor(pipelineDescriptor: Seq[PipelineStageDescriptor]): Try[Pipeline] = {

    // · Traversing the array of PipelineStageDescriptor for constructing an array of SparkML PipelineStages
    val stages: Seq[Try[PipelineStage]] = for (stageDescriptor <- pipelineDescriptor) yield {
      Try {
        // · Instantiate SparkML PipelineStage class
        val stage = Try {
          assert(stageDescriptor.className.startsWith("org.apache.spark.ml"))
          Class.forName(stageDescriptor.className).getConstructor(classOf[String]).newInstance(stageDescriptor.uid)
        }.getOrElse(throw new Exception(
                      s"Error instantiating PipelineStage '${stageDescriptor.name}@id(${stageDescriptor.uid})': " +
                        s"invalid 'className=${stageDescriptor.className}'"))

        // · Set parameters of SparkML PipelineStage instance
        // filter out parameters with null or empty values
        val parameterValidator: Seq[Try[Params]] = stageDescriptor.properties
          .filter((t) => okParam(t._2))
          .map { case (paramName, paramValue) => {
            // - Getting parameter from PipelineStage using its name
            val paramToSet: Param[Any] = Try(stage.asInstanceOf[Params].getParam(paramName))
              .getOrElse(throw new Exception(s"PipelineStage '${stageDescriptor.name}@id(${stageDescriptor.uid})' " +
                                               s"don't have a parameter named '$paramName'."))
            // - Getting value of parameter decoding the string value set in PipelineStageDescriptor
            decodeParamValue(paramToSet, paramValue)
              .transform(
                s => Try(stage.asInstanceOf[Params].set(paramToSet, s)),
                f => Failure(throw new Exception(f.getMessage + s" of PipelineStage '${stageDescriptor.name}@id(${stageDescriptor.uid})'"))
              ).transform(
                s => Success(s),
                f => Failure(
                  throw new Exception(
                    s"'${stageDescriptor.name}@id(${stageDescriptor.uid})' has an invalid value. Details: ${f.getMessage}"
                  )
                )
              )
            }
          }.toSeq

        Try(parameterValidator.map(_.get)).getOrElse(
          throw new Exception(parameterValidator.collect { case Failure(t) => t }.map(_.getMessage).mkString("\n"))
        )

        stage.asInstanceOf[PipelineStage]
      }
    }
    val stagesArray = stages.toArray[Try[PipelineStage]]
    val validatedStages: Either[Array[Throwable], Pipeline] = Try(
      Right(new Pipeline().setStages(stagesArray.map(_.get)))).getOrElse(Left(stagesArray.collect { case Failure(t) => t }))

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
