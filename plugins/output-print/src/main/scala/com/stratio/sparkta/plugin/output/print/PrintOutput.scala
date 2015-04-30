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
package com.stratio.sparkta.plugin.output.print

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql._
import ValidatingPropertyMap._
import scala.util.Try

class PrintOutput(keyName : String,
                  properties: Map[String, JSerializable],
                  sqlContext : SQLContext,
                  operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                  bcSchema : Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sqlContext, operationTypes, bcSchema) {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min, WriteOp.Avg, WriteOp.Median,
  WriteOp.Stddev, WriteOp.Variance, WriteOp.AccSet)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val timeBucket = properties.getString("dateBucket", None)

  override val granularity = properties.getString("granularity", None)

  override val autoCalculateId = Try(properties.getString("autoCalculateId").toBoolean).getOrElse(false)

  override def upsert(dataFrame : DataFrame, tableName : String): Unit = {
    println("Table name : " + tableName)
    dataFrame.printSchema()
    dataFrame.foreach(println)
    println("Count : "  +  dataFrame.count())
  }

  override def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {
    metricOperations.foreach(metricOp => println(metricOp.toString))
  }
}
