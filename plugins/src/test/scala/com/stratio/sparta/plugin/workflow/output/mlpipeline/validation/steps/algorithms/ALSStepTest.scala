/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import org.apache.spark.sql.DataFrame

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.fommil.netlib.BLAS.{getInstance => blas}
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepMLeapErrorsTest
import org.apache.spark.ml.recommendation.ALS.Rating

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class ALSStepTest extends GenericPipelineStepMLeapErrorsTest {


  override def stepName: String = "als"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/als/"

  override def trainingDf: DataFrame = {

    def genFactors(
                    size: Int,
                    rank: Int,
                    random: Random,
                    a: Float = -1.0f,
                    b: Float = 1.0f): Seq[(Int, Array[Float])] = {
      require(size > 0 && size < Int.MaxValue / 3)
      require(b > a)
      val ids = mutable.Set.empty[Int]
      while (ids.size < size) {
        ids += random.nextInt()
      }
      val width = b - a
      ids.toSeq.sorted.map(id => (id, Array.fill(rank)(a + random.nextFloat() * width)))
    }

    // The assumption of the implicit feedback model is that unobserved ratings are more likely to
    // be negatives.
    val positiveFraction = 0.8
    val negativeFraction = 1.0 - positiveFraction
    val random = new Random(1234)
    val userFactors = genFactors(20, 2, random)
    val itemFactors = genFactors(40, 2, random)
    val data = ArrayBuffer.empty[Rating[Int]]
    for ((userId, userFactor) <- userFactors; (itemId, itemFactor) <- itemFactors) {
      val rating = blas.sdot(2, userFactor, 1, itemFactor, 1)
      val threshold = if (rating > 0) positiveFraction else negativeFraction
      val observed = random.nextDouble() < threshold
      if (observed) {
        val x = random.nextDouble()
        val noise = 0.01 * random.nextGaussian()
        data += Rating(userId, itemId, rating + noise.toFloat)
      }
    }
    sparkSession.createDataFrame(sparkSession.sparkContext.parallelize(data))
  }
}
