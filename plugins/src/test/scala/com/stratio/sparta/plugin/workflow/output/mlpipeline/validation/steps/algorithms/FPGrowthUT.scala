/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import java.io.{Serializable => JSerializable}

import com.github.fommil.netlib.BLAS.{getInstance => blas}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.{Failure, Random, Try}

@RunWith(classOf[JUnitRunner])
class FPGrowthUT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait ReadDescriptorResource{
    def getJsonDescriptor(filename:String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/singlesteps/algorithms/fpgrowth/" + filename)).mkString
    }
  }

  trait WithExampleData {
      val training: DataFrame = sparkSession.createDataFrame(Seq(
        (0, Array("1", "2")),
        (0, Array("1", "2")),
        (0, Array("1", "2")),
        (0, Array("1", "3"))
      )).toDF("id", "items")
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

  "ALS with default configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("fpgrowth-default-params-v0.json")))

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

  "ALS with empty configuration values" should "provide a valid SparkMl pipeline than it can be trained in a workflow (given no valid column names)" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("fpgrowht-emtpy-params-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isSuccess)

    }

  /* -------------------------------------------------------------
 => Wrong Pipeline construction and execution
  ------------------------------------------------------------- */

  "ALS with empty configuration values" should "not provide a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("fpgrowth-wrong-columnname-values-v0.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      //TODO add expected error tye assert(validation.get.valid)
      val execution = Try{executeStepAndUsePipeline(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

}
