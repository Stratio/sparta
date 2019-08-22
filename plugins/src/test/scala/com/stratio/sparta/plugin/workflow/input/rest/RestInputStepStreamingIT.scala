/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.rest

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, getAllServeEvents, urlEqualTo}
import com.stratio.sparta.core.models.OutputWriterOptions
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
class RestInputStepStreamingIT extends TemporalSparkContext with WireMockSupport with Matchers {

  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
  val stringSchema = StructType(Seq(StructField("output1", StringType)))
  val response = "Everything is fine: blue"

  "RestInputStepStreaming" should "generate a DString with just one row the response as string" in {

    wireMockServer.stubFor(get(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write(response))
          .withStatus(200)))

    val totalEvents = sc.accumulator(0L, "Number of events received")
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val properties =Map("url" -> "http://localhost:20000/test/blue", "requestTimeout" -> "20s", "httpMethod" -> "get",
      "httpOutputField" -> "output1")
    val restInput = new RestInputStepStreaming("testrest1", outputOptions, Option(ssc), sparkSession, properties)
    val generatedRow = new GenericRowWithSchema(Array(response), stringSchema)
    val inputStream = restInput.init
    inputStream.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      totalEvents += streamingEvents
      log.debug(s" TOTAL EVENTS : \t $totalEvents")
      assert(rdd.first() === generatedRow)
      assert(streamingEvents === 1)
    })

    ssc.start()
    ssc.stop()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
  }
}
