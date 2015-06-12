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

package com.stratio.sparkta.driver.test.service

import java.io.File

import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.sdk.Event
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.TestSuiteBase
import org.apache.spark.{SparkContext, SparkException}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author arincon
 *         This test sucks and I know it...
 */
@RunWith(classOf[JUnitRunner])
class RawDataStorageIT extends TestSuiteBase {

  val ExpectedResult: Long = 9
  val path = "testPath"
  val PreserverOrder: Boolean = true
  val SleepTime: Long = 2000

  test("Write and read events in/from parquet") {

    configureContext
    val rds = new RawDataStorageService(sparktaTestSQLContext, path, "day")
    //This is not a test, This is a way to feed parquet
    intercept[SparkException] {
      //ArrayStoreException:BoxedUnit
      testOperation(
        rawDataInput,
        rds.save,
        expectedOutput,
        PreserverOrder
      )
    }

    //We need to wait for the async write operation
    Thread.sleep(SleepTime)

    try {
      val pqFile = sparktaTestSQLContext.parquetFile(path + rds.timeSuffix)
      assert(pqFile.count() == ExpectedResult)
    } finally {
      //We can't use the before method
      val file = new File(path + rds.timeSuffix)
      deleteParquetFiles(file)
    }
  }

  private def configureContext: Unit = {
    conf.set("spark.driver.allowMultipleContexts", "true")
    conf.setMaster("local[2]")
    conf.setAppName("1" + randomSuffix)
    conf.set("spark.ui.port", "666" + randomSuffix)
  }

  private def randomSuffix: String = {
    val currentStringTime = System.currentTimeMillis().toString
    currentStringTime.substring(currentStringTime.length - 3, currentStringTime.length - 1)
  }

  private def sparktaTestSQLContext: SQLContext = {
    configureContext
    val sc = new SparkContext(conf)
    new SQLContext(sc)
  }

  private def rawDataInput: Seq[Seq[Event]] =
    Seq(Seq(
      Event(Map("eventKey" -> "value1")),
      Event(Map("eventKey" -> "value2")),
      Event(Map("eventKey" -> "value3"))),
      Seq(
        Event(Map("eventKey" -> "value1")),
        Event(Map("eventKey" -> "value2")),
        Event(Map("eventKey" -> "value3")))
    )

  private def expectedOutput(): Seq[Seq[Event]] =
    Seq(Seq(
      Event(Map("eventKey" -> "value1")),
      Event(Map("eventKey" -> "value2")),
      Event(Map("eventKey" -> "value3"))),
      Seq(
        Event(Map("eventKey" -> "value1")),
        Event(Map("eventKey" -> "value2")),
        Event(Map("eventKey" -> "value3")))
    )

  private def deleteParquetFiles(file: File): Unit = {
    if (file.exists() && file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(deleteParquetFiles(_))
    file.delete()
  }
}
