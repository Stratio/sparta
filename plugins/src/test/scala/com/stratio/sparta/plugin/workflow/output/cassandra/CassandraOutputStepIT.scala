/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.cassandra

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.workflow.step.OutputStep._
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import org.apache.spark.sql.crossdata.XDSession

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class CassandraOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  private lazy val config = ConfigFactory.load()
  val cassandraHost = Try(config.getString("cassandra.hosts.0")) match {
    case Success(configHost) =>
      log.info(s"Cassandra from config: $configHost")
      s"$configHost"
    case Failure(_) =>
      log.info(s"Cassandra from default")
      "localhost"
  }

  trait CommonValues {
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    import xdSession.implicits._

    val time = DateTime.now.getMillis
    val data = sc.parallelize(Seq(Person("Marcos", 18), Person("Juan", 21), Person("Jose", 26))).toDS().toDF
  }

  trait WithEventData extends CommonValues {
    val properties = Map("nodes" -> JsoneyString(s"""
         |[{
         |  "node":"$cassandraHost"
         |}]
      """.stripMargin),
      "cluster" -> "spartacluster",
      "keyspace" -> "spartakeyspace"
    )

    val cassandraOutput = new CassandraOutputStep("cassandra.out", sparkSession, properties)
  }

  "Cassandra" should "save a dataFrame and read the same information" in new WithEventData {

    xdSession.conf.set("spark.cassandra.connection.host", cassandraOutput.connectionHosts)

    import com.datastax.spark.connector.cql.CassandraConnector

    CassandraConnector(new SparkConf().setAll(xdSession.conf.getAll)).withSessionDo { session =>
      session.execute(
        "CREATE KEYSPACE IF NOT EXISTS spartakeyspace " +
          "WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }"
      )
      session.execute(
        "DROP TABLE IF EXISTS spartakeyspace.sparta"
      )
      session.execute(
        "CREATE TABLE IF NOT EXISTS spartakeyspace.sparta (name text PRIMARY KEY, age int)"
      )
    }

    cassandraOutput.save(data, SaveModeEnum.Append, Map(TableNameKey -> "sparta"))

    val loadData = xdSession.read.format(cassandraOutput.CassandraClass)
      .options(Map("cluster" -> "spartacluster", "keyspace" -> "spartakeyspace", "table" -> "sparta"))
      .load()

    loadData.count() should be (3)
  }
}

case class Person(name: String, age: Int) extends Serializable