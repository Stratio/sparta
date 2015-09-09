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
import org.apache.spark.streaming.dstream.DStream

/**
 * Saves calculated cubes on Redis.
 * The hashkey will have this kind of structure -> A_B_C:A:valueA:B:valueB:C:valueC.It is important to see that
 * values will be part of the key and the objective of it is to perform better searches in the hash.
 * @author anistal
 */
class RedisOutput(keyName: String,
                  properties: Map[String, Serializable],
                  operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                  bcSchema: Option[Seq[TableSchema]])
  extends Output(keyName, properties, operationTypes, bcSchema)
  with AbstractRedisDAO with Serializable {

  override val hostname = properties.getString("hostname", DefaultRedisHostname)

  override val port = properties.getString("port", DefaultRedisPort).toInt

  override val eventTimeFieldName = properties.getString("timestampFieldName", "timestamp")

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Max, WriteOp.Min)

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistMetricOperation(stream)
  }

  /**
   * Saves in Redis' hash cubes values.
   * @param metricOperations that will be saved.
   */
  override def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    metricOperations.toList.groupBy(upMetricOp =>
      AggregateOperations.keyString(upMetricOp._1, upMetricOp._1.timeDimension, fixedDimensions))
      .filter(_._1.size > 0).foreach(collMetricOp => {
      collMetricOp._2.foreach(metricOp => {
        val timeDimension = metricOp._1.timeDimension
        val hashKey = collMetricOp._1 + IdSeparator + metricOp._1.dimensionValues
          .map(dimVal => List(dimVal.getNameDimension, dimVal.value.toString))
          .flatMap(_.toSeq).mkString(IdSeparator) + IdSeparator + timeDimension + IdSeparator + metricOp._1.time

        metricOp._2.foreach(aggregation => {
          val currentOperation = operationTypes.get.get(aggregation._1).get._1
          hset(hashKey, aggregation._1, aggregation._2.get)
        })
      })
    })
  }
}
