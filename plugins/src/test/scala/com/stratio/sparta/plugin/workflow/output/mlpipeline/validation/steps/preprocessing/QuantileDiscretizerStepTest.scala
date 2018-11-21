/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.{GenericPipelineStepTest, ValidationErrorMessages}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class QuantileDiscretizerStepTest extends GenericPipelineStepTest {

  override def stepName: String = "quantile-discretizer"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/quantile-discretizer/"

  override def trainingDf: DataFrame = {
    val data = Array((0, 18.0), (1, 19.0), (2, 8.0), (3, 5.0), (4, 2.2))
    sparkSession.createDataFrame(data).toDF("id", "hour")
  }


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with invalid params
  ------------------------------------------------------------- */

  s"$stepName with wrong handle Invalid param" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {

      val filePath = s"$resourcesPath$stepName-wrong-params-2-v0.json"
      properties = properties.updated("pipeline", JsoneyString(getJsonDescriptor(filePath)))

      // Validation step mut be done correctly
      val validation = Try {
        validateMlPipelineStep(properties)
      }
      assert(validation.isSuccess)

      val execution = Try {
        executeStepAndUsePipeline(generateInputDf(), properties)
      }
      assert(execution.isFailure)
      assert(execution.failed.get.getMessage.startsWith(ValidationErrorMessages.errorBuildingPipelineInstance))
      log.info(execution.failed.get.toString)
    }

}
