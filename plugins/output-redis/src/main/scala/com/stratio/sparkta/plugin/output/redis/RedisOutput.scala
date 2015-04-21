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

/**
 * Saves calculated rollups on Redis.
 *
 * @param properties that has needed properties to start the server.
 * @param schema with the equivalence between an operation id and its WriteOp.
 * @author anistal
 */
class RedisOutput(properties: Map[String, Serializable], schema: Option[Map[String, WriteOp]])
  extends Output(properties, schema) with AbstractRedisDAO with Multiplexer with Serializable with Logging {

  override val hostname = properties.getString("hostname", "localhost")

  override val port = properties.getInt("port", 6379)

  override val eventTimeFieldName = properties.getString("timestampFieldName", "timestamp")

  override def supportedWriteOps: Seq[WriteOp] = Seq(WriteOp.Inc, WriteOp.Max, WriteOp.Min)

  override def multiplexer: Boolean = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override def granularity: String = properties.getString("granularity", "")

  override def timeBucket: String = properties.getString("timestampBucket", "")

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = {
    getStreamsFromOptions(stream, multiplexer, timeBucket)
      .foreachRDD(rdd => rdd.foreachPartition(ops => hx(ops)))
  }

  /**
   * Saves in a Redis' hash rollups values.
   *
   * @param metricOperations that will be saved.
   */
  def hx(metricOperations: Iterator[UpdateMetricOperation]): Unit = {
    metricOperations.toList.groupBy(metricOp => metricOp.keyString).filter(_._1.size > 0).foreach(collMetricOp => {
      collMetricOp._2.map(metricOp => {

        // Step 1) It calculates redis' hash key with this structure -> A:B:C:A:valueA:B:valueB:C:valueC
        // It is important to see that values be part of the key. This is needed to perform searches.
        val hashKey = collMetricOp._1 + IdSeparator + metricOp.rollupKey
          .map(dimVal => List(extractDimensionName(dimVal), dimVal.value.toString))
          .flatMap(_.toSeq).mkString(IdSeparator)

        // Step 2) It calculates aggregations depending of its types and saves the result in the value of the hash.
        metricOp.aggregations.map(aggregation => {
          val currentOperation = schema.get(aggregation._1)

          currentOperation match {
            case WriteOp.Inc => {
              val valueHashOperation: Long  =
                hget(hashKey, aggregation._1).getOrElse("0").toLong

              val valueCurrentOperation: Long =
                aggregation._2.getOrElse(0L).asInstanceOf[Long]

              hset(hashKey, aggregation._1, valueHashOperation + valueCurrentOperation)
            }

            case WriteOp.Max => {
              val valueHashOperation: Double  =
                hget(hashKey, aggregation._1).getOrElse(Double.MinValue.toString).toDouble

              val valueCurrentOperation: Double =
                aggregation._2.getOrElse(Double.MinValue).asInstanceOf[Double]

              if(valueCurrentOperation > valueHashOperation) hset(hashKey, aggregation._1, valueCurrentOperation)
            }

            case WriteOp.Min => {
              val valueHashOperation: Double  =
                hget(hashKey, aggregation._1).getOrElse(Double.MaxValue.toString).toDouble

              val valueCurrentOperation: Double =
                aggregation._2.getOrElse(Double.MaxValue).asInstanceOf[Double]

              if(valueCurrentOperation < valueHashOperation) hset(hashKey, aggregation._1, valueCurrentOperation)
            }
          }
        })
      })
    })
  }
}
