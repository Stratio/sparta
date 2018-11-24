/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline

import java.io.{Serializable => JSerializable}

import akka.util.Timeout
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.{MlPipelineSaveMode, MlPipelineSerializationLibs}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Minutes, Span}
import org.scalatest.{BeforeAndAfterAll, _}

import scala.io.Source
import scala.util.Try
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class MlPipelinePreprocessingComplexGraphTest extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>


  override val timeLimit = Span(1, Minutes)

  override val timeout = Timeout(1 minutes)

  trait ReadDescriptorResource {
    def getJsonDescriptor(filename: String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/complexpipelines/" + filename)).mkString
    }
  }

  trait WithExampleData {
    val training: DataFrame = sparkSession.createDataFrame(Seq(
      (0, 0.0, "Hi I heard about Spark", Vectors.sparse(5, Seq((1, 1.0), (3, 7.0))), 1, 1, 2, 3, -0.5, 2.0),
      (1, 0.0, "I wish Java could use case classes", Vectors.dense(2.0, 0.0, 3.0, 4.0, 5.0), 2, 4, 3, 8, -0.3, Double.NaN),
      (2, 1.0, "Logistic regression models are neat", Vectors.dense(4.0, 0.0, 0.0, 6.0, 7.0), 3, 6, 1, 9, 0.2, 8.0)
    )).toDF("id", "sentenceLabel", "sentence", "vectorPCAIn", "num1", "num2", "num3", "num4", "bucketedData", "imputerData")
  }

  trait WithFilesystemProperties {
    var properties: Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.FILESYSTEM.toString),
      "path" -> JsoneyString("/tmp/pipeline_tests"),
      "serializationLib" -> JsoneyString(MlPipelineSerializationLibs.SPARK.toString)
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
   => Correct Pipeline construction and execution
    ------------------------------------------------------------- */

  "MlPipeline" should "construct a valid SparkMl complex pre-processing pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("pre-processing-complex-v0.json")))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)
      assert(validation.get.valid)

      val execution = Try {
        executeStepAndUsePipeline(training, properties)
      }
      assert(execution.isSuccess)
    }


  /* -------------------------------------------------------------
 => Correct Pipeline construction and execution
  ------------------------------------------------------------- */

  "MlPipeline" should "construct a valid SparkMl complex logistic regression pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("logistic-regression-complex-v0.json")))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)
      assert(validation.get.valid)

      val execution = Try {
        executeStepAndUsePipeline(training, properties)
      }
      assert(execution.isSuccess)
    }
}
