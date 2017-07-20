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
package com.stratio.sparta.plugin.output.filesystem

import java.io.File

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.output.fileSystem.FileSystemOutput
import com.stratio.sparta.sdk.pipeline.output.{Output, OutputFormatEnum, SaveModeEnum}
import org.apache.commons.io.FileUtils
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class FileSystemOutputIT extends TemporalSparkContext with Matchers {

  val directory = getClass().getResource("/origin.txt")
  val parentFile = new File(directory.getPath).getParent
  val properties = Map(("path", parentFile + "/testRow"), ("outputFormat", "row"))
  val fields = StructType(StructField("name", StringType, false) ::
    StructField("age", IntegerType, false) ::
    StructField("year", IntegerType, true) :: Nil)
  val fsm = new FileSystemOutput("key", sparkSession, properties)


  "An object of type FileSystemOutput " should "have the same values as the properties Map" in {
    fsm.outputFormat should be(OutputFormatEnum.ROW)
  }

  /* DataFrame generator */
  private def dfGen(): DataFrame = {
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    val dataRDD = sc.parallelize(List(("user1", 23, 1993), ("user2", 26, 1990), ("user3", 21, 1995)))
      .map { case (name, age, year) => Row(name, age, year) }

    xdSession.createDataFrame(dataRDD, fields)
  }

  def fileExists(path: String): Boolean = new File(path).exists()

  "Given a DataFrame, a directory" should "be created with the data written inside" in {
    fsm.save(dfGen(), SaveModeEnum.Append, Map(Output.TableNameKey -> "test"))
    fileExists(fsm.path.get) should equal(true)
  }

  it should "exist with the given path and be deleted" in {
    if (fileExists(fsm.path.get))
      FileUtils.deleteDirectory(new File(fsm.path.get))
    fileExists(fsm.path.get) should equal(false)
  }

  val fsm2 = new FileSystemOutput("key", sparkSession, properties.updated("outputFormat", "json")
    .updated("path", parentFile + "/testJson"))

  "Given another DataFrame, a directory" should "be created with the data inside in JSON format" in {
    fsm2.outputFormat should be(OutputFormatEnum.JSON)
    fsm2.save(dfGen(), SaveModeEnum.Append, Map(Output.TableNameKey -> "test"))
    fileExists(fsm2.path.get) should equal(true)
  }

  it should "exist with the given path and be deleted" in {
    if (fileExists(s"${fsm2.path.get}/test"))
      FileUtils.deleteDirectory(new File(s"${fsm2.path.get}/test"))
    fileExists(s"${fsm2.path.get}/test") should equal(false)
  }
}
