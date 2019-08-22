/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.cassandra

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.output.cassandra.CassandraOutputStep
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql.crossdata.XDSession
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class CassandraInputStepBatchIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

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

  "Cassandra" should "read a dataFrame" in {

    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    import com.datastax.spark.connector.cql.CassandraConnector
    import xdSession.implicits._

    val outputProperties = Map(
      "nodes" -> JsoneyString(
        s"""
           |[{
           |  "node":"$cassandraHost"
           |}]
      """.stripMargin),
      "cluster" -> "spartacluster",
      "keyspace" -> "spartakeyspace"
    )
    val inputProperties = Map("nodes" -> JsoneyString(
      s"""
         |[{
         |  "node":"$cassandraHost"
         |}]
      """.stripMargin),
      "cluster" -> "spartacluster",
      "table" -> "sparta",
      "keyspace" -> "spartakeyspace"
    )
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val cassandraOutput = new CassandraOutputStep("cassandra.out", sparkSession, outputProperties)
    val cassandraInput = new CassandraInputStepBatch(
      "cassandra-out",
      outputOptions,
      Option(ssc),
      sparkSession,
      inputProperties
    )
    val data = sc.parallelize(
      Seq(
        Person("Marcos", 18), Person("Juan", 21), Person("Jose", 26)
      )
    ).toDS().toDF

    xdSession.conf.set("spark.cassandra.connection.host", cassandraOutput.connectionHosts)

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

    cassandraInput.initWithSchema()._1.ds.count() should be(3)
  }
}

case class Person(name: String, age: Int) extends Serializable