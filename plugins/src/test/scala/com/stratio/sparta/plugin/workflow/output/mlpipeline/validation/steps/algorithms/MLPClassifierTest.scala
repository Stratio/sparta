/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepMissingDefaultTest
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MLPClassifierTest extends GenericPipelineStepMissingDefaultTest {


  override def stepName: String = "mlpclassifier"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/mlpclassifier/"

  override def trainingDf: DataFrame = {
    sparkSession.createDataFrame(Seq(
      (Vectors.dense(0.0, 0.0), 0.0),
      (Vectors.dense(0.0, 1.0), 1.0),
      (Vectors.dense(1.0, 0.0), 1.0),
      (Vectors.dense(1.0, 1.0), 0.0)
    )).toDF("features", "label")

  }
}
