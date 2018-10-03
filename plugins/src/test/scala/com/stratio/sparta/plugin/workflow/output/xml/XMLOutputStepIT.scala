/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.xml

import java.sql.Timestamp
import java.time.Instant

import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.reflect.io.File
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class XMLOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  trait CommonValues {
    val tmpPath: String = File.makeTemp().name
    val schema = StructType(Seq(
      StructField("name", StringType),
      StructField("age", LongType),
      StructField("minute", LongType)
    ))

    val data =
      sparkSession.createDataFrame(sc.parallelize(Seq(
        Row("Kevin", Random.nextLong, Timestamp.from(Instant.now).getTime),
        Row("Kira", Random.nextLong, Timestamp.from(Instant.now).getTime),
        Row("Ariadne", Random.nextLong, Timestamp.from(Instant.now).getTime)
      )), schema)
  }

  trait WithEventData extends CommonValues {
    val properties = Map("path" -> tmpPath, "rowTag" -> "person")
    val output = new XMLOutputStep("xml-test", sparkSession, properties)
  }

  trait WithoutMandatoryOption extends CommonValues {
    val properties = Map("path" -> tmpPath)
    val output = new XMLOutputStep("xml-test", sparkSession, properties)
  }

  it should "save a dataframe" in new WithEventData {
    output.save(data, SaveModeEnum.Append, Map(TableNameKey -> "person"))
    val read = sparkSession.read.format("xml").options(properties).load(s"$tmpPath/person.xml")
    read.schema should be(StructType(schema.fields.sortBy(_.name)))
    read.count should be(3)
    read should be eq data
    File(tmpPath).deleteRecursively
    File("spark-warehouse").deleteRecursively
  }

  it should "raise an exception if rowTag is not defined" in new WithoutMandatoryOption {
    an[Exception] should be thrownBy
      output.save(data, SaveModeEnum.Append, Map(TableNameKey -> "person"))
  }
}