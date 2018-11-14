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
import com.stratio.sparta.plugin.enumerations.{MlPipelineSaveMode, MlPipelineSerializationLibs}
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class MlPipelineUnsupportedStagesTests extends TemporalSparkContext with Matchers {

  trait WithExampleData {
    val training: DataFrame = sparkSession.createDataFrame(Seq(
      (0L, "a b c d e spark", 1.0),
      (1L, "b d", 0.0),
      (2L, "spark f g h", 1.0),
      (3L, "hadoop mapreduce", 0.0)
    )).toDF("id", "text", "label")
  }

  trait ReadDescriptorResource {
    def getJsonDescriptor(filename: String): String = {
      Source.fromInputStream(getClass.getResourceAsStream("/mlpipeline/singlesteps/" + filename)).mkString
    }
  }

  trait WithMandatoryProperties {
    var properties: Map[String, JSerializable] = Map(
      "output.mode" -> JsoneyString(MlPipelineSaveMode.MODELREP.toString),
      "mlmodelrepModelName" -> "localtest"
    )
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


  "FPGrowth" should "not be supported with mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/fpgrowth/fpgrowth-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.fpm.FPGrowth")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))

    }

  "FPGrowth" should "not be supported with spark and mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/fpgrowth/fpgrowth-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK_AND_MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.fpm.FPGrowth")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))
    }


  "FPGrowth" should "be supported with spark serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/fpgrowth/fpgrowth-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)

      assert(validationResult.valid)
    }


  "ALS" should "not be supported with mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/als/als-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.recommendation.ALS")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))

    }

  "ALS" should "not be supported with spark and mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/als/als-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK_AND_MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.recommendation.ALS")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))
    }


  "ALS" should "be supported with spark serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/als/als-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)

      assert(validationResult.valid)
    }

  //
  "BucketedRandomProjectionLSH" should "not be supported with mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("preprocessing/bucketedrandomprojectionlsh/bucketed-random-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.feature.BucketedRandomProjectionLSH")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))

    }

  "BucketedRandomProjectionLSH" should "not be supported with spark and mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("preprocessing/bucketedrandomprojectionlsh/bucketed-random-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK_AND_MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.feature.BucketedRandomProjectionLSH")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))
    }


  "BucketedRandomProjectionLSH" should "be supported with spark serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("preprocessing/bucketedrandomprojectionlsh/bucketed-random-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)

      assert(validationResult.valid)
    }


  //

  "LinearSVC" should "not be supported with mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/linearsvc/linearsvc-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.classification.LinearSVC")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))

    }

  "LinearSVC" should "not be supported with spark and mleap serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/linearsvc/linearsvc-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK_AND_MLEAP.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)
      lazy val messageError: String =  ValidationErrorMessages.stageNotSupportedMleap("org.apache.spark.ml.classification.LinearSVC")

      assert(!validationResult.valid)
      assert(validationResult.messages.last.message.contains(s"${messageError}"))
    }


  "LinearSVC" should "be supported with spark serialization" in
    new ReadDescriptorResource with WithExampleData with WithMandatoryProperties with WithValidateStep {
      properties = properties.updated(
        "pipeline", JsoneyString(getJsonDescriptor("algorithms/linearsvc/linearsvc-default-params-v0.json"))
      ).updated("serializationLib", JsoneyString(MlPipelineSerializationLibs.SPARK.toString))

      lazy val validationResult: ErrorValidations = validateMlPipelineStep(properties)

      assert(validationResult.valid)
    }
}
