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

package com.stratio.sparta.plugin.workflow.output.csv

import java.sql.Timestamp
import java.time.Instant

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class CsvOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait CommonValues {
    val tmpPath: String = File.makeTemp().name
    val schema = StructType(Seq(
      StructField("name", StringType),
      StructField("age", IntegerType),
      StructField("minute", LongType)
    ))

    val data =
      sparkSession.createDataFrame(sc.parallelize(Seq(
        Row("Kevin", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Kira", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Ariadne", Random.nextInt, Timestamp.from(Instant.now).getTime)
      )), schema)
  }

  trait WithEventData extends CommonValues {
    val properties = Map("path" -> tmpPath)
    val output = new CsvOutputStep("csv-test", sparkSession, properties)
  }

  it should "save a dataframe " in new WithEventData {
    output.save(data, SaveModeEnum.Append, Map(output.TableNameKey -> "person"))
    val read = sparkSession.read.csv(s"$tmpPath/person.csv")
    read.count should be(3)
    read should be eq data
    File(tmpPath).deleteRecursively
    File("spark-warehouse").deleteRecursively
  }
}