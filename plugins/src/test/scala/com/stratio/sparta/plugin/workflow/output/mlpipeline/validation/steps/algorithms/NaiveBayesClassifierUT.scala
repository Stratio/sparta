/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.{DenseMatrix, Vectors}
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import breeze.linalg.{DenseVector => BDV, Vector => BV}
import breeze.stats.distributions.{Multinomial => BrzMultinomial, RandBasis => BrzRandBasis}

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class NaiveBayesClassifierUT extends GenericPipelineStepTest {


  override def stepName: String = "naivebayesclassifier"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/naivebayesclassifier/"

  override def trainingDf: DataFrame = {

    def calcLabel(p: Double, pi: Array[Double]): Int = {
      var sum = 0.0
      for (j <- 0 until pi.length) {
        sum += pi(j)
        if (p < sum) return j
      }
      -1
    }

    // Generate input of the form Y = (theta * x).argmax()
    def generateNaiveBayesInput(
                                 pi: Array[Double], // 1XC
                                 theta: Array[Array[Double]], // CXD
                                 nPoints: Int,
                                 seed: Int,
                                 sample: Int = 10): Seq[LabeledPoint] = {
      val D = theta(0).length
      val rnd = new Random(seed)
      val _pi = pi.map(math.exp)
      val _theta = theta.map(row => row.map(math.exp))

      implicit val rngForBrzMultinomial = BrzRandBasis.withSeed(seed)
      for (i <- 0 until nPoints) yield {
        val y = calcLabel(rnd.nextDouble(), _pi)
        val xi = {
          val mult = BrzMultinomial(BDV(_theta(y)))
          val emptyMap = (0 until D).map(x => (x, 0.0)).toMap
          val counts = emptyMap ++ mult.sample(sample).groupBy(x => x).map {
            case (index, reps) => (index, reps.size.toDouble)
          }
          counts.toArray.sortBy(_._1).map(_._2)
        }
        LabeledPoint(y, Vectors.dense(xi))
      }
    }

    val nPoints = 1000
    val piArray = Array(0.5, 0.1, 0.4).map(math.log)
    val thetaArray = Array(
      Array(0.70, 0.10, 0.10, 0.10), // label 0
      Array(0.10, 0.70, 0.10, 0.10), // label 1
      Array(0.10, 0.10, 0.70, 0.10) // label 2
    ).map(_.map(math.log))
    val pi = Vectors.dense(piArray)
    val theta = new DenseMatrix(3, 4, thetaArray.flatten, true)

    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(generateNaiveBayesInput(piArray, thetaArray, nPoints, 1244)))
  }
}
