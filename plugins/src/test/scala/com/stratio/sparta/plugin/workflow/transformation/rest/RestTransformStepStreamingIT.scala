/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.rest

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable


@RunWith(classOf[JUnitRunner])
class RestTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits
  with BeforeAndAfterEach {

  private val port = 20000
  private val hostname = "localhost"
  private val wireMockServer = new WireMockServer(wireMockConfig().port(port))
  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
  private val response = "Everything is fine: no color"

  override def beforeEach() {
    wireMockServer.start()
    WireMock.configureFor(hostname, port)
  }

  override def afterEach {
    wireMockServer.resetAll()
    wireMockServer.stop()
  }



  "A RestTransformStepStreaming" should "append the extracted field over one Dstream" in {

    wireMockServer.stubFor(get(urlEqualTo(s"/test"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: no color"))
          .withStatus(200)))

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()

    val data1 = Seq[Row](
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1)
    )

    dataQueue1 += sc.parallelize(data1)

    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val outputSchema = StructType(schema1 ++ Seq(StructField("output1", StringType)))
    val result = new RestTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "APPEND")
    ).transformWithDiscards(inputData)


    val totalEvents = ssc.sparkContext.longAccumulator("Number of events received")
    val totalDiscardedEvents = ssc.sparkContext.longAccumulator("Number of discarded events received")

    result._1.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents.add(streamingEvents)
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        streamingRegisters.foreach(row => assert(row.schema === outputSchema))
      }
    })

    result._3.get.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      totalDiscardedEvents.add(streamingEvents)
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        streamingRegisters.foreach(row => assert(data1.contains(row)))
      }
    })

    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
    assert(totalDiscardedEvents.value === 0)

  }

}