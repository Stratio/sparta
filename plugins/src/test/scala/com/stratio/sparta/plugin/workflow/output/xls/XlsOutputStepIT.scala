/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.xls

import java.sql.Timestamp
import java.time.Instant

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class XlsOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait CommonValues {
    val tmpPath: String = File.makeTemp().name
    val delimiter = ","
    val schema = StructType(Seq(
      StructField("name", StringType),
      StructField("age", StringType),
      StructField("minute", StringType)
    ))
    //We assume headers are strings!
    val data =
      sparkSession.createDataFrame(sc.parallelize(Seq(
        Row("Kevin", Random.nextInt.toString, Timestamp.from(Instant.now).getTime.toString),
        Row("Kira",Random.nextInt.toString, Timestamp.from(Instant.now).getTime.toString),
        Row("Ariadne", Random.nextInt.toString, Timestamp.from(Instant.now).getTime.toString)
      )), schema)
  }
  val dataRange="A1"
  trait WithEventData extends CommonValues {
    val properties = Map("location" -> tmpPath, "useHeader" -> "true", "sheetName"->"Person","dataRange"->dataRange,"inferSchema"->"false")
    val output = new XlsOutputStep("csv-test", sparkSession, properties)
  }

  it should "save a dataframe " in new WithEventData {
    output.save(data, SaveModeEnum.Append, Map(TableNameKey -> "person"))
    //val read = sparkSession.read.csv(s"$tmpPath/person.xls")
    val prop = Map("useHeader" -> "false", "dataRange"->"A1","sheetName"->"Person","inferSchema"->"true")
    val location=s"$tmpPath/person.xls"
    val df = sparkSession.read.format("com.crealytics.spark.excel").options(prop).load(location)
    df.count should be(3)
    File(tmpPath).deleteRecursively
    File("spark-warehouse").deleteRecursively
  }
}