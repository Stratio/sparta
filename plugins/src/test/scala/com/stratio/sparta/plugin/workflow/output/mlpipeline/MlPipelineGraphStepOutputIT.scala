/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.ValidationErrorMessages
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.io.Source
import scala.util.{Failure, Try}

@RunWith(classOf[JUnitRunner])
class MlPipelineGraphStepOutputIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait ReadDescriptorResource{
    def getJsonDescriptor(filename:String): String = {
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

  "MlPipeline" should "construct a valid SparkMl pipeline than it can be trained in a workflow" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("nlp_pipeline_good.json")))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)
      assert(validation.get.valid)

      executeStepAndUsePipeline(training, properties)
    }

  /* -------------------------------------------------------------
     => Incorrect Pipeline Graphs
      ------------------------------------------------------------- */
  "MlPipeline" should "show a validation error with graph with two inputs" in
    new ReadDescriptorResource with WithValidateStep with WithFilesystemProperties {
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_graph_two_inputs.json")))
      val validation = Try(validateMlPipelineStep(properties))
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length == 1)
      assert(validation.get.messages(0).message
          .contains(ValidationErrorMessages.moreThanOneStart))
    }

  "MlPipeline" should "show a validation error with graph with two outputs" in
    new ReadDescriptorResource with WithValidateStep with WithFilesystemProperties {
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_graph_two_outputs.json")))
      val validation = Try(validateMlPipelineStep(properties))
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length == 1)
      assert(validation.get.messages(0).message
        .contains(ValidationErrorMessages.moreThanOneEnd))
    }

  "MlPipeline" should "show a validation error with graph with a loop" in
    new ReadDescriptorResource with WithValidateStep with WithFilesystemProperties {
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_graph_with_loop.json")))
      val validation = Try(validateMlPipelineStep(properties))
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length == 1)
      assert(validation.get.messages(0).message
        .contains(ValidationErrorMessages.moreThanOneOutput("HashingTF")))
    }

  "MlPipeline" should "show a validation error with graph with a floating node" in
    new ReadDescriptorResource with WithValidateStep with WithFilesystemProperties {
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_graph_floating_node.json")))
      val validation = Try(validateMlPipelineStep(properties))
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length == 1)
      assert(validation.get.messages(0).message
        .contains(ValidationErrorMessages.moreThanOneStart))
    }


  /* -------------------------------------------------------------
     => Incorrect Pipeline construction and execution
      ------------------------------------------------------------- */

  // *********************************
  //  JSON descriptor related errors
  // *********************************

  // - Empty JSON Pipeline descriptor
  // · On validation
  "MlPipeline" should "show a validation error message with an empty JSON descriptor" in
    new WithValidateStep with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(""))

      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message == ValidationErrorMessages.emptyJsonPipelineDescriptor)
    }
  // · On execution
  "MlPipeline" should "throw an error with an empty JSON descriptor when executing workflow" in
    new WithExampleData with WithExecuteStep with WithFilesystemProperties{

      properties = properties.updated("pipeline", JsoneyString(""))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

  // - Bad JSON formatted Pipeline descriptor
  // · On validation
  "MlPipeline" should "show a validation error message with non valid formatted JSON descriptor" in
    new WithValidateStep with WithFilesystemProperties{
      properties = properties.updated("pipeline", JsoneyString("aaaaa"))

      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.invalidJsonFormatPipelineGraphDescriptor))
    }
  // · On execution
  "MlPipeline" should "throw an error with an invalid JSON format when executing workflow" in
    new WithExampleData with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline", JsoneyString("aaaaa"))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

  // - JSON Pipeline descriptor without a required property
  // · On validation
  "MlPipeline" should "show a validation error message with a JSON descriptor without a required property" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("nlp_pipeline_bad_noName.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.invalidJsonFormatPipelineGraphDescriptor))
    }
  // · On execution
  "MlPipeline" should "throw an error with a JSON descriptor without a required property when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor("nlp_pipeline_bad_noName.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

  // - JSON Pipeline descriptor with a bad property name
  // · On validation
  "MlPipeline" should "show a validation error message with a JSON descriptor with a bad property name" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_property_name.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==2)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
    }
  // · On execution
  "MlPipeline" should "throw an error with a JSON descriptor with a bad property name when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_property_name.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.toString) }
    }

  // *********************************
  //  Pipeline building related errors
  // *********************************

  // - Non existent PipelineStage/s
  // · On validation
  "MlPipeline" should "show a validation error message with a nonexistent stage" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_stage.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==2)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
    }
  "MlPipeline" should "show a validation error message with nonexistent stages" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_stages.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==3)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
    }
  // · On execution
  "MlPipeline" should "throw an error with a nonexistent stage when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_stage.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }
  "MlPipeline" should "throw an error with nonexistent stages when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_stages.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }

  // - Non existent Param/s in a PipelineStage
  // · On validation
  "MlPipeline" should "show a validation error message with a nonexistent parameter" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_parameter.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==2)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
    }
  "MlPipeline" should "show a validation error message with nonexistent parameters" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_parameters.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==3)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
    }
  // · On execution
  "MlPipeline" should "throw an error with a nonexistent parameter when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_parameter.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }
  "MlPipeline" should "throw an error with nonexistent parameters when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_nonexistent_parameters.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }

  // - Invalid Param value in a PipelineStage
  // · On validation
  "MlPipeline" should "show a validation error message with a invalid parameter value" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_param_value.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==2)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
    }
  "MlPipeline" should "show a validation error message with invalid parameters values" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_params_values.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==3)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
    }
  // · On execution
  "MlPipeline" should "throw an error with a invalid parameter value when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_param_value.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }
  "MlPipeline" should "throw an error with invalid parameters values when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_params_values.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }

  // - Multiple errors in definition
  // · On validation
  "MlPipeline" should "show a validation error message with multiple errors in definition" in
    new WithValidateStep with ReadDescriptorResource with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_multi_definition_error.json")))
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess && !validation.get.valid)
      assert(validation.get.messages.length==1)
      assert(validation.get.messages(0).message.startsWith(ValidationErrorMessages.invalidJsonFormatPipelineDescriptor))
    }
  // · On execution
  "MlPipeline" should "throw an error with multiple errors in definition when executing workflow" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_multi_definition_error.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }

  // *********************************
  //  Executing workflow errors
  // *********************************
  // · On execution
  "MlPipeline" should "throw an error with a non existent input column in training df or another pipelineStage" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_df_no_input_column.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }

  "MlPipeline" should "throw an error with non existent input columns in training df or another pipelineStage" in
    new WithExampleData with ReadDescriptorResource with WithExecuteStep with WithFilesystemProperties{
      properties = properties.updated("pipeline",
        JsoneyString(getJsonDescriptor("nlp_pipeline_bad_df_no_input_columns.json")))
      val execution = Try{executeStep(training, properties)}
      assert(execution.isFailure)
      execution match { case Failure(t) => log.info(t.getLocalizedMessage) }
    }

}
