/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline

import java.io.{Serializable => JSerializable}

import akka.util.Timeout
import com.stratio.sparta.core.constants.SdkConstants
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.{MlPipelineSaveMode, MlPipelineSerializationLibs}
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Minutes, Span}

import scala.io.Source
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class MlModelRepositoryServiceTest extends TemporalSparkContext with Matchers {

  override val timeLimit = Span(1, Minutes)

  override val timeout = Timeout(1 minutes)

  trait ReadDescriptorResource {
    def getJsonDescriptor(filename: String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/" + filename)).mkString
    }
  }

  trait WithExampleData {
    val training: DataFrame = sparkSession.createDataFrame(Seq(
      (0L, "a b c d e spark", 1.0),
      (1L, "b d", 0.0),
      (2L, "spark f g h", 1.0),
      (3L, "hadoop mapreduce", 0.0)
    )).toDF("id", "text", "label")
  }

  trait WithLocalRepositoryProperties {
    var properties: Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.MODELREP.toString),
      "mlmodelrepModelName" -> "localtest",
      "mlmodelrepModelTmpDir" -> "/tmp",
      SdkConstants.ModelRepositoryUrl -> "http://localhost:8080",
      "serializationLib" -> JsoneyString(MlPipelineSerializationLibs.MLEAP.toString)
    )
  }

  trait WithLocalRepositoryPropertiesInvalidName {
    var properties: Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.MODELREP.toString),
      "mlmodelrepModelName" -> "123_%456",
      "mlmodelrepModelTmpDir" -> "/tmp",
      SdkConstants.ModelRepositoryUrl -> "http://localhost:8080",
      "serializationLib" -> JsoneyString(MlPipelineSerializationLibs.SPARK.toString)
    )
  }

  trait WithLocalRepositoryPropertiesmissingName {
    var properties: Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.MODELREP.toString),
      "mlmodelrepModelTmpDir" -> "/tmp",
      SdkConstants.ModelRepositoryUrl -> "http://localhost:8080",
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


  // TODO - Mock for unit test - Integration test
  // Note: only in local environment

  "MlModelRepositoryClient" should "accept a model with valid name" in
    new ReadDescriptorResource with WithExampleData with WithValidateStep
      with WithLocalRepositoryProperties {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("nlp_pipeline_good.json"))
      )
      assert(validateMlPipelineStep(properties).valid)
    }


  "MlModelRepositoryClient" should "not accept a model with a missing name" in
    new ReadDescriptorResource with WithExampleData with WithValidateStep
      with WithLocalRepositoryPropertiesmissingName {
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("nlp_pipeline_good.json")))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      assert(!validationResult.valid)
      assert(validationResult.messages.head.message.equals(ValidationErrorMessages.mlModelModelName))
    }

  "MlModelRepositoryClient" should "not accept a model with an invalid name" in
    new ReadDescriptorResource with WithExampleData with WithValidateStep
      with WithLocalRepositoryPropertiesInvalidName {
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("nlp_pipeline_good.json")))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      assert(!validationResult.valid)
      assert(validationResult.messages.head.message.equals(ValidationErrorMessages.mlModelRepModelInvalidModelName))
    }

}
