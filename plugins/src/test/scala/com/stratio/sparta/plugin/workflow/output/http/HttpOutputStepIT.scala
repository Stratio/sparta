/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.http

import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.enumerators.OutputFormatEnum
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{as, entity, get, parameterSeq, pathPrefix, post, _}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import java.net.ServerSocket

@RunWith(classOf[JUnitRunner])
class HttpOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val port = new ServerSocket(0).getLocalPort

  def internalWebServer() = {
    val routes = pathPrefix("test") {
      get {
        parameterSeq { param =>
          complete(200, "get call ok")
        }
      } ~
        post {
          entity(as[String]) {
            ent =>
              complete(200, "post call ok")
          }
        }
    }
    Http().bindAndHandle(routes, "0.0.0.0", port)
  }

  override protected def beforeAll(): Unit = internalWebServer

  override protected def afterAll(): Unit = system.terminate()

  val url = s"http://0.0.0.0:$port/test"

  val properties = Map(
    "url" -> url,
    "delimiter" -> ",",
    "parameterName" -> "thisIsAKeyName",
    "readTimeOut" -> "5000",
    "outputFormat" -> "ROW",
    "postType" -> "body",
    "connTimeout" -> "6000"
  )

  val fields = StructType(StructField("name", StringType, false) ::
    StructField("age", IntegerType, false) ::
    StructField("year", IntegerType, true) :: Nil)
  val OkHTTPResponse = 200

  "An object of type RestOutput " should "have the same values as the properties Map" in {
    val rest = new HttpOutputStep("key", sparkSession, properties)

    rest.outputFormat should be(OutputFormatEnum.ROW)
    rest.readTimeout should be(5000)
  }

  it should "throw a NoSuchElementException" in {
    val properties2 = properties.updated("postType", "vooooooody")
    a[NoSuchElementException] should be thrownBy {
      new HttpOutputStep("keyName", sparkSession, properties2)
    }
  }

  /* DataFrame generator */
  private def dfGen(): DataFrame = {
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")
    val dataRDD = sc.parallelize(List(("user1", 23, 1993), ("user2", 26, 1990))).map { case (name, age, year) =>
      Row(name, age, year)
    }
    xdSession.createDataFrame(dataRDD, fields)
  }

  /*val restMock1 = new HttpOutputStep("key", sparkSession, properties)
  "Given a DataFrame it" should "be parsed and send through a Raw data POST request" in {
    dfGen().collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock1.sendData(row.mkString(restMock1.delimiter)).code)
    })
  }

  it should "return the same amount of responses as rows in the DataFrame" in {
    val size = dfGen().collect().map(row => restMock1.sendData(row.mkString(restMock1.delimiter)).code).size
    assertResult(dfGen().count())(size)
  }

  val restMock2 = new HttpOutputStep("key", sparkSession, properties.updated("postType", "parameter"))
  it should "be parsed and send as a Get request along with a parameter stated by properties.parameterKey " in {
    dfGen().collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock2.sendData(row.mkString(restMock2.delimiter)).code)
    })
  }

  val restMock3 = new HttpOutputStep("key", sparkSession, properties.updated("outputFormat", "JSON"))
  "Given a DataFrame it" should "be sent as JSON through a Raw data POST request" in {
    dfGen().toJSON.collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock3.sendData(row).code)
    })
  }

  val restMock4 = new HttpOutputStep("key", sparkSession, properties.updated("postType", "parameter").updated("format", "JSON"))
  it should "sent as a GET request along with a parameter stated by properties.parameterKey " in {
    dfGen().toJSON.collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock4.sendData(row).code)
    })
  }*/
}
