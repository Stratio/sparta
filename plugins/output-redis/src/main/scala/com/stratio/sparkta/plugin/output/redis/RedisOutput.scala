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

import com.stratio.sparkta.plugin.output.redis.dao.AbstractRedisDAO
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.Logging
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try

class RedisOutput(properties: Map[String, Serializable], schema: Option[Map[String, WriteOp]])
  extends Output(properties, schema) with AbstractRedisDAO with Multiplexer with Serializable with Logging {

  override val dbName = properties.getString("dbName", "sparkta")

  override val hostname = properties.getString("hostname", "localhost")

  override val port = properties.getInt("port", 6379)

  override def supportedWriteOps: Seq[WriteOp] = Seq()

  override def multiplexer: Boolean = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override def granularity: String = properties.getString("granularity", "")

  override def timeBucket: String = properties.getString("timestampBucket", "")

  override def getStreamsFromOptions(stream: DStream[UpdateMetricOperation],
                                     multiplexer: Boolean,
                                     fixedBucket: String): DStream[UpdateMetricOperation] = {
    multiplexer match {
      case false => stream
      case _ => fixedBucket match {
        case "" => Multiplexer.multiplexStream(stream)
        case _ => Multiplexer.multiplexStream[fixedBucket.type](stream, fixedBucket)
      }
    }
  }

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = {
    getStreamsFromOptions(stream, multiplexer, timeBucket)
      .foreachRDD(rdd => rdd.foreachPartition(ops => upsert(ops)))
  }

  override def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    streams.foreach(persist)
  }

  def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {
    metricOperations.toList.groupBy(metricOp => metricOp.keyString).filter(_._1.size > 0).foreach(collMetricOp => {
      collMetricOp._2.map(metricOp => {
        def extractDimensionName(dimensionValue: DimensionValue): String =
          dimensionValue.bucketType match {
            case Bucketer.identity | Bucketer.fulltext => dimensionValue.dimension.name
            case _ => dimensionValue.bucketType.id
          }

        val hashKey = collMetricOp._1 + idSeparator + metricOp.rollupKey.filter(rollup =>
          (rollup.bucketType.id != Bucketer.fulltext.id) && (rollup.bucketType.id != timeBucket))
          .map(dimVal =>
            List(extractDimensionName(dimVal), dimVal.value.toString))
          .flatMap(_.toSeq).mkString(idSeparator)

        metricOp.rollupKey.map(dimensionValue => {
          client.hset(hashKey, extractDimensionName(dimensionValue), dimensionValue.value)
        })

        metricOp.aggregations.map(aggregation => {
          client.hset(hashKey, aggregation._1, aggregation._2.get)
        })
      })
    })
  }
}
