/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.steps.algorithms

import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.GenericPipelineStepTest
import org.apache.spark.ml.attribute.{AttributeGroup, NominalAttribute, NumericAttribute}
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.ml.tree.impl.TreeTests
import org.apache.spark.rdd.RDD

@RunWith(classOf[JUnitRunner])
class DecisionTreeClassifierIT extends GenericPipelineStepTest {


  override def stepName: String = "decisiontreeclassifier"

  override def resourcesPath: String = "/mlpipeline/singlesteps/algorithms/decisiontreeclassifier/"

  override def trainingDf: DataFrame = {

    val sparkValToImportImplicits = sparkSession
    import sparkValToImportImplicits.implicits._

    // Copied form Spark Tests
    def setMetadata(data: RDD[LabeledPoint],
                    categoricalFeatures: Map[Int, Int],
                    numClasses: Int): DataFrame = {

      val df = data.toDF()
      val numFeatures = data.first().features.size
      val featuresAttributes = Range(0, numFeatures).map { feature =>
        if (categoricalFeatures.contains(feature)) {
          NominalAttribute.defaultAttr.withIndex(feature).withNumValues(categoricalFeatures(feature))
        } else {
          NumericAttribute.defaultAttr.withIndex(feature)
        }
      }.toArray
      val featuresMetadata = new AttributeGroup("features", featuresAttributes).toMetadata()
      val labelAttribute = if (numClasses == 0) {
        NumericAttribute.defaultAttr.withName("label")
      } else {
        NominalAttribute.defaultAttr.withName("label").withNumValues(numClasses)
      }
      val labelMetadata = labelAttribute.toMetadata()
      df.select(df("features").as("features", featuresMetadata),
        df("label").as("label", labelMetadata))
    }

    val arr = Array(
      LabeledPoint(0.0, Vectors.dense(0.0)),
      LabeledPoint(0.0, Vectors.dense(0.0)),
      LabeledPoint(0.0, Vectors.dense(0.0)),
      LabeledPoint(1.0, Vectors.dense(0.0)),
      LabeledPoint(0.0, Vectors.dense(1.0)),
      LabeledPoint(0.0, Vectors.dense(1.0)),
      LabeledPoint(0.0, Vectors.dense(1.0)),
      LabeledPoint(0.0, Vectors.dense(1.0)),
      LabeledPoint(0.0, Vectors.dense(2.0)),
      LabeledPoint(0.0, Vectors.dense(2.0)),
      LabeledPoint(0.0, Vectors.dense(2.0)),
      LabeledPoint(1.0, Vectors.dense(2.0)))
    val data = sc.parallelize(arr)
    val df = setMetadata(data, Map(0 -> 3), 2)
    df
  }

}
