/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.apache.spark.ml.attribute.{Attribute, AttributeGroup, NumericAttribute}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row}
import org.scalatest.{BeforeAndAfterAll, _}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source
import scala.util.Try

@RunWith(classOf[JUnitRunner])
class VectorSlicerStepUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  def stepName: String = "vectorslicer"

  def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/vectorslicer/"

  def trainingDf: DataFrame = {

    val numUserFeatures = 5
    val data = Array(
      Vectors.sparse(numUserFeatures, Seq((0, -2.0), (1, 2.3))),
      Vectors.dense(-2.0, 2.3, 0.0, 0.0, 1.0),
      Vectors.dense(0.0, 0.0, 0.0, 0.0, 0.0),
      Vectors.dense(0.6, -1.1, -3.0, 4.5, 3.3),
      Vectors.sparse(numUserFeatures, Seq())
    )

    val defaultAttr = NumericAttribute.defaultAttr
    val attrs = Array("f0", "f1", "f2", "f3", "f4").map(defaultAttr.withName)
    val attrGroup = new AttributeGroup("userFeatures", attrs.asInstanceOf[Array[Attribute]])

    val rdd = sc.parallelize(data).map {
      Row(_)
    }
    sparkSession.createDataFrame(rdd,
      StructType(Array(attrGroup.toStructField())))

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
   => Correct Pipeline construction with valid params - indices
   (in this step default params are not valid, both names and indices are set to empty array,
   but al least one of them should be non-empty)
  ------------------------------------------------------------- */
  s"$stepName with indices set" should "provide a valid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}vectorslicer-correct-params-indices-v0.json"
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
   => Correct Pipeline construction with valid params - names
   (in this step default params are not valid, both names and indices are set to empty array,
   but al least one of them should be non-empty)
  ------------------------------------------------------------- */
  s"$stepName with names set" should "provide a valid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}vectorslicer-correct-params-names-v0.json"
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
   => Correct Pipeline construction with valid params - both
   (in this step default params are not valid, both names and indices are set to empty array,
   but al least one of them should be non-empty)
  ------------------------------------------------------------- */
  s"$stepName with both names and indices set" should "provide a valid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}vectorslicer-correct-params-both-v0.json"
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
     => Wrong Pipeline construction with overlapping in names and indices
     (in this step default params are not valid, both names and indices are set to empty array,
     but al least one of them should be non-empty)
    ------------------------------------------------------------- */

  s"$stepName with both names and indices set overlapping" should "provide an invalid SparkMl pipeline" in
    new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
      val filePath = s"${resourcesPath}vectorslicer-wrong-params-overlap-v0.json"
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
   => Wrong Pipeline construction with default params
   (in this step default params are not valid, both names and indices are set to empty array,
   but al least one of them should be non-empty)
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
   => Wrong Pipeline construction with empty params
   (in this step default params are not valid, both names and indices are set to empty array,
   but al least one of them should be non-empty)
  ------------------------------------------------------------- */

  s"$stepName with empty configuration values" should "provide an invalid SparkMl pipeline using default values" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(stepEmptyParamsPath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      assert(execution.isFailure)
      log.info(execution.failed.get.toString)

    }


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with invalid params
  ------------------------------------------------------------- */

  s"$stepName with wrong configuration parmas" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(stepWrongParamsPath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      assert(execution.isFailure)
      assert(execution.failed.get.getMessage.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
      log.info(execution.failed.get.toString)
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
