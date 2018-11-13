/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import java.io.{Serializable => JSerializable}

import breeze.optimize.linear.PowerMethod.BDV
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.enumerations.MlPipelineSaveMode
import com.stratio.sparta.plugin.workflow.output.mlpipeline.MlPipelineOutputStep
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}
import org.apache.spark.sql.functions.udf

import scala.io.Source
import scala.util.{Failure, Random, Try}

@RunWith(classOf[JUnitRunner])
class LinearSVCStepUT extends GenericPipelineStepTest {
  override def stepName: String = "linearsvc"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/linearsvc/"

  override def trainingDf: DataFrame =  {
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

    // NOTE: Intercept should be small for generating equal 0s and 1s
    val A = 0.01
    val B = -1.5
    val C = 1.0
    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(generateSVMInput(A, Array[Double](B, C), 50, 17)))
  }
}
