/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.constants.SdkConstants
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.io.Source
import scala.util.Try

@RunWith(classOf[JUnitRunner])
class MlPipelineOutputModeValidationTest extends TemporalSparkContext with Matchers{

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

  trait WithValidPipeline {
    def getJsonDescriptor(filename:String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/" + filename)).mkString
    }
    var properties: Map[String, JSerializable] = Map(
      "pipeline" -> JsoneyString(getJsonDescriptor("nlp_pipeline_good.json")))
  }

  "MlPipeline" should "show a validation error with if output.mode property is not defined" in
    new WithValidPipeline with WithValidateStep {
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message == ValidationErrorMessages.invalidSaveMode)
    }

  "MlPipeline" should "show a validation error with a invalid output.mode property value" in
    new WithValidPipeline with WithValidateStep {

      properties = properties.updated("output.mode", "blablabla")
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message == ValidationErrorMessages.invalidSaveMode)
    }

  "MlPipeline" should "show a validation error with filesystem output.mode without defining a path" in
    new WithValidPipeline with WithValidateStep {

      properties = properties.updated("output.mode", MlPipelineSaveMode.FILESYSTEM)
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message == ValidationErrorMessages.nonDefinedPath)
    }

  "MlPipeline" should "show a validation error with mlModelRepo output.mode without defining the model name" in
    new WithValidPipeline with WithValidateStep {

      properties = properties.updated("output.mode", MlPipelineSaveMode.MODELREP)
        .updated(SdkConstants.ModelRepositoryUrl, "http://localhost:8000")
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message == ValidationErrorMessages.mlModelModelName)
    }

}
