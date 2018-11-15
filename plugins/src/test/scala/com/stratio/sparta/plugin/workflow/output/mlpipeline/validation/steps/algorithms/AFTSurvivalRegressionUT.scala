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
class DecisionTreeRegressorUT extends GenericPipelineStepTest {


  override def stepName: String = "decisiontreeregressor"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/decisiontreeregressor/"

  override def trainingDf: DataFrame = {
    def generateCategoricalDataPoints(): Array[LabeledPoint] = {
      val arr = new Array[LabeledPoint](1000)
      for (i <- 0 until 1000) {
        if (i < 600) {
          arr(i) = new LabeledPoint(1.0, Vectors.dense(0.0, 1.0))
        } else {
          arr(i) = new LabeledPoint(0.0, Vectors.dense(1.0, 0.0))
        }
      }
      arr
    }

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(generateCategoricalDataPoints()))
  }
}
