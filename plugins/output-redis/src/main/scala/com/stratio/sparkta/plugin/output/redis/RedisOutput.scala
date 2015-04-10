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

import java.io.Serializable
import com.stratio.sparkta.sdk.ValidatingPropertyMap._

import com.stratio.sparkta.plugin.output.redis.dao.AbstractRedisDAO
import com.stratio.sparkta.sdk.WriteOp._
import com.stratio.sparkta.sdk.{Event, UpdateMetricOperation, Multiplexer, Output}
import org.apache.spark.Logging
import org.apache.spark.streaming.dstream.DStream


class RedisOutput(properties: Map[String, Serializable], schema : Map[String, WriteOp])
  extends Output(properties, Some(schema)) with AbstractRedisDAO with Multiplexer with Serializable with Logging {

  override val dbName = properties.getString("dbName", "sparkta")

  override val hostname = properties.getString("hostname", "localhost")

  override val port = properties.getInt("port", 6379)

  override def supportedWriteOps: Seq[WriteOp] = ???

  override def persist(stream: DStream[UpdateMetricOperation]) : Unit = {

  }

  def save(stream: Iterator[Event]) : Unit = {
    stream.foreach(event => insert(event))
  }
  def save(event: Event) : Unit = {
    insert(event)
  }

  def get(key: String) : Option[String] = {
    client.get(key)
  }

  override def multiplexer: Boolean = ???

  override def granularity: String = ???

  override def timeBucket: String = ???

  override def getStreamsFromOptions(stream: DStream[UpdateMetricOperation], multiplexer: Boolean, fixedBucket: String): DStream[UpdateMetricOperation] = ???

}
