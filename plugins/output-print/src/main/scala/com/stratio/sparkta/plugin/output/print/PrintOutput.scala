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

package com.stratio.sparkta.plugin.output.print

import java.io.{Serializable => JSerializable}

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql._
import org.apache.spark.{Logging, SparkContext}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

/**
 * This output prints all AggregateOperations or DataFrames information on screen. Very useful to debug.
 * @param keyName
 * @param properties
 * @param operationTypes
 * @param bcSchema
 */
class PrintOutput(keyName: String,
                  properties: Map[String, JSerializable],
                  operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                  bcSchema: Option[Seq[TableSchema]])
  extends Output(keyName, properties, operationTypes, bcSchema) with Logging {

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    if (log.isDebugEnabled) {
      log.debug(s"> Table name       : $tableName")
      log.debug(s"> Data frame count : " + dataFrame.count())
      log.debug(s"> DataFrame schema")
      dataFrame.printSchema()
    }

    dataFrame.foreach(frame => log.info(frame.toString()))
  }

  override def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    metricOperations.foreach(metricOp =>
      log.info(AggregateOperations.toString(metricOp._1, metricOp._2, metricOp._1.timeDimension, fixedDimensions)))
  }
}
