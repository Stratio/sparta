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
package com.stratio.sparta.plugin.output.avro

import java.sql.Timestamp
import java.time.Instant

import com.databricks.spark.avro._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.output.parquet.Person
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import org.apache.spark.sql.SQLContext
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File
import scala.util.Random


@RunWith(classOf[JUnitRunner])
class AvroOutputIT extends TemporalSparkContext with Matchers {

  trait CommonValues {
    val tmpPath: String = File.makeTemp().name
    val sqlContext = new SQLContext(sc)

    import sqlContext.implicits._

    val data =
      sc.parallelize(
        Seq(
          Person("Kevin", Random.nextInt, Timestamp.from(Instant.now)),
          Person("Kira", Random.nextInt, Timestamp.from(Instant.now)),
          Person("Ariadne", Random.nextInt, Timestamp.from(Instant.now))
        )).toDF
  }

  trait WithEventData extends CommonValues {
    val properties = Map("path" -> tmpPath)
    val output = new AvroOutput("avro-test", None, properties, Seq())
  }

  trait NonePath extends CommonValues {
    val output = new AvroOutput("avro-test", None, Map.empty, Seq())
  }

  trait EmptyPath extends CommonValues {
    val properties = Map("path" -> "    ")
    val output = new AvroOutput("avro-test", None, Map.empty, Seq())
  }


  "AvroOutput" should  "throw an exception when path is not present" in new NonePath {
      an[Exception] should be thrownBy output
        .save(data, SaveModeEnum.Append, Map(Output.TimeDimensionKey -> "minute", Output.TableNameKey -> "person"))
    }

  it should "throw an exception when empty path " in new EmptyPath {
      an[Exception] should be thrownBy output
        .save(data, SaveModeEnum.Append, Map(Output.TimeDimensionKey -> "minute", Output.TableNameKey -> "person"))
    }

  it should "save a dataframe with timedimension" in new WithEventData {
      output.save(data, SaveModeEnum.Append, Map(Output.TimeDimensionKey -> "minute", Output.TableNameKey -> "person"))
      val read = sqlContext.read.avro(tmpPath).toDF
      read.count should be(3)
      read should be eq data
      File(tmpPath).deleteRecursively
    }

}

case class Person(name: String, age: Int, minute: Timestamp) extends Serializable
