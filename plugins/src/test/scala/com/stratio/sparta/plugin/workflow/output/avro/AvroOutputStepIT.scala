/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.avro

import java.sql.Timestamp
import java.time.Instant

import com.databricks.spark.avro._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class AvroOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait CommonValues {
    val tmpPath: String = File.makeTemp().name
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")
    val schema = StructType(Seq(
      StructField("name", StringType),
      StructField("age", IntegerType),
      StructField("minute", LongType)
    ))

    val data =
      xdSession.createDataFrame(sc.parallelize(Seq(
        Row("Kevin", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Kira", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Ariadne", Random.nextInt, Timestamp.from(Instant.now).getTime)
      )), schema)
  }

  trait WithEventData extends CommonValues {
    val properties = Map("path" -> tmpPath)
    val output = new AvroOutputStep("avro-test", sparkSession, properties)
  }

  it should "save a dataframe " in new WithEventData {
    output.save(data, SaveModeEnum.Append, Map(output.TableNameKey -> "person"))
    val read = xdSession.read.avro(s"$tmpPath/person")
    read.count should be(3)
    read should be eq data
    File(tmpPath).deleteRecursively
    File("spark-warehouse").deleteRecursively
  }
}