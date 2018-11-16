/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.io.Source
import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ElementWiseProductStepUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  def stepName: String = "elementwiseproduct"

  def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/elementwiseproduct/"

  def trainingDf: DataFrame = {
    sparkSession.createDataFrame(Seq(
      ("a", Vectors.dense(1.0, 2.0, 3.0)),
      ("b", Vectors.dense(4.0, 5.0, 6.0)))).toDF("id", "vector")
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
   (in this step there is no default value for the parameter scalingVector)
  ------------------------------------------------------------- */
  s"$stepName with scalingVector set" should "provide a valid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}elementwiseproduct-correct-params-v0.json"
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
      println("Execution: " + execution)
      assert(execution.isSuccess)
    }


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with "default" params - scalingVec not defined
      (in this step there is no default value for the parameter scalingVector)
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
   => Wrong Pipeline construction with wrong scalingVec param - bad dimension
       (in this step there is no default value for the parameter splits)
  ------------------------------------------------------------- */

  s"$stepName with a scalingVec of wrong dimension" should "provide an invalid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(stepWrongParamsPath)))

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
   => Wrong Pipeline construction with wrong scalingVec param - empty array
       (in this step there is no default value for the parameter splits)
  ------------------------------------------------------------- */

  s"$stepName with an empty array for scalingVec" should "provide an invalid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}elementwiseproduct-wrong-params2-v0.json"
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
      assert(execution.isFailure)
      log.info(execution.failed.get.toString)

    }

  /* -------------------------------------------------------------
   => Wrong Pipeline construction with wrong scalingVec param - not an array
       (in this step there is no default value for the parameter splits)
  ------------------------------------------------------------- */

  s"$stepName with not an array for scalingVec" should "provide an invalid SparkMl pipeline detected during validation" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}elementwiseproduct-wrong-params3-v0.json"
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(filePath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }

      assert(validation.isSuccess)
      assert(!validation.get.valid)
      val errorMessages: Seq[String] = validation.get.messages.map(_.message)
      assert(errorMessages.exists(_ contains "scalingVec"))
      assert(errorMessages.exists(_ contains "Values must be"))
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
