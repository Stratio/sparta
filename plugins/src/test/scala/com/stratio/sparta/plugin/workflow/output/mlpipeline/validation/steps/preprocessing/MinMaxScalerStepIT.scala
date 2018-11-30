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
class MinMaxScalerStepIT extends GenericPipelineStepTest {

  override def stepName: String = "minmaxscaler"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/minmaxscaler/"

  override def trainingDf: DataFrame = sparkSession.createDataFrame(Seq(
    (0, Vectors.dense(1.0, 0.1, -1.0)),
    (1, Vectors.dense(2.0, 1.1, 1.0)),
    (2, Vectors.dense(3.0, 10.1, 3.0))
  )).toDF("id", "features")


  /* -------------------------------------------------------------
   => Wrong Pipeline construction with invalid params
  ------------------------------------------------------------- */

  s"$stepName with min bigger than max" should "provide an invalid SparkMl pipeline" in
    new ReadDescriptorResource with WithExampleData with WithExecuteStep with WithValidateStep
      with WithFilesystemProperties {
      val filePath = s"${resourcesPath}minmaxscaler-min-bigger-max-v0.json"
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
      assert(execution.failed.get.getMessage.startsWith(ValidationErrorMessages.schemaErrorInit))
      log.info(execution.failed.get.toString)
    }


}
