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

package com.stratio.sparta.plugin.workflow.output.json

import java.io.File

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.commons.io.FileUtils
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  trait CommonValues {
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")
    val dataRDD = sc.parallelize(List(
      ("user1", 23, 1993),
      ("user2", 26, 1990),
      ("user3", 21, 1995)
    )).map { case (name, age, year) => Row(name, age, year) }
    val inputDataFrame = xdSession.createDataFrame(dataRDD, fields)
  }

  private def fileExists(path: String): Boolean = new File(path).exists()

  val directory = getClass().getResource("/origin.txt")
  val parentFile = new File(directory.getPath).getParent
  val tempPath = parentFile + "/testJson"
  val properties = Map(("path", tempPath))
  val fields = StructType(
    StructField("name", StringType, false) ::
      StructField("age", IntegerType, false) ::
      StructField("year", IntegerType, true) :: Nil)
  val jsonStep = new JsonOutputStep("key", sparkSession, properties)

  "Given a DataFrame, a directory" should "be created with the data inside in JSON format" in new CommonValues {
    jsonStep.save(inputDataFrame, SaveModeEnum.Append, Map(jsonStep.TableNameKey -> "test"))
    fileExists(jsonStep.path) should equal(true)
    val read = xdSession.read.json(s"$tempPath/test")
    read.count should be(3)
  }

  it should "exist with the given path and be deleted" in {
    if (fileExists(s"${jsonStep.path}/test"))
      FileUtils.deleteDirectory(new File(s"${jsonStep.path}/test"))
    fileExists(s"${jsonStep.path}/test") should equal(false)
  }
}