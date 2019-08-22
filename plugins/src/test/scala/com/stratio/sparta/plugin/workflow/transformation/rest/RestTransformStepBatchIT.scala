/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.rest



import com.github.tomakehurst.wiremock.client.WireMock._
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.{SaveModeEnum, WhenError, WhenFieldError, WhenRowError}
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.plugin.{TemporalSparkContext, WireMockSupport}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class RestTransformStepBatchIT extends TemporalSparkContext with
  WireMockSupport with Matchers with DistributedMonadImplicits
  with BeforeAndAfterEach {

  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
  private val response = "Everything is fine: no color"


  "A RestTransformStepBatch" should "discard timeout if the request is not completed within the timeout" in {

    wireMockServer.stubFor(get(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: blue")).withFixedDelay(2000)
          .withStatus(200)))


    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq[Row](
      new GenericRowWithSchema(Array("blue", 12.1), schema1)
    )
    val inputRdd1 = sc.parallelize(data1,1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val (result, _, resultDiscard, _) = new RestTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(WhenError.Error, WhenRowError.RowDiscard, WhenFieldError.FieldError),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test/${color}", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "1 s", "fieldsPreservationPolicy" -> "JUST_EXTRACTED",
        "akkaHttpOptions" -> s"""
                                |[{
                                |  "akkaHttpOptionsKey":"akka.http.host-connection-pool.max-connections",
                                |  "akkaHttpOptionsValue": "1"
                                |},
                                |{
                                |  "akkaHttpOptionsKey":"akka.http.host-connection-pool.max-open-requests",
                                |  "akkaHttpOptionsValue": "16"
                                |},
                                |{
                                |  "akkaHttpOptionsKey":"akka.http.host-connection-pool.max-retries",
                                |  "akkaHttpOptionsValue": "0"
                                |},
                                |{
                                |  "akkaHttpOptionsKey":"akka.stream.materializer.debug-logging",
                                |  "akkaHttpOptionsValue": "on"
                                |}
                                |]
                               """.stripMargin)
    ).transformWithDiscards(inputData)

    result.ds.collect() should have length 0
    resultDiscard should not be empty
    resultDiscard.get.ds.collect() should have length 1

  }


  "A RestTransformStepBatch" should "append the extracted field" in {
    wireMockServer.stubFor(get(urlEqualTo(s"/test"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: no color"))
          .withStatus(200)))

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq[Row](
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1)
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val outputSchema = StructType(schema1 ++ Seq(StructField("output1", StringType)))
    val result = new RestTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "APPEND")
    ).transformWithDiscards(inputData)._1

    val batchRegisters = result.ds.collect()
    assert(batchRegisters.length == 2)
    assert(batchRegisters.head.schema == outputSchema)
    assert(batchRegisters.forall(row => {
      row.size == outputSchema.fields.length && row.toSeq.last.asInstanceOf[String].contains(response)
    }))
  }

  "A RestTransformStepBatch" should "keep only extracted field" in {

    wireMockServer.stubFor(get(urlEqualTo(s"/test"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: no color"))
          .withStatus(200)))

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq[Row](
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1)
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val outputSchema = StructType(Seq(StructField("output1", StringType)))
    val result = new RestTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1

    val batchRegisters = result.ds.collect()
    assert(batchRegisters.length == 2)
    assert(batchRegisters.head.schema == outputSchema)
    assert(batchRegisters.forall(row => {
      row.size == outputSchema.fields.length && row.toSeq.last.asInstanceOf[String].contains(response)
    }))
  }


  "A RestTransformStepBatch" should "discard an error" in {

    wireMockServer.stubFor(get(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: blue"))
          .withStatus(200)))

    wireMockServer.stubFor(get(urlEqualTo("/test/red"))
      .willReturn(
        aResponse().withBody(Serialization.write("Error!"))
          .withStatus(500)))

    wireMockServer.stubFor(get(urlEqualTo(s"/test/yellow"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: yellow"))
          .withStatus(200)))


    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq[Row](
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1),
      new GenericRowWithSchema(Array("yellow", 12.2), schema1)
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val (result, _, resultDiscard, _) = new RestTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(WhenError.Error, WhenRowError.RowDiscard, WhenFieldError.FieldError),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test/${color}", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)
    val batchRegisters = result.ds.collect()
    assert(batchRegisters.length == 2)

    resultDiscard should not be empty
    resultDiscard.get.ds.collect() should have length 1
  }

  "A RestTransformStepBatch" should "raise an error if no column name is found" in {

    wireMockServer.stubFor(get(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: blue"))
          .withStatus(200)))

    wireMockServer.stubFor(get(urlEqualTo("/test/red"))
      .willReturn(
        aResponse().withBody(Serialization.write("Error!"))
          .withStatus(500)))

    wireMockServer.stubFor(get(urlEqualTo(s"/test/yellow"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: yellow"))
          .withStatus(200)))


    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq[Row](
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1),
      new GenericRowWithSchema(Array("yellow", 12.2), schema1)
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val (result, _, resultDiscard, _) = new RestTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(WhenError.Error, WhenRowError.RowDiscard, WhenFieldError.FieldError),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test/${colour}", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)
    val batchRegisters = result.ds.collect()
    batchRegisters shouldBe empty
  }

  "A RestTransformStepBatch" should "raise an error wrt request" in {

    val path = s"/test"
    wireMockServer.stubFor(get(urlEqualTo(path))
      .willReturn(
        aResponse().withBody(Serialization.write("Error!"))
          .withStatus(500)))

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq[Row](
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1)
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    intercept[Exception](new RestTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(WhenError.Discard, WhenRowError.RowError, WhenFieldError.FieldError),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1
    )
  }

  "A RestTransformStepBatch" should "do nothing when we have an empty RDD[Row] as input" in {

    val path = s"/test"
    wireMockServer.stubFor(get(urlEqualTo(path))
      .willReturn(
        aResponse().withBody(Serialization.write("Error!"))
          .withStatus(200)))

    val data1 = Seq.empty[Row]
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val (result, _, resultDiscard, _) = new RestTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(WhenError.Discard, WhenRowError.RowError, WhenFieldError.FieldError),
      Option(ssc),
      sparkSession,
      Map("url" -> "http://localhost:20000/test", "httpMethod" -> "get",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)

    assert(result.ds.isEmpty())
    // We also check that the server has not received any request during this last test
    assert(getAllServeEvents.size() == 0)

  }

}