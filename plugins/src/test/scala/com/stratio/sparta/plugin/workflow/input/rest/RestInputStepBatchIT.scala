/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.rest

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.plugin.{TemporalSparkContext, WireMockSupport}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class RestInputStepBatchIT extends TemporalSparkContext with WireMockSupport with Matchers {

  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

  "RestInputStepBatch" should "generate a RDD with just one row the response as string" in {

    val response = "Everything is fine: blue"
    wireMockServer.stubFor(get(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write(response))
          .withStatus(200)))

    val totalEvents = sc.accumulator(0L, "Number of events received")
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val properties =Map("url" -> "http://localhost:20000/test/blue", "requestTimeout" -> "20s", "httpMethod" -> "get",
      "httpOutputField" -> "output1")
    val restInput = new RestInputStepBatch("testrest1", outputOptions, None, sparkSession, properties)
    val input = restInput.initWithSchema()._1
    val stringSchema = StructType(Seq(StructField("output1", StringType)))
    val generatedRow = new GenericRowWithSchema(Array(response), stringSchema)

    input.ds.foreachPartition { partition =>
      totalEvents += partition.size
    }

    assert(totalEvents.value == 1)
    val firstEl = input.ds.first()
    assert( firstEl.getString(0).contains(generatedRow.getString(0)))
  }

  "RestInputStepBatch" should "generate a RDD if is whitelisted the response" in {

    val response = "Everything is ....error BUT whitelisted!!!"
    wireMockServer.stubFor(get(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write(response))
          .withStatus(505)))

    val totalEvents = sc.accumulator(0L, "Number of events received")
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val properties =Map("url" -> "http://localhost:20000/test/blue", "requestTimeout" -> "20s", "httpMethod" -> "get",
      "httpOutputField" -> "output1", "statusCodeWhiteList" -> "505" )
    val restInput = new RestInputStepBatch("testrest1", outputOptions, None, sparkSession, properties)
    val input = restInput.initWithSchema()._1
    val stringSchema = StructType(Seq(StructField("output1", StringType)))
    val generatedRow = new GenericRowWithSchema(Array(response), stringSchema)

    input.ds.foreachPartition { partition =>
      totalEvents += partition.size
    }

    assert(totalEvents.value == 1)
    val firstEl = input.ds.first()
    assert( firstEl.getString(0).contains(generatedRow.getString(0)))
  }

  "RestInputStepBatch" should "raise an error" in {

    val response = "Everything is ....error!!!"
    wireMockServer.stubFor(get(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write(response))
          .withStatus(505)))

    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val properties =Map("url" -> "http://localhost:20000/test/blue", "requestTimeout" -> "20s", "httpMethod" -> "get",
      "httpOutputField" -> "output1")
    intercept[Exception]{
      new RestInputStepBatch("testrest1", outputOptions, None, sparkSession, properties).initWithSchema()._1.ds.first()
    }
  }
}
