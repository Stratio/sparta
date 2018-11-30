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
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Minutes, Span}
import org.scalatest.{BeforeAndAfterAll, _}

import scala.io.Source
import scala.util.Try
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class MlPipelineComplexGraphStepOutputIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

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
      (60, "US", "Syrupy and dense, this wine is jammy in plum and vanilla, with indeterminate structure and plenty of oak. Ripe and full-bodied, it has accents of graphite and leather.", "Estate", 86, 100.0, "California", "Napa Valley", "Napa", "Virginie Boone", "@vboone", "Okapi 2013 Estate Cabernet Sauvignon (Napa Valley)", "Cabernet Sauvignon", "Okapi"),
      (62, "US", "The aromas are brooding, with notes of barrel spice and cherry. The flavors are tart and elegant in style, with lightly gritty tannins backing them up. Best suited to the dinner table.", "Alder Ridge Vineyard", 86, 25.0, "Washington", "Columbia Valley (WA)", "Columbia Valley", "Sean P. Sullivan", "@wawinereport", "Ram 2014 Alder Ridge Vineyard Cabernet Franc (Columbia Valley (WA))", "Cabernet Franc", "Ram")
    )).toDF("id", "country", "description", "designation", "points", "price", "province", "region_1", "region_2", "taster_name", "taster_twitter_handle", "title", "variety", "winery")
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
   => Correct Pipeline construction and execution
    ------------------------------------------------------------- */

  "MlPipeline" should "construct a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("complexTest.json")))

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

  "MlPipeline" should "construct an invalid SparkMl pipeline than it can be trained in a workflow an return correct errors" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("complexTest-wrong.json")))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)
      assert(!validation.get.valid)
      assert(!validation.get.messages.isEmpty)
      assert(validation.get.messages.size == 8)
      assert(validation.get.messages(0).subStep == None)
      assert(validation.get.messages(1).subStep != None)
      assert(validation.get.messages(2).subStep != None)
      assert(validation.get.messages(3).subStep != None)
      assert(validation.get.messages(4).subStep != None)
      assert(validation.get.messages(5).subStep != None)
      assert(validation.get.messages(6).subStep != None)
      assert(validation.get.messages(7).subStep != None)
    }


}
