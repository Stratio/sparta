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
import java.util.UUID

import com.stratio.sparkta.plugin.output.redis.dao.AbstractRedisDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.dstream.DStream

/**
  * Saves calculated cubes on Redis.
  * The hashKey will have this kind of structure -> A:valueA:B:valueB:C:valueC.It is important to see that
  * values will be part of the key and the objective of it is to perform better searches in the hash.
  * @author anistal
  */
class RedisOutput[T](keyName: String,
                  version: Option[Int],
                  properties: Map[String, Serializable],
                  operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                  bcSchema: Option[Seq[TableSchema]])
  extends Output[T](keyName, version, properties, operationTypes, bcSchema)
  with AbstractRedisDAO with Serializable {

  override val hostname = properties.getString("hostname", DefaultRedisHostname)

  override val port = properties.getString("port", DefaultRedisPort).toInt

  override def doPersist(stream: DStream[(T, MeasuresValues)]): Unit = {
    persistMetricOperation(stream)
  }

  /**
    * Saves in Redis' hash cubes values.
    * @param metricOperations that will be saved.
    */
  override def upsert(metricOperations: Iterator[(T, MeasuresValues)]): Unit = {
    val dimAggGrouped = filterNonEmptyMetricOperations(groupMetricOperationsByHashKey(metricOperations.toList))

    for {
      (hashKey, dimensionsMeasures) <- dimAggGrouped
      (dimensionsTime, measures) <- dimensionsMeasures
      (aggregationName, aggregationValue) <- measures.values
      currentOperation = operationTypes.get.get(aggregationName).get._1
    } yield {
      if (supportedWriteOps.contains(currentOperation)) hset(hashKey, aggregationName, aggregationValue.get)
      else log.warn(s"Operation $currentOperation not supported in the cube with name $aggregationName")
    }
  }

  def groupMetricOperationsByHashKey(metricOp: List[(T, MeasuresValues)])
  : Map[String, List[(T, MeasuresValues)]] = {
    metricOp.groupBy { case (dimensionsTime, _) => getHashKey(dimensionsTime) }
  }

  def filterNonEmptyMetricOperations(metricOp : Map[String, List[(T, MeasuresValues)]])
  : Map[String, List[(T, MeasuresValues)]] = {
    metricOp.filter { case (key, _) => key.nonEmpty }
  }

  def getHashKey(dimensionsTime: T): String = {

    dimensionsTime match {
      case dimensionsTimeWithTime: DimensionValuesTime => {
        if (dimensionsTimeWithTime.dimensionValues.nonEmpty) {
          val hasValues = dimensionsTimeWithTime.dimensionValues.exists(dimValue => dimValue.value.toString.nonEmpty)
          if (hasValues) {
            dimensionsTimeWithTime.dimensionValues.map(dimVal =>
              List(dimVal.getNameDimension, dimVal.value.toString)).flatMap(_.toSeq).mkString(IdSeparator) +
              IdSeparator + dimensionsTimeWithTime.timeDimension + IdSeparator + dimensionsTimeWithTime.time
          } else ""
        } else {
          dimensionsTimeWithTime.timeDimension + IdSeparator + dimensionsTimeWithTime.time
        }
      }
      case dimensionValuesWithoutTime: DimensionValuesWithoutTime => {
        if (dimensionValuesWithoutTime.dimensionValues.nonEmpty) {
          val hasValues =
            dimensionValuesWithoutTime.dimensionValues.exists(dimValue => dimValue.value.toString.nonEmpty)
          if (hasValues) {
            dimensionValuesWithoutTime.dimensionValues.map(dimVal =>
              List(dimVal.getNameDimension, dimVal.value.toString)).flatMap(_.toSeq).mkString(IdSeparator)
          } else ""
        } else {
          UUID.randomUUID().toString
        }
      }
    }
  }
}
