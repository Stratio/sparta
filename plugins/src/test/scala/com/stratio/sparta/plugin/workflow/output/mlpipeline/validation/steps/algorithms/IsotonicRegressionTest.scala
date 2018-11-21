/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IsotonicRegressionTest extends GenericPipelineStepTest {


  override def stepName: String = "isotonicregression"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/isotonicregression/"

  override def trainingDf: DataFrame = {

    def generateIsotonicInput(labels: Seq[Double]) = {
      labels.zipWithIndex.map { case (label, i) => Row.fromSeq(Seq(label, i.toDouble, 1.0)) }
    }

    sparkSession.createDataFrame(
      sparkSession.sparkContext.parallelize(
        generateIsotonicInput(Seq(1, 2, 3, 1, 6, 17, 16, 17, 18))),
      StructType(
        List(
          StructField("label", DoubleType, true),
          StructField("features", DoubleType, true),
          StructField("weight", DoubleType, true)
        )
      ))

  }

}
