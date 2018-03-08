/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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