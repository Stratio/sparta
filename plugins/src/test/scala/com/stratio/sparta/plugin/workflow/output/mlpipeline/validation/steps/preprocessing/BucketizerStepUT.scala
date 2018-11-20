/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.apache.spark.ml.attribute.{Attribute, AttributeGroup, NumericAttribute}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.io.Source
import scala.util.Try

@RunWith(classOf[JUnitRunner])
class BucketizerStepUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  def stepName: String = "bucketizer"

  def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/bucketizer/"

  def trainingDf: DataFrame = {
    val data = Array(-0.5, -0.3, 0.0, 0.2)
    sparkSession.createDataFrame(data.map(Tuple1.apply)).toDF("features")
  }

  private def stepDefaultParamsPath = s"$resourcesPath$stepName-default-params-v0.json"

  private def stepEmptyParamsPath = s"$resourcesPath$stepName-empty-params-v0.json"

  private def stepWrongParamsPath = s"$resourcesPath$stepName-wrong-params-v0.json"

  private def stepWrongInputColumnPath = s"$resourcesPath$stepName-wrong-input-column-v0.json"

  trait ReadDescriptorResource {
    def getJsonDescriptor(filename: String): String = {
      Source.fromInputStream(getClass.getResourceAsStream(filename)).mkString
    }
  }

  trait WithExampleData {
    // Generate noisy input of the form Y = signum(x.dot(weights) + intercept + noise)
    def generateInputDf(): DataFrame = trainingDf
  }

  trait WithFilesystemProperties {
    var properties: Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.FILESYSTEM.toString),
      "path" -> JsoneyString("/tmp/pipeline_tests")
    )
  }

  trait WithExecuteStep {
    def executeStep(training: DataFrame, properties: Map[String, JSerializable]) {
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      mlPipelineOutput.save(training, SaveModeEnum.Overwrite, Map.empty[String, String])
    }

    def executeStepAndUsePipeline(training: DataFrame, properties: Map[String, JSerializable]) {
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      mlPipelineOutput.save(training, SaveModeEnum.Overwrite, Map.empty[String, String])
      // · Use pipeline object
      val pipelineModel = mlPipelineOutput.pipeline.get.fit(training)
      val df = pipelineModel.transform(training)
      df.show()
    }
  }

  trait WithValidateStep {
    def validateMlPipelineStep(properties: Map[String, JSerializable]): ErrorValidations = {
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      val e = mlPipelineOutput.validate()
      e.messages.foreach(x => log.info(x.message))
      e
    }
  }


  /* -------------------------------------------------------------
   => Correct Pipeline construction with valid params
   (in this step there is no default value for the parameter splits)
  ------------------------------------------------------------- */
  s"$stepName with both splits and handleInvalid set" should "provide a valid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}bucketizer-correct-params-v0.json"
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(filePath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)
      assert(validation.get.valid)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      //println("Execution: " + execution)
      assert(execution.isSuccess)
    }


  /* -------------------------------------------------------------
   => Correct Pipeline construction with valid params - splits defined but no handleInvalid
      (in this step there is no default value for the parameter splits)
  ------------------------------------------------------------- */
  s"$stepName with splits set but no handleInvalid" should "provide a valid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}bucketizer-no-handle-invalid-v0.json"
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(filePath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)
      assert(validation.get.valid)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      assert(execution.isSuccess)
    }


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with "default" params - splits not defined
    (in this step there is no default value for the parameter splits)
  ------------------------------------------------------------- */

  s"$stepName default configuration values" should "provide an invalid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(stepDefaultParamsPath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)
      assert(validation.get.valid)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      assert(execution.isFailure)
      log.info(execution.failed.get.toString)
    }

  /* -------------------------------------------------------------
   => Wrong Pipeline construction with wrong handleInvalid param
       (in this step there is no default value for the parameter splits)
  ------------------------------------------------------------- */

  s"$stepName with an invalid value for handleInvalid" should "provide an invalid SparkMl pipeline detected during validation" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}bucketizer-wrong-handle-invalid-v0.json"
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(filePath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)
      assert(!validation.get.valid)
      val errorMessages: Seq[String] = validation.get.messages.map(_.message)
      assert(errorMessages.exists(_ contains "handleInvalid"))
      assert(errorMessages.exists(_ contains "has an invalid value"))
    }

  /* -------------------------------------------------------------
   => Wrong Pipeline construction with input column not present in training DF
  ------------------------------------------------------------- */

  s"$stepName with invalid input column" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(stepWrongInputColumnPath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      assert(execution.isFailure)
      assert(execution.failed.get.getMessage.startsWith(ValidationErrorMessages.schemaErrorInit))
      log.info(execution.failed.get.toString)
    }
}
