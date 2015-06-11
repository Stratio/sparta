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

package com.stratio.sparkta.plugin.output.redis

import java.io.Serializable

import com.stratio.sparkta.plugin.output.redis.dao.AbstractRedisDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try

/**
 * Saves calculated rollups on Redis.
 *
 */
class RedisOutput(keyName: String,
                  properties: Map[String, Serializable],
                  @transient sparkContext: SparkContext,
                  operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                  bcSchema: Option[Broadcast[Seq[TableSchema]]],
                  timeName: String)
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema, timeName)
  with AbstractRedisDAO with Serializable {

  override val hostname = properties.getString("hostname", DefaultRedisHostname)

  override val port = properties.getInt("port", DefaultRedisPort)

  override val eventTimeFieldName = properties.getString("timestampFieldName", "timestamp")

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Max, WriteOp.Min)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val fixedPrecisions: Array[String] = properties.getString("fixedBuckets", None) match {
    case None => Array()
    case Some(fixPrecisions) => fixPrecisions.split(fieldsSeparator)
  }

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistMetricOperation(stream)
  }

  /**
   * Saves in a Redis' hash rollups values.
   *
   * @param metricOperations that will be saved.
   */
  override def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    metricOperations.toSeq.groupBy(upMetricOp => AggregateOperations.keyString(upMetricOp._1))
      .filter(_._1.size > 0).map(collMetricOp => {
      collMetricOp._2.map(metricOp => {

        // Step 1) It calculates redis' hash key with this structure -> A_B_C:A:valueA:B:valueB:C:valueC
        // It is important to see that values be part of the key. This is needed to perform searches.
        val hashKey = collMetricOp._1 + IdSeparator + metricOp._1.dimensionValues
          .map(dimVal => List(dimVal.getNameDimension, dimVal.value.toString))
          .flatMap(_.toSeq).mkString(IdSeparator)

        // Step 2) It calculates aggregations depending of its types and saves the result in the value of the hash.
        metricOp._2.map(aggregation => {
          val currentOperation = operationTypes.get.value.get(aggregation._1).get._1

          currentOperation match {
            case WriteOp.Inc => {
              val valueHashOperation: Long = hget(hashKey, aggregation._1).getOrElse("0").toLong
              val valueCurrentOperation: Long = aggregation._2.getOrElse(0L).asInstanceOf[Long]
              hset(hashKey, aggregation._1, valueHashOperation + valueCurrentOperation)
            }
            case WriteOp.IncBig => {
              val valueHashOperation: Long = hget(hashKey, aggregation._1).getOrElse("0").toLong
              val valueCurrentOperation: Long = aggregation._2 match {
                case None => 0L
                case Some(value) => value.asInstanceOf[BigDecimal].toLong
              }
              hset(hashKey, aggregation._1, valueHashOperation + valueCurrentOperation)
            }
            case WriteOp.Max => {
              val valueHashOperation: Double =
                hget(hashKey, aggregation._1).getOrElse(scala.Double.MinValue.toString).toDouble
              val valueCurrentOperation: Double = aggregation._2.getOrElse(scala.Double.MinValue).asInstanceOf[Double]
              if (valueCurrentOperation > valueHashOperation) hset(hashKey, aggregation._1, valueCurrentOperation)
            }
            case WriteOp.Min => {
              val valueHashOperation: Double =
                hget(hashKey, aggregation._1).getOrElse(scala.Double.MaxValue.toString).toDouble
              val valueCurrentOperation: Double = aggregation._2.getOrElse(scala.Double.MaxValue).asInstanceOf[Double]
              if (valueCurrentOperation < valueHashOperation) hset(hashKey, aggregation._1, valueCurrentOperation)
            }
          }
        })
      })
    })
  }
}
