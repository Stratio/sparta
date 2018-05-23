/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import java.sql.Timestamp
import java.util.UUID

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import com.stratio.sparta.sdk.workflow.step.OutputStep._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest._

import scala.reflect.io.File


class FileOutputStepIT extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  self: FlatSpec =>

  @transient var sc: SparkContext = _

  override def beforeAll {
    Logger.getRootLogger.setLevel(Level.ERROR)
    sc = FileOutputStepIT.getNewLocalSparkContext(1, "test")
  }

  override def afterAll {
    sc.stop()
    System.clearProperty("spark.driver.port")
  }

  trait CommonValues {

    val sqlContext = XDSession.builder()
      .config(sc.getConf)
      .create("dummyUser")

    import sqlContext.implicits._

    val time = new Timestamp(DateTime.now.getMillis)

    val data =
      sc.parallelize(Seq(Person("Kevin", 18, time), Person("Kira", 21, time), Person("Ariadne", 26, time))).toDF

    val tmpPath: String = s"/tmp/sparta-test/${UUID.randomUUID().toString}"
  }

  trait WithEventData extends CommonValues {
    val properties = Map("path" -> tmpPath, "createDifferentFiles" -> "false")
    val output = new FileCustomOutputStep("file-test", sqlContext, properties)
  }

  "FileOutputIT" should "save a dataframe" in new WithEventData {
    output.save(data, SaveModeEnum.Append, Map())

    val source = new java.io.File(tmpPath).listFiles()
    val read = sqlContext.read.json(tmpPath).toDF
    read.count shouldBe(3)
    File("/tmp/sparta-test").deleteRecursively
  }
}

object FileOutputStepIT {

  def getNewLocalSparkContext(numExecutors: Int = 1, title: String): SparkContext = {
    val conf = new SparkConf().setMaster(s"local[$numExecutors]").setAppName(title)
    SparkContext.getOrCreate(conf)
  }
}

case class Person(name: String, age: Int, minute: Timestamp) extends Serializable
