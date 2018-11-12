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
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.io.Source
import scala.util.{Failure, Try}

@RunWith(classOf[JUnitRunner])
class LDAUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait ReadDescriptorResource{
    def getJsonDescriptor(filename:String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/singlesteps/algorithms/lda/" + filename)).mkString
    }
  }

  trait WithExampleData {
    case class TestRow(features: Vector)


    val avgWC = 1  // average instances of each word in a doc
    val rng = new java.util.Random()
    rng.setSeed(1)
    val rdd = sc.parallelize(1 to 10).map { i =>
      Vectors.dense(Array.fill(120)(rng.nextInt(2 * avgWC).toDouble))
    }.map(v => new TestRow(v))


    val training: DataFrame = sparkSession.createDataFrame(rdd)
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
   => Correct Pipeline construction and execution
    ------------------------------------------------------------- */

  "LDA with default configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow (given valid user and item columns)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("lda-default-params-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      assert(validation.get.valid)

      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isSuccess)

    }

  /* -------------------------------------------------------------
=> Wrong Pipeline construction and execution
------------------------------------------------------------- */

  "LDA with empty configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow (given default user and item columns)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("lda-emtpy-params-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isSuccess)

    }

  /* -------------------------------------------------------------
 => Wrong Pipeline construction and execution
  ------------------------------------------------------------- */

  "LDA with wrong configuration values" should "not provide a valid SparkMl pipeline than it can be trained in a workflow (given no valid column names)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("lda-wrong-columnname-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      //TODO add expected error tye assert(validation.get.valid)
      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }


  /* -------------------------------------------------------------
 => Wrong Pipeline construction and execution
  ------------------------------------------------------------- */

  "LDA with wrong configuration values" should "not provide a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("lda-wrong-column-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      //TODO add expected error tye assert(validation.get.valid)
      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

}
