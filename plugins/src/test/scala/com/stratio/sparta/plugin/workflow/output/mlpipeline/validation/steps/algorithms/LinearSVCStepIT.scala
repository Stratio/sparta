/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import breeze.optimize.linear.PowerMethod.BDV
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepMLeapErrorsTest
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class LinearSVCStepIT extends GenericPipelineStepMLeapErrorsTest {
  override def stepName: String = "linearsvc"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/linearsvc/"

  override def trainingDf: DataFrame = {
    // Generate noisy input of the form Y = signum(x.dot(weights) + intercept + noise)
    def generateSVMInput(
                          intercept: Double,
                          weights: Array[Double],
                          nPoints: Int,
                          seed: Int): Seq[LabeledPoint] = {
      val rnd = new Random(seed)
      val weightsMat = new BDV(weights)
      val x = Array.fill[Array[Double]](nPoints)(
        Array.fill[Double](weights.length)(rnd.nextDouble() * 2.0 - 1.0))
      val y = x.map { xi =>
        val yD = new BDV(xi).dot(weightsMat) + intercept + 0.01 * rnd.nextGaussian()
        if (yD > 0) 1.0 else 0.0
      }
      y.zip(x).map(p => LabeledPoint(p._1, Vectors.dense(p._2)))
    }

    val A = 0.01
    val B = -1.5
    val C = 1.0
    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(generateSVMInput(A, Array[Double](B, C), 10, 17)))
  }
}
