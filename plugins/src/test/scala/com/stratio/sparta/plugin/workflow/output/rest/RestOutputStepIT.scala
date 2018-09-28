/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.rest


import com.github.tomakehurst.wiremock.client.WireMock._
import com.stratio.sparta.core.enumerators._
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.plugin.{TemporalSparkContext, WireMockSupport}
import org.apache.spark.sql.types._
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.junit.runner.RunWith
import org.scalatest.ShouldMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RestOutputStepIT extends TemporalSparkContext with WireMockSupport with ShouldMatchers {

  import RestOutputStepIT._

  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

  trait CommonValues {
    val xdSession = sparkSession
    import xdSession.implicits._

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(Item("blue", 12.1),Item("yellow", 12.2))
    val df = sc.parallelize(data1).toDS().toDF
  }

  "A RestOutputStep" should "create a correct httpRequest with the processed body and url" in new CommonValues{

    wireMockServer.stubFor(post(urlEqualTo(s"/test/blue"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: blue"))
          .withStatus(200)))

    wireMockServer.stubFor(post(urlEqualTo(s"/test/yellow"))
      .willReturn(
        aResponse().withBody(Serialization.write("Everything is fine: yellow"))
          .withStatus(200)))

    new RestOutputStep(
      "dummy",
      sparkSession,
      Map("url" -> "http://localhost:20000/test/${color}", "httpMethod" -> "post",
        "httpOutputField" -> "output1", "requestTimeout" -> "20s", "fieldsPreservationPolicy" -> "JUST_EXTRACTED", "HTTP_Body" ->
          """{ "colorResponse": ${color}}""", "HTTP_Body_Format" -> "JSON"
      )).save(df, SaveModeEnum.Append, Map("tableName" -> "aaa"))

    verify(postRequestedFor(urlEqualTo("/test/blue"))
      .withRequestBody(equalToJson("""{"colorResponse": "blue"}""")))

    verify(postRequestedFor(urlEqualTo("/test/yellow"))
      .withRequestBody(equalToJson("""{"colorResponse": "yellow"}""")))
  }
}

object RestOutputStepIT {

  case class Item(color: String, price: Double) extends Serializable

}