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
  val dataAddress="A1:G27"
  trait WithEventData extends CommonValues {
    val properties = Map("location" -> tmpPath, "useHeader" -> "true", "dataAddress"->dataAddress,"inferSchema"->"false")
    val output = new XlsOutputStep("csv-test", sparkSession, properties)
  }

  it should "save a dataframe " in new WithEventData {
    output.save(data, SaveModeEnum.Append, Map(TableNameKey -> "person"))
    val read = sparkSession.read.csv(s"$tmpPath/person.xls")

//    read.count should be(3)
//    read should be eq data
//    File(tmpPath).deleteRecursively
//    File("spark-warehouse").deleteRecursively
  }
}