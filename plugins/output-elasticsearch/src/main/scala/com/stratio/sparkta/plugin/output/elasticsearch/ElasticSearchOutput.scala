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

package com.stratio.sparkta.plugin.output.elasticsearch

import java.io.{Serializable => JSerializable}

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.streaming.dstream.DStream
import org.elasticsearch.node.NodeBuilder
import org.elasticsearch.spark.sql._

import com.stratio.sparkta.plugin.output.elasticsearch.dao.ElasticSearchDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

/**
 *
 * Check for possible Elasticsearch-dataframe settings
 *
 * org/elasticsearch/hadoop/cfg/Settings.java
 * org/elasticsearch/hadoop/cfg/ConfigurationOptions.java
 *
 */
class ElasticSearchOutput(keyName: String,
                          properties: Map[String, JSerializable],
                          operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                          bcSchema: Option[Seq[TableSchema]])
  extends Output(keyName, properties, operationTypes, bcSchema) with ElasticSearchDAO {

  // this regex pretend validate all localhost loopback values including ipv6
  final val LocalhostPattern = "^localhost$|^127(?:\\.[0-9]+){0,2}\\.[0-9]+$|^(?:0*\\:)*?:?0*1$".r.pattern

  override val dateType = getDateTimeType(properties.getString("dateType", None))

  override def isAutoCalculateId: Boolean = true

  override val nodes = properties.getHostPortConfs("nodes", DefaultNode, DefaultPort)

  @transient private lazy val elasticClient = {
    if (isLocalhost) ElasticClient.fromNode(NodeBuilder.nodeBuilder().client(true).node())
    else ElasticClient.remote(nodes(0)._1, nodes(0)._2)
  }

  val isLocalhost: Boolean = LocalhostPattern.matcher(nodes(0)._1).matches

  override val idField = properties.getString("idField", None)

  override val mappingType = properties.getString("indexMapping", Some(DefaultIndexType))

  def indexNameType(tableName: String): String = (tableName + "/" + mappingType.get).toLowerCase

  override def setup: Unit = createIndices

  private def createIndices: Unit = {
    getSchema.map(tableSchemaTime => createIndexAccordingToSchema(tableSchemaTime.tableName, tableSchemaTime.schema))
    elasticClient.close()
  }

  def getSchema: Seq[TableSchema] = bcSchema.get.filter(_.outputName == keyName).map(getTableSchemaFixedId)

  private def createIndexAccordingToSchema(tableName: String, schema: StructType) =
    elasticClient.execute {
      create index tableName shards 1 replicas 0 mappings (
        mappingType.get.toLowerCase as (getElasticsearchFields(schema))
        )
    }

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit =
    persistDataFrame(stream)

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val sparkConfig = getSparkConfig(timeDimension, idField.isDefined || isAutoCalculateId)
    dataFrame.saveToEs(indexNameType(tableName), sparkConfig)
  }

  //scalastyle:off
  def getElasticsearchFields(schema: StructType): Seq[TypedFieldDefinition] = {
    schema.map(structField => structField.dataType match {
      case LongType => structField.name typed FieldType.LongType
      case DoubleType => structField.name typed FieldType.DoubleType
      case DecimalType() => structField.name typed FieldType.DoubleType
      case IntegerType => structField.name typed FieldType.IntegerType
      case BooleanType => structField.name typed FieldType.BooleanType
      case DateType => structField.name typed FieldType.DateType
      case TimestampType => structField.name typed FieldType.DateType
      case ArrayType(_, _) => structField.name typed FieldType.MultiFieldType
      case MapType(_, _, _) => structField.name typed FieldType.ObjectType
      case StringType => structField.name typed FieldType.StringType index "not_analyzed"
      case _ => structField.name typed FieldType.BinaryType
    })
  }
}
