/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.test.service

import java.io.{File, Serializable => JSerializable}
import scala.collection.mutable
import scala.language.postfixOps

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.sdk.Event

/**
 * Created by arincon on 21/04/15.
 */
@RunWith(classOf[JUnitRunner])
class RawDataStorageServiceIT extends WordSpecLike with BeforeAndAfterAll with Matchers {

  val WindowDuration: Long = 200
  val SleepTime: Long = 1000
  val ExpectedResult: Long = 2000
  val path = "testPath"

  override def afterAll {
    val file = new File(path)
    deleteParquetFiles(file)
    sc.stop()
    otherSc.stop()
    Thread.sleep(WindowDuration)

  }

  val myConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName + "" + System.currentTimeMillis())
    .setMaster("local[1]")
  val sc = new SparkContext(myConf)
  lazy val otherSc=new SparkContext(myConf)
  val ssc = new StreamingContext(sc, Duration.apply(WindowDuration))
  val sqlCtx: SQLContext = new SQLContext(sc)

  val events = mutable.Queue[RDD[Event]]()
  val getEvents: Seq[Event] = (0 until 1000) map (i => new Event(Map("key" + i -> System.currentTimeMillis()
    .asInstanceOf[JSerializable]), Some("myRaw event data here")))

  def deleteParquetFiles(file: File): Unit = {
    if (file.exists() && file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(deleteParquetFiles(_))
    file.delete()
  }

  "A raw data storage " should {
    "save events in the configured path" in {
      val dataStorage = new RawDataStorageService(sqlCtx, path)
      val a: DStream[Event] = ssc.queueStream(events)
      dataStorage.save(a)
      ssc.start()

      events += sc.makeRDD(getEvents)
      Thread.sleep(SleepTime)
      events += sc.makeRDD(getEvents)
      Thread.sleep(SleepTime)

      ssc.stop()

      val newSqlCtx = new SQLContext(otherSc)
      val pqFile = newSqlCtx.parquetFile(path)

      pqFile.count() should be(ExpectedResult)
    }
  }
}


