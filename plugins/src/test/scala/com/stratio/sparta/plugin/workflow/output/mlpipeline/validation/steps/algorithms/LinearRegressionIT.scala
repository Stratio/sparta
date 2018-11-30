/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class LinearRegressionIT extends GenericPipelineStepTest {

  override def stepName: String = "linearregression"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/linearregression/"

  override def trainingDf: DataFrame = {

    val data = (0.0 to 9.0 by 1) // create a collection of Doubles
      .map(n => (n, n)) // make it pairs
      .map { case (label, features) =>
      LabeledPoint(label, Vectors.dense(features)) // create labeled points of dense vectors
    }

    sparkSession.createDataFrame(data).toDF()
  }
}