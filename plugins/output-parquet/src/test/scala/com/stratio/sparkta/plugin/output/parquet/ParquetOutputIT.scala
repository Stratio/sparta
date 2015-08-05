/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.output.parquet

import scala.reflect.io.File

import com.github.nscala_time.time.Imports._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.sdk.DateOperations

@RunWith(classOf[JUnitRunner])
class ParquetOutputIT extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  self: FlatSpec =>

  @transient var sc: SparkContext = _

  override def beforeAll {
    Logger.getRootLogger.setLevel(Level.ERROR)
    sc = ParquetOutputIT.getNewLocalSparkContext(1, "test")
  }

  override def afterAll {
    sc.stop()
    System.clearProperty("spark.driver.port")
  }

  trait CommonValues {

    val sqlContext = new SQLContext(sc)

    import sqlContext.implicits._

    val data = sc.parallelize(Seq(Person("Kevin", 18), Person("Kira", 21), Person("Ariadne", 26))).toDF
    val tmpPath: String = File.makeTemp().name
  }

  trait WithEventData extends CommonValues {

    val properties = Map("path" -> tmpPath)
    val output = new ParquetOutput("parquet-test", properties, None, None)
  }

  trait WithWrongOutput extends CommonValues {

    val output = new ParquetOutput("parquet-test", Map(), None, None)
  }

  trait WithDatePattern extends CommonValues {

    val datePattern = "yyyy/MM/dd"
    val properties = Map("path" -> tmpPath, "datePattern" -> datePattern)
    val granularity = "minute"
    val output = new ParquetOutput("parquet-test", properties, None, None)
    val dt = DateTime.now
    val expectedPath = "/" + DateTimeFormat.forPattern(datePattern).print(dt) + "/" + dt.withMillisOfSecond(0)
      .withSecondOfMinute(0).getMillis
  }

  trait WithoutGranularity extends CommonValues {

    val datePattern = "yyyy/MM/dd"
    val properties = Map("path" -> tmpPath, "datePattern" -> datePattern)
    val output = new ParquetOutput("parquet-test", properties, None, None)
    val expectedPath = "/0"
  }

  "ParquetOutputIT" should "save a dataframe" in new WithEventData {
    output.upsert(data, "person", "minute")
    val read = sqlContext.parquetFile(tmpPath).toDF
    read.count should be(3)
    read should be eq (data)
    File(tmpPath).deleteRecursively
  }

  it should "throw an exception when path is not present" in new WithWrongOutput {
    an[Exception] should be thrownBy output.upsert(data, "person", "minute")
  }

  it should "format path with pattern" in new WithDatePattern {
    DateOperations.subPath(granularity, Some(datePattern)) should be(expectedPath)
  }

  it should "format path ignoring pattern" in new WithoutGranularity {
    DateOperations.subPath("", Some(datePattern)) should be(expectedPath)
  }
}

object ParquetOutputIT {

  def getNewLocalSparkContext(numExecutors: Int = 1, title: String): SparkContext =
    new SparkContext(s"local[$numExecutors]", title)
}

case class Person(name: String, age: Int) extends Serializable
