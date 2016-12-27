/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.plugin.output.http

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.pipeline.output.OutputFormatEnum
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class HttpOutputTest extends TemporalSparkContext with Matchers {

  val properties = Map(("url", "https://posttestserver.com/post.php?dir=strat"),
    ("delimiter", ","),
    ("parameterName", "thisIsAKeyName"),
    ("readTimeOut", "5000"),
    ("outputFormat", "ROW"),
    ("postType", "body"))

  val fields = StructType(StructField("name", StringType, false) ::
    StructField("age", IntegerType, false) ::
    StructField("year", IntegerType, true) :: Nil)

  val tableSchema = Seq(SpartaSchema(Seq("outputName"), "testTable", fields, Option("minute")))
  val OkHTTPResponse = 200

  "An object of type RestOutput " should "have the same values as the properties Map" in {
    val rest = new HttpOutput("key", Some(2), properties, tableSchema)

    rest.outputFormat should be(OutputFormatEnum.ROW)
    rest.readTimeout should be(5000)
  }
  it should "throw a NoSuchElementException" in {
    val properties2 = properties.updated("postType", "vooooooody")
    a[NoSuchElementException] should be thrownBy {
      new HttpOutput("keyName", Some(2), properties2, tableSchema)
    }
  }


  /* DataFrame generator */
  private def dfGen(): DataFrame = {
    val sqlCtx = new SQLContext(sc)
    val dataRDD = sc.parallelize(List(("user1", 23, 1993), ("user2", 26, 1990))).map { case (name, age, year) =>
      Row(name, age, year)
    }
    sqlCtx.createDataFrame(dataRDD, fields)
  }

  val restMock1 = new HttpOutput("key", Some(2), properties, tableSchema)
  "Given a DataFrame it" should "be parsed and send through a Raw data POST request" in {

    dfGen().collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock1.sendData(row.mkString(restMock1.delimiter)).code)
    })
  }

  it should "return the same amount of responses as rows in the DataFrame" in {
    val size = dfGen().collect().map(row => restMock1.sendData(row.mkString(restMock1.delimiter)).code).size
    assertResult(dfGen().count())(size)
  }


  val restMock2 = new HttpOutput("key", Some(2), properties.updated("postType", "parameter"), tableSchema)
  it should "be parsed and send as a POST request along with a parameter stated by properties.parameterKey " in {
    dfGen().collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock2.sendData(row.mkString(restMock2.delimiter)).code)
    })
  }

  val restMock3 = new HttpOutput("key", Some(2), properties.updated("outputFormat", "JSON"), tableSchema)
  "Given a DataFrame it" should "be sent as JSON through a Raw data POST request" in {

    dfGen().toJSON.collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock3.sendData(row).code)
    })
  }

  val restMock4 = new HttpOutput("key", Some(2), properties.updated("postType", "parameter")
    .updated("format","JSON"), tableSchema)
  it should "sent as a POST request along with a parameter stated by properties.parameterKey " in {

    dfGen().toJSON.collect().foreach(row => {
      assertResult(OkHTTPResponse)(restMock4.sendData(row).code)
    })
  }
}