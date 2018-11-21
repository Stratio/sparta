/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.{GenericPipelineStepTest, GenericPipelineStepMLeapErrorsTest}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BucketedRandomProjectionLshStepTest extends GenericPipelineStepMLeapErrorsTest {

  override def stepName: String = "bucketed-random-projection-lsh"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/bucketedrandomprojectionlsh/"

  override def trainingDf: DataFrame = sparkSession.createDataFrame(Seq(
    (0, Vectors.dense(1.0, 1.0)),
    (1, Vectors.dense(1.0, -1.0)),
    (2, Vectors.dense(-1.0, -1.0)),
    (3, Vectors.dense(-1.0, 1.0))
  )).toDF("id", "features")

  override def emptyParamsAvailable: Boolean = false
}
