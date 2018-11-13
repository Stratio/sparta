/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.preprocessing

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BinarizerStepUT extends GenericPipelineStepTest {

  override def stepName: String = "chi-sq-selector"

  override def resourcesPath: String = "/mlpipeline/singlesteps/preprocessing/chisqselector/"

  override def trainingDf: DataFrame = sparkSession.createDataFrame(Seq(
    (1.0, Vectors.dense(Array(0.0, 9.0, 8.0, 5.0, 6.0, 4.0)), Vectors.dense(0.0)),
    (2.0, Vectors.dense(Array(8.0, 9.0, 6.0, 5.0, 4.0, 4.0)), Vectors.dense(8.0)),
    (2.0, Vectors.dense(Array(8.0, 9.0, 6.0, 4.0, 0.0, 0.0)), Vectors.dense(8.0))
  )).toDF("label", "features", "topFeature")

}
