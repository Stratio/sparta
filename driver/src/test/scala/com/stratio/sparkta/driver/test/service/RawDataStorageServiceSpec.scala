package com.stratio.sparkta.driver.test.service

import scala.collection.mutable
import scala.language.postfixOps

import org.apache.spark.rdd.RDD

import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.{DStream, InputDStream}

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SQLContext
import java.io.{Serializable => JSerializable, File}
import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.sdk.Event

import org.scalatest._

/**
 * Created by arincon on 21/04/15.
 */
class RawDataStorageServiceSpec extends WordSpecLike  with BeforeAndAfterAll with Matchers{
  val path="testPath"

  override def afterAll {
    val file = new File(path)
    deleteParquetFiles(file)
  }

  val myConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName + "" + System.currentTimeMillis())
    .setMaster("local[2]")
  val sc = new SparkContext(myConf)
  val ssc= new StreamingContext(sc, Duration.apply(100))
  val sqlCtx: SQLContext = new SQLContext(sc)


  val events = mutable.Queue[RDD[Event]]()
  val getEvents: Seq[Event] = (0 until 1000) map (i => new Event(Map("key" + i -> System.currentTimeMillis()
    .asInstanceOf[JSerializable]), Some("myRaw event data here")))

  def deleteParquetFiles(file: File): Unit = {
    if(file.exists() && file.isDirectory)
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
      Thread.sleep(500)
      events += sc.makeRDD(getEvents)
      Thread.sleep(800)

      ssc.stop()

      val newSqlCtx = new SQLContext(new SparkContext(myConf))
      val pqFile = newSqlCtx.parquetFile(path)

      pqFile.count() should be(2000)
    }


  }
}


