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

package com.stratio.sparkta.plugin.output.solr

import java.io.{Serializable => JSerializable}
import scala.util.Try

import com.lucidworks.spark.SolrRelation
import org.apache.solr.client.solrj.SolrClient
import org.apache.spark.sql.DataFrame
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class SolrOutput[T](keyName: String,
                 version: Option[Int],
                 properties: Map[String, Serializable],
                 operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                 bcSchema: Option[Seq[TableSchema]])
  extends Output[T](keyName, version, properties, operationTypes, bcSchema) with SolrDAO {

  override val supportedWriteOps = Seq(WriteOp.FullText, WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Range,
    WriteOp.Max, WriteOp.Min, WriteOp.Avg, WriteOp.Median, WriteOp.Variance, WriteOp.Stddev)

  override val isAutoCalculateId = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(true)

  override val idField = properties.getString("idField", None)

  override val connection = properties.getString("connection", s"$DefaultNode:$DefaultPort")

  override val createSchema = Try(properties.getString("createSchema").toBoolean).getOrElse(true)

  override val isCloud = Try(properties.getString("isCloud").toBoolean).getOrElse(true)

  override val dataDir = properties.getString("dataDir", None)

  override val tokenizedFields = Try(properties.getString("tokenizedFields").toBoolean).getOrElse(false)

  override def dateType: TypeOp.Value = TypeOp.Long

  @transient
  private val solrClients: Map[String, SolrClient] = {
    bcSchema.get.filter(tschema => tschema.outputName == keyName).map(tschemaFiltered => {
      val tableSchemaTime = getTableSchemaFixedId(tschemaFiltered)
      tableSchemaTime.tableName -> getSolrServer(connection, isCloud)
    }).toMap
  }

  override def setup: Unit = if (validConfiguration) createCores else log.info(SolrConfigurationError)

  private def createCores: Unit = {
    val coreList = getCoreList(connection, isCloud)
    bcSchema.get.filter(tschema => tschema.outputName == keyName).foreach(tschemaFiltered => {
      val tableSchemaTime = getTableSchemaFixedId(tschemaFiltered)
      if (!coreList.contains(tableSchemaTime.tableName)) {
        createCoreAccordingToSchema(solrClients, tableSchemaTime.tableName, tableSchemaTime.schema)
      }
    })
  }

  override def doPersist(stream: DStream[(T, MeasuresValues)]): Unit =
    if (validConfiguration) persistDataFrame(stream)
    else log.info(SolrConfigurationError)

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val slrRelation = new SolrRelation(sqlContext, getConfig(connection, tableName), dataFrame)
    slrRelation.insert(dataFrame, true)
  }
}
