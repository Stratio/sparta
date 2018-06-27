/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.parquet

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.enumerators.SaveModeEnum
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