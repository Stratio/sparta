/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.mlModel

import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.json.RowJsonHelper
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RowJsonHelperTest extends TemporalSparkContext with Matchers /*with DistributedMonadImplicits*/ {

  "A RowJsonHelper" should "serialize rows containing strings" in {
    val inputRow = new GenericRowWithSchema(Array("val"), StructType(Seq(StructField("key", StringType))) )
    val expectedString = RowJsonHelper.toJSON(inputRow, Map.empty, useTypedConverters = true)
    expectedString shouldBe """{"key":"val"}"""
  }

  it should "serialize rows containing VectorUDT" in {

    val model = {
      // Prepare training documents from a list of (id, text, label) tuples.
      val training = sparkSession.createDataFrame(Seq(
        (0L, "a b c d e spark", 1.0),
        (1L, "b d", 0.0)
      )).toDF("id", "text", "label")

      // Configure an ML pipeline, which consists of three stages: tokenizer, hashingTF, and lr.
      val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
      val hashingTF = new HashingTF().setNumFeatures(10).setInputCol(tokenizer.getOutputCol).setOutputCol("features")
      val lr = new LogisticRegression().setMaxIter(1)
      val pipeline = new Pipeline().setStages(Array(tokenizer, hashingTF, lr))

      // Fit the pipeline to training documents.
      pipeline.fit(training)
    }


    // Prepare test documents, which are unlabeled (id, text) tuples.
    val test = sparkSession.createDataFrame(Seq(
      (2L, "spark i j k")
    )).toDF("id", "text")

    // Make predictions on test documents.
    val rowWithVector = model.transform(test).select("id", "text", "probability", "prediction").collect().head

    val expectedString = RowJsonHelper.toJSON(rowWithVector, Map.empty, useTypedConverters = true)
    println(RowJsonHelper.toJSON(rowWithVector, Map.empty, useTypedConverters = true))

    expectedString shouldBe """{"id":2,"text":"spark i j k","probability":{"type":1,"values":[0.3302384506733431,0.6697615493266569]},"prediction":1.0}"""

  }
}
