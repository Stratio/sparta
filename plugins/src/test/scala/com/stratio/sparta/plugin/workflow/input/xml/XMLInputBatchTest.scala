/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.xml

import java.net.URL

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class XMLInputBatchTest extends TemporalSparkContext with Matchers {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName")
  val resourcePath: URL = getClass().getResource("/test.xml")

  "XMLInputStep" should "match the number of events and the content" in {
    val properties = Map("path" -> s"file://${resourcePath.getFile}", "rowTag" -> "book")
    val input = new XMLInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val expectedSchema = StructType(Seq(StructField("_id",StringType,true),
      StructField("author",StringType,true),
      StructField("description",StringType,true),
      StructField("genre",StringType,true),
      StructField("price",DoubleType,true),
      StructField("publish_date",StringType,true),
      StructField("title",StringType,true)
    ))
    val rdd = input.initWithSchema()._1
    val schema = rdd.ds.first().schema
    val count = rdd.ds.count()

    count shouldBe 12

    schema should be(expectedSchema)
  }
}
