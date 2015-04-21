/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
/**
* Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.output.redis

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk.{Event, WriteOp}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import redis.embedded.RedisServer

import scala.collection.mutable.SynchronizedQueue

/**
* This spec describes the behaviour of Redis' output.
 *
 * @author anistal
*/
class RedisOutputSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  val DefaultRedisTestingPort: Int = 63790
  var redisServer : RedisServer = _
  var ssc: StreamingContext = _

  "Behaviour of RedisOutput"

  "A RedisOutput" should "insert an element in memory correctly" ignore  {
    redisServer = new RedisServer(63790)
    redisServer.start

    val properties: Map[String, JSerializable] =
      Map("hostname" -> "localhost", "port" -> 63790)
    val schema: Option[Map[String, WriteOp]] = Option(Map("count-operator" -> WriteOp.Set))
    val redisOutput: RedisOutput = new RedisOutput(properties, schema)

    val sc = new SparkContext("local[1]", "RedisOutputTest")

    ssc = new StreamingContext(sc, Seconds(1))

    val rddQueue = new SynchronizedQueue[RDD[String]]()
    val inputStream = ssc.queueStream(rddQueue)
    val result = inputStream.map(value => new Event(Map("some:key" -> value)))

//    redisOutput.persist(Seq(result))
    ssc.start()

    rddQueue += ssc.sparkContext.makeRDD(Seq("someValue"))

    Thread.sleep(3000)
//    val expectedValue = redisOutput.get("some:key")
//    expectedValue should equal(Some("someValue"))
  }

  override def afterEach(): Unit = {
    if(redisServer != null) {
      redisServer.stop()
    }
    if(ssc != null) {
      ssc.stop()
    }
  }
}
