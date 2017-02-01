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
package com.stratio.sparta.plugin.output.parquet

import java.sql.Timestamp

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File

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

    val time = new Timestamp(DateTime.now.getMillis)

    val data =
      sc.parallelize(Seq(Person("Kevin", 18, time), Person("Kira", 21, time), Person("Ariadne", 26, time))).toDF

    val tmpPath: String = File.makeTemp().name
  }

  trait WithEventData extends CommonValues {

    val properties = Map("path" -> tmpPath)
    val output = new ParquetOutput("parquet-test", properties, Seq())
  }

  trait WithWrongOutput extends CommonValues {

    val output = new ParquetOutput("parquet-test", Map(), Seq())
  }

  trait WithoutGranularity extends CommonValues {

    val datePattern = "yyyy/MM/dd"
    val properties = Map("path" -> tmpPath, "datePattern" -> datePattern)
    val output = new ParquetOutput("parquet-test", properties, Seq())
    val expectedPath = "/0"
  }

  "ParquetOutputIT" should "save a dataframe" in new WithEventData {
    output.save(data, SaveModeEnum.Append, Map(Output.TimeDimensionKey -> "minute", Output.TableNameKey -> "person"))
    val read = sqlContext.read.parquet(tmpPath).toDF
    read.count should be(3)
    read should be eq (data)
    File(tmpPath).deleteRecursively
  }

  it should "throw an exception when path is not present" in new WithWrongOutput {
    an[Exception] should be thrownBy output
      .save(data, SaveModeEnum.Append, Map(Output.TimeDimensionKey -> "minute", Output.TableNameKey -> "person"))
  }
}

object ParquetOutputIT {

  def getNewLocalSparkContext(numExecutors: Int = 1, title: String): SparkContext = {
    val conf = new SparkConf().setMaster(s"local[$numExecutors]").setAppName(title)
    SparkContext.getOrCreate(conf)
  }
}

case class Person(name: String, age: Int, minute: Timestamp) extends Serializable
