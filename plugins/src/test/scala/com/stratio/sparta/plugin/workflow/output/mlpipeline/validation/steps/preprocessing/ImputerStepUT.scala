/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.{GenericPipelineStepTest, ValidationErrorMessages}
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ImputerStepUT extends GenericPipelineStepTest {

  override def stepName: String = "imputer"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/imputer/"

  override def trainingDf: DataFrame =sparkSession.createDataFrame( Seq(
    (0, 1.0, 4.0),
    (1, 11.0, 12.0),
    (2, 3.0, Double.NaN),
    (3, Double.NaN, 14.0)
  )).toDF("id", "value1", "value2")


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with wrong number of output columns
  ------------------------------------------------------------- */

  s"$stepName with wrong number of  output columns" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemAndAllSerializationProperties{
      val filePath = s"${resourcesPath}imputer-wrong-output-columns-v0.json"
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(filePath)))

      // Validation step mut be done correctly
      val validation = Try{validateMlPipelineStep(properties)}
      assert(validation.isSuccess)

      val execution = Try{executeStepAndUsePipeline(generateInputDf(), properties)}
      assert(execution.isFailure)
      assert(execution.failed.get.getMessage.startsWith(ValidationErrorMessages.schemaErrorInit))
      log.info(execution.failed.get.toString)
    }

}
