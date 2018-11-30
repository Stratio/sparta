/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import scala.util.Random
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.{DenseVector, Vectors}
import org.apache.spark.sql.{DataFrame, Row}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.mllib.random._

@RunWith(classOf[JUnitRunner])
class GeneralizedLinearRegressionIT extends GenericPipelineStepTest {


  override def stepName: String = "generalizedlinearregression"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/generalizedlinearregression/"

  override def trainingDf: DataFrame = {

    /**
      * dot(x, y)
      */
    def dot(x: DenseVector, y: DenseVector): Double = {
      val n = x.size
      org.netlib.blas.Ddot.ddot(n, x.values, 0, 1, y.values, 0, 1);
    }

    def generateGeneralizedLinearRegressionInput(
                                                  intercept: Double,
                                                  coefficients: Array[Double],
                                                  xMean: Array[Double],
                                                  xVariance: Array[Double],
                                                  nPoints: Int,
                                                  seed: Int,
                                                  noiseLevel: Double,
                                                  family: String,
                                                  link: String): Seq[LabeledPoint] = {

      val rnd = new Random(seed)

      def rndElement(i: Int) = {
        (rnd.nextDouble() - 0.5) * math.sqrt(12.0 * xVariance(i)) + xMean(i)
      }

      val (generator, mean) = family match {
        case "gaussian" => (new StandardNormalGenerator, 0.0)
        case "poisson" => (new PoissonGenerator(1.0), 1.0)
        case "gamma" => (new GammaGenerator(1.0, 1.0), 1.0)
      }
      generator.setSeed(seed)

      (0 until nPoints).map { _ =>
        val features = Vectors.dense(coefficients.indices.map(rndElement).toArray)
        val eta = dot(Vectors.dense(coefficients).asInstanceOf[DenseVector], features.asInstanceOf[DenseVector]) + intercept
        val mu = link match {
          case "identity" => eta
          case "log" => math.exp(eta)
          case "sqrt" => math.pow(eta, 2.0)
          case "inverse" => 1.0 / eta
        }
        val label = mu + noiseLevel * (generator.nextValue() - mean)
        // Return LabeledPoints with DenseVector
        LabeledPoint(label, features)
      }
    }


    sparkSession.createDataFrame(
      sparkSession.sparkContext.parallelize(generateGeneralizedLinearRegressionInput(
        intercept = 2.5, coefficients = Array(2.2, 0.6), xMean = Array(2.9, 10.5),
        xVariance = Array(0.7, 1.2), nPoints = 10000, 1234, noiseLevel = 0.01,
        family = "gaussian", link = "identity")))

  }

}
