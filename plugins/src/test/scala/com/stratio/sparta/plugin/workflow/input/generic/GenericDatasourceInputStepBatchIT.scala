/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.generic

import java.net.URL

import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.util.matching.Regex

@RunWith(classOf[JUnitRunner])
class GenericDatasourceInputStepBatchIT extends TemporalSparkContext with Matchers {

  val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
  val resourcePath: URL = getClass.getResource("/test.parquet")

  "GenericInputStep" should "allow to read data from generic input datasource" in {
    val properties = Map(
      "datasource" -> "parquet",
      "path" -> s"file://${resourcePath.getFile}"
    )

    val input = new GenericDatasourceInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    input.validate().valid shouldBe true

    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("jc"), outputSchema))
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 1

    rdd.ds.collect().toSeq should be(dataOut)
  }

  it should "not validate an empty input datasource" in {
    val properties = Map(
      "path" -> s"file://${resourcePath.getFile}"
    )

    val input = new GenericDatasourceInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val validation = input.validate()

    validation.valid shouldBe false
    validation.messages should have length 1
  }

  it should "not validate a wrong table name" in {
    val properties = Map(
      "datasource" -> "parquet",
      "path" -> s"file://${resourcePath.getFile}"
    )

    val input = new GenericDatasourceInputStepBatch("-*-*/-*/*", outputOptions, Option(ssc), sparkSession, properties)
    val validation = input.validate()

    validation.valid shouldBe false
    validation.messages should have length 1
    validation.messages.head.message should fullyMatch regex new Regex("The step name [^\\s]+ is not valid")
  }

}