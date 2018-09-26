/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.crossdata

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.crossdata.XDSession
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class CrossdataOutputStepIT extends TemporalSparkContext
  with ShouldMatchers
  with BeforeAndAfterAll {


  val hdfsPrefix: String = {
    val config = ConfigFactory.load()
    for {
      hdfsMaster <- Try(config.getString("sparta.hdfs.hdfsMaster"))
      hdfsPort <- Try(config.getString("sparta.hdfs.hdfsPort"))
    } yield s"hdfs://$hdfsMaster:$hdfsPort"

  }.getOrElse("hdfs://localhost:9000")


  "CrossdataOutputStepIT" should "save a dataFrame" in  {

    val sparkSession = XDSession.builder().config(sc.getConf).create("dummyUser")
    val tableName = "person"
    import sparkSession.implicits._

    val crossdataOutput = new CrossdataOutputStep("crossdata-test", sparkSession, Map.empty)
    val crossdataTable =
      sparkSession.sql(s"CREATE TABLE person (name STRING, age INT) USING parquet OPTIONS ( path '$hdfsPrefix/tmp/partitionedTable') PARTITIONED BY (AGE)")

    val newData = sc.parallelize(Seq(Person("Kira", 20), Person("Ariadne", 26))).toDS().toDF

    crossdataOutput.save(
      newData,
      SaveModeEnum.Append,
      Map(crossdataOutput.TableNameKey -> tableName, crossdataOutput.PartitionByKey -> "age")
    )

    val read = sparkSession.table(tableName)
    read.count should be(2)
  }

}

case class Person(name: String, age: Int)