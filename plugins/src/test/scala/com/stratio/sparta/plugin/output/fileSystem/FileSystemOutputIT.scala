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
package com.stratio.sparta.plugin.output.fileSystem

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.text.SimpleDateFormat


import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.pipeline.output.{OutputFormatEnum, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner
import org.apache.commons.io.FileUtils


@RunWith(classOf[JUnitRunner])
class FileSystemOutputIT extends TemporalSparkContext with Matchers {

  val directory = getClass().getResource("/origin.txt")
  val parentFile = new File(directory.getPath).getParent
  val properties = Map(("path", parentFile + "/testRow"),
    ("fileWithDate", "false"),
    ("outputFormat", "row"),
    ("partitionUntil", "DD"))
  val fields = StructType(StructField("name", StringType, false) ::
    StructField("age", IntegerType, false) ::
    StructField("year", IntegerType, true) :: Nil)
  val tableSchema = Seq(SpartaSchema(Seq("outputName"), "testTable", fields, Option("minute")))
  val fsm = new FileSystemOutput("key", properties, tableSchema)


  "An object of type FileSystemOutput " should "have the same values as the properties Map" in {
    fsm.outputFormat should be(OutputFormatEnum.ROW)
    fsm.fileWithDate should be(false)
    fsm.dateFormat should be(fsm.DateFormat)
  }

  /* DataFrame generator */
  private def dfGen(): DataFrame = {
    val sqlCtx = new SQLContext(sc)
    val dataRDD = sc.parallelize(List(("user1", 23, 1993), ("user2", 26, 1990), ("user3", 21, 1995)))
      .map { case (name, age, year) => Row(name, age, year) }

    sqlCtx.createDataFrame(dataRDD, fields)
  }

  def fileExists(path: String): Boolean = new File(path).exists()

  "Given a DataFrame, a directory" should "be created with the data written inside" in {
    fsm.writeOutput(dfGen())
    fileExists(fsm.path) should equal(true)
  }

  it should "exist with the given path and be deleted" in {
    if (fileExists(fsm.path))
      FileUtils.deleteDirectory(new File(fsm.path))
    fileExists(fsm.path) should equal(false)
  }

  val fsm2 = new FileSystemOutput("key", properties.updated("outputFormat", "json")
    .updated("path", parentFile + "/testJson"), tableSchema)

  "Given another DataFrame, a directory" should "be created with the data inside in JSON format" in {
    fsm2.outputFormat should be(OutputFormatEnum.JSON)
    fsm2.writeOutput(dfGen())
    fileExists(fsm2.path) should equal(true)
  }

  it should "exist with the given path and be deleted" in {
    if (fileExists(fsm2.path))
      FileUtils.deleteDirectory(new File(fsm2.path))
    fileExists(fsm2.path) should equal(false)
  }

  "The path" should "be formatted with the given date until days" in {
    val fsm3 = new FileSystemOutput("key", properties.updated("fileWithDate", "true"), tableSchema)
    val testDate = new Date()
    val partitionFormat = new SimpleDateFormat("YYYY").format(testDate) + "/" +
      new SimpleDateFormat("MM").format(testDate) + "/" + new SimpleDateFormat("DD").format(testDate) + "/" +
      new SimpleDateFormat("HHmmss").format(testDate)

    fsm3.formatPath(testDate) should equal (fsm3.path + "/" + partitionFormat)

  }

  "When 'No partition' option is chosen the date" should "not be parsed with slashes and just be concatenated to " +
    "the path" in {
    val fsm4 = new FileSystemOutput("key", properties.updated("fileWithDate", "true").
      updated ("partitionUntil", "NONE"), tableSchema)
    val testDate = new Date()

    fsm4.formatPath(testDate) should equal (fsm4.path + "/" +  new SimpleDateFormat(fsm4.dateFormat).format(testDate))
  }
}
