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

package com.stratio.sparkta.testat.outputs

import com.datastax.driver.core.{Cluster, ResultSet, Session}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.testat.SparktaATSuite

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: Cassandra.
 * [Operators]: sum, avg.
 * @author gschiavon anistal
 */

@RunWith(classOf[JUnitRunner])
class ISocketOCassandraIT extends SparktaATSuite {

  val CassandraPort = 9142
  override val policyFile = "policies/ISocket-OCassandra.json"

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in cassandra" in {
      sparktaRunner
      checkData
    }

    def checkData(): Unit = {
      val cluster = Cluster.builder().addContactPoints(Localhost).withPort(CassandraPort).build()
      val session: Session = cluster.connect("sparkta")

      val resultProductA: ResultSet = session.execute("select * from product_minute where product = 'producta'")
      val rowProductA = resultProductA.iterator().next()
      (rowProductA).getDouble("avg_price") should be(750.0d)
      (rowProductA).getDouble("sum_price") should be(6000.0d)

      val resultProductB: ResultSet = session.execute("select * from product_minute where product = 'productb'")
      val rowProductB = resultProductB.iterator().next()
      (rowProductB).getDouble("avg_price") should be(1000.0d)
      (rowProductB).getDouble("sum_price") should be(8000.0d)
    }
  }

  override def extraBefore: Unit = EmbeddedCassandraServerHelper.startEmbeddedCassandra()

  override def extraAfter: Unit = EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
}
