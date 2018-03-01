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

package com.stratio.sparta.plugin.workflow.output.parquet

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.crossdata.XDSession
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File

@RunWith(classOf[JUnitRunner])
class ParquetOutputStepIT extends TemporalSparkContext
  with ShouldMatchers
  with BeforeAndAfterAll {

  self: FlatSpec =>

  trait CommonValues {
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    import xdSession.implicits._
    val time = DateTime.now.getMillis

    val data = sc.parallelize(
      Seq(Person("Kevin", 18, time), Person("Kira", 21, time), Person("Ariadne", 26, time)))
      .toDS().toDF
    val tempPath = File.makeTemp().name
  }

  trait WithEventData extends CommonValues {

    val properties = Map("path" -> tempPath)
    val parquetOutput = new ParquetOutputStep("parquet-test", sparkSession, properties)
  }

  trait WithoutGranularity extends CommonValues {

    val datePattern = "yyyy/MM/dd"
    val properties = Map("path" -> tempPath, "datePattern" -> datePattern)
    val parquetOutput = new ParquetOutputStep("parquet-test", sparkSession, properties)
    val expectedPath = "/0"
  }

  "ParquetOutputStepIT" should "save a dataFrame" in new WithEventData {
    parquetOutput.save(data, SaveModeEnum.Append, Map(parquetOutput.TableNameKey -> "person"))
    val read = xdSession.read.parquet(s"$tempPath/person")
    read.count should be(3)
    File(tempPath).deleteRecursively()
  }

}

case class Person(name: String, age: Int, minute: Long) extends Serializable