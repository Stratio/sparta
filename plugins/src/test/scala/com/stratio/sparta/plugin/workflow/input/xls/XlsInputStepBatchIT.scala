/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.xls

import java.net.URL

import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class XlsInputStepBatchIT extends TemporalSparkContext with Matchers {

  val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
  val resourcePath: URL = getClass().getResource("/test.xls")
  print(resourcePath.getFile())
  "Events in xls/xlsx file" should "match the number of events and the content" in {
    val properties = Map("location" -> s"${resourcePath.getFile}", "useHeader" -> "true", "dataRange"->"A1","sheetName"->"SalesOrders", "dateFormat"->"dd/mm/yyyy")
    val input = new XlsInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()
    count shouldBe 43

  }

}