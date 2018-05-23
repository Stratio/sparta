/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.parquet

import java.net.URL

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.models.OutputOptions
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParquetInputStepBatchIT extends TemporalSparkContext with Matchers {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName")
  val resourcePath: URL = getClass().getResource("/test.parquet")

  "Events in parquet file" should "match the number of events and the content" in {
    val properties = Map("path" -> s"file://${resourcePath.getFile}")
    val input = new ParquetInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("jc"), outputSchema))
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 1

    rdd.ds.collect().toSeq should be(dataOut)
  }

}