/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

case class TestRow(features: Vector)

@RunWith(classOf[JUnitRunner])
class LDATest extends GenericPipelineStepTest {

  override def stepName: String = "lda"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/lda/"

  override def trainingDf: DataFrame = {

    val avgWC = 1  // average instances of each word in a doc
    val rng = new java.util.Random()
    rng.setSeed(1)
    val rdd = sc.parallelize(1 to 5).map { i =>
      Vectors.dense(Array.fill(20)(rng.nextInt(2 * avgWC).toDouble))
    }.map(v => new TestRow(v))
    sparkSession.createDataFrame(rdd)
  }
}
