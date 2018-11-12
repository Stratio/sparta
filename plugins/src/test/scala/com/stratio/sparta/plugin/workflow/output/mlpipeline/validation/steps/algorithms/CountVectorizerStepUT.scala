/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import java.io.{Serializable => JSerializable}

import breeze.optimize.linear.PowerMethod.BDV
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.{DataFrame, Dataset}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.io.Source
import scala.util.{Failure, Random, Try}

@RunWith(classOf[JUnitRunner])
class CountVectorizerStepUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait ReadDescriptorResource{
    def getJsonDescriptor(filename:String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/singlesteps/preprocessing/countvectorizer/" + filename)).mkString
    }
  }

  trait WithExampleData {
    // Generate noisy input of the form Y = signum(x.dot(weights) + intercept + noise)
    def generateInputDf(): DataFrame = {
      sparkSession.createDataFrame(Seq(
        (0, Array("a", "b", "c")),
        (1, Array("a", "b", "b", "c", "a"))
      )).toDF("id", "words")
    }
  }

  trait WithFilesystemProperties {
    var properties:Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.FILESYSTEM.toString),
      "path" -> JsoneyString("/tmp/pipeline_tests")
    )
  }

  trait WithExecuteStep{
    def executeStep(training:DataFrame, properties:Map[String, JSerializable]){
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      mlPipelineOutput.save(training, SaveModeEnum.Overwrite, Map.empty[String, String])
    }

    def executeStepAndUsePipeline(training:DataFrame, properties:Map[String, JSerializable]){
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

  trait WithValidateStep{
    def validateMlPipelineStep(properties:Map[String, JSerializable]): ErrorValidations = {
      // · Creating outputStep
      val mlPipelineOutput = new MlPipelineOutputStep("MlPipeline.out", sparkSession, properties)
      // · Executing step
      val e = mlPipelineOutput.validate()
      e.messages.foreach(x => log.info(x.message))
      e
    }
  }

  /* -------------------------------------------------------------
   => Correct Pipeline construction with default params
  ------------------------------------------------------------- */

  "CountVectorizer default configuration values" should "provide a valid SparkMl pipeline" in
  new WithFilesystemProperties with WithExampleData with WithExecuteStep with WithValidateStep with ReadDescriptorResource {
    properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("count-vectorizer-default-params-v0.json")))

    // Validation step mut be done correctly
    val validation = Try{validateMlPipelineStep(properties)}
    assert(validation.isSuccess)
    assert(validation.get.valid)

    executeStepAndUsePipeline(generateInputDf(), properties)
  }


  /* -------------------------------------------------------------
   => Correct Pipeline construction with empty params
  ------------------------------------------------------------- */

  "CountVectorizer with empty configuration values" should "provide a valid SparkMl pipeline using default values" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("count-vectorizer-empty-params-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      executeStepAndUsePipeline(generateInputDf(), properties)
    }


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with invalid params
  ------------------------------------------------------------- */

  "CountVectorizer with wrong configuration parmas" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("count-vectorizer-wrong-params-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      val execution = Try{executeStepAndUsePipeline(generateInputDf(), properties)}
      assert(execution.isFailure)
      log.info(execution.failed.get.toString)
    }


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with input column not present in training DF
  ------------------------------------------------------------- */

  "CountVectorizer with invalid input column" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("count-vectorizer-wrong-input-column-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      val execution = Try{executeStepAndUsePipeline(generateInputDf(), properties)}
      assert(execution.isFailure)
      log.info(execution.failed.get.toString)
    }
}
