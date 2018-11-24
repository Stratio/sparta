/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.attribute.{AttributeGroup, NominalAttribute, NumericAttribute}
import org.apache.spark.ml.feature.{LabeledPoint, StringIndexer, VectorIndexer}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GBTClassifierTest extends GenericPipelineStepTest {

  override def stepName: String = "gbtclassifier"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/gbtclassifier/"

  override def trainingDf: DataFrame = {
    val sparkValToImportImplicits = sparkSession
    import sparkValToImportImplicits.implicits._

    def generateOrderedLabeledPoints(numFeatures: Int, numInstances: Int): Array[LabeledPoint] = {
      val arr = new Array[LabeledPoint](numInstances)
      for (i <- 0 until numInstances) {
        val label = if (i < numInstances / 10) {
          0.0
        } else if (i < numInstances / 2) {
          1.0
        } else if (i < numInstances * 0.9) {
          0.0
        } else {
          1.0
        }
        val features = Array.fill[Double](numFeatures)(i.toDouble)
        arr(i) = LabeledPoint(label, Vectors.dense(features))
      }
      arr
    }

    val numFeatures = 2
    val numInstances = 2
    sc.parallelize(generateOrderedLabeledPoints(numFeatures, numInstances)).toDF()
  }

}
