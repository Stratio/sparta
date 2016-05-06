/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.testat.outputs

import java.{util, lang}

import com.datastax.driver.core.{Cluster, ResultSet, Session}
import com.stratio.sparta.testat.SpartaATSuite
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._

import scala.reflect.ClassTag

/**
  * Acceptance test:
  * [Input]: Socket.
  * [Output]: Cassandra.
  * [Operators]: avg, count, firsValue, fullText, lastValue, max,
  * median, min, range, stddev, sum, variance.
  */
@RunWith(classOf[JUnitRunner])
class ISocketOCassandraOperatorsIT extends SpartaATSuite {

  System.setProperty("jna.nosys","true")

  override val policyFile = "policies/ISocket-OCassandra-operators.json"
  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-operators.csv").getPath

  var cluster: Cluster = _
  var session: Session = _
  val CassandraPort = 9142
  val NumEventsExpected: Int = 8

  "Sparta" should {
    "starts and executes a policy that reads from a socket and writes in cassandra" in {
      spartaRunner
      checkData("testCubeWithTime_v1")
      checkData("testCubeWithoutTime_v1")
    }

    def checkData(tableName: String): Unit = {

      session = cluster.connect("sparta")

      val resultProductA: ResultSet = session.execute(s"select * from $tableName where product = 'producta'")
      val rowProductA = resultProductA.iterator().next()

      rowProductA.getDouble("avg_price") should be(639.0d)
      rowProductA.getDouble("sum_price") should be(5112.0d)
      rowProductA.getInt("count_price") should be(NumEventsExpected)
      rowProductA.getString("first_price") should be("10")
      rowProductA.getString("last_price") should be("600")
      rowProductA.getDouble("max_price") should be(1002.0d)
      rowProductA.getDouble("min_price") should be(10.0d)
      rowProductA.getString("fulltext_price") should be("10 500 1000 500 1000 500 1002 600")
      rowProductA.getDouble("stddev_price") should be(347.9605889013459d)
      rowProductA.getDouble("variance_price") should be(121076.57142857143d)
      rowProductA.getDouble("range_price") should be(992.0d)
      rowProductA.getDouble("variance_price") should be(121076.57142857143d)
      val counts = mapAsScalaMap(rowProductA.getMap("entitycount_text", classOf[String], classOf[java.lang.Integer]))
      counts should be(Map("hola" -> new lang.Long(16), "holo" -> new lang.Long(8)))
      rowProductA.getInt("totalentity_text") should be(24)

      val resultProductB: ResultSet = session.execute(s"select * from $tableName where product = 'productb'")
      val rowProductB = resultProductB.iterator().next()

      rowProductB.getDouble("avg_price") should be(758.25d)
      rowProductB.getDouble("sum_price") should be(6066.0d)
      rowProductB.getInt("count_price") should be(NumEventsExpected)
      rowProductB.getString("first_price") should be("15")
      rowProductB.getString("last_price") should be("50")
      rowProductB.getDouble("max_price") should be(1001.0d)
      rowProductB.getDouble("min_price") should be(15.0d)
      rowProductB.getString("fulltext_price") should be("15 1000 1000 1000 1000 1000 1001 50")
      rowProductB.getDouble("stddev_price") should be(448.04041590655d)
      rowProductB.getDouble("variance_price") should be(200740.2142857143d)
      rowProductB.getDouble("range_price") should be(986.0d)
      val counts2 = mapAsScalaMap(rowProductB.getMap("entitycount_text", classOf[String], classOf[java.lang.Integer]))
      counts2 should be(Map("hola" -> new java.lang.Long(16), "holo" -> new java.lang.Long(8)))
      rowProductB.getInt("totalentity_text") should be(24)

    }
  }

  override def extraBefore: Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra()
    cluster = Cluster.builder().addContactPoints(Localhost).withPort(CassandraPort).build()
  }

  override def extraAfter: Unit = {
    session.close()
    cluster.close()
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    deletePath(s"$CheckpointPath/${"ATSocketCassandra".toLowerCase}")
  }
}

