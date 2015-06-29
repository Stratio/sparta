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

import scala.util.Try

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings._
import com.stratio.sparkta.plugin.output.elasticsearch.dao.ElasticSearchDAO
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.streaming.dstream.DStream
import org.elasticsearch.node.NodeBuilder
import org.elasticsearch.spark.sql._

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
                          @transient sparkContext: SparkContext,
                          operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                          bcSchema: Option[Broadcast[Seq[TableSchema]]],
                          timeName: String)
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema, timeName) with ElasticSearchDAO {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Max, WriteOp.Min,
    WriteOp.Range, WriteOp.AccAvg, WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText,
    WriteOp.AccSet)

  override val dateType = getDateTimeType(properties.getString("dateType", None))

  override val nodes = properties.getString("nodes", DEFAULT_NODE)

  override val defaultPort = properties.getString("defaultPort", DEFAULT_PORT)

  @transient private val elasticClient = {
    if (nodes.equals("localhost") || nodes.equals("127.0.0.1")){
      val timeout: Long = 5000
      ElasticClient.fromNode(NodeBuilder.nodeBuilder().client(true).node(), timeout)
    }else{
      ElasticClient.remote(nodes, defaultPort.toInt)
    }
  }

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val fixedDimensions: Array[String] = properties.getString("fixedDimensions", None) match {
    case None => Array()
    case Some(fixDimensions) => fixDimensions.split(fieldsSeparator)
  }

  override val isAutoCalculateId = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(false)

  val fixedAgg = properties.getString("fixedAggregation", None)

  override val fixedAggregation: Map[String, Option[Any]] =
    if(fixedAgg.isDefined){
      val fixedAggSplited = fixedAgg.get.split(Output.FixedAggregationSeparator)
      Map(fixedAggSplited.head -> Some(fixedAggSplited.last))
    } else Map()

  override val idField = properties.getString("idField", None)

  override val mappingType = properties.getString("indexMapping", Some(DEFAULT_INDEX_TYPE))

  override def setup: Unit = { createIndices }

  private def createIndices = {
    bcSchema.get.value.filter(tschema => (tschema.outputName == keyName)).foreach(tschemaFiltered => {
      val tableSchemaTime = getTableSchemaFixedId(tschemaFiltered)
      createIndexAcordingToSchema(tableSchemaTime.tableName, tableSchemaTime.schema)
    })
    elasticClient.close()
  }

  private def createIndexAcordingToSchema(tableName: String, schema: StructType) = {
    elasticClient.execute {
      create index tableName shards 1 replicas 0 mappings(
        mappingType.get.toLowerCase as(getElasticsearchFields(schema))
        )
    }
  }


  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String): Unit = {
    val sparkConfig = getSparkConfig(timeName, idField.isDefined || isAutoCalculateId)
    val indexNameType = (tableName + "/" + mappingType.get).toLowerCase
    dataFrame.saveToEs(indexNameType, sparkConfig)
  }



  //scalastyle:off
  def getElasticsearchFields(schema: StructType): Seq[TypedFieldDefinition] = {
    schema.map(structField => structField.dataType match {
      case org.apache.spark.sql.types.LongType => structField.name typed FieldType.LongType
      case org.apache.spark.sql.types.DoubleType => structField.name typed FieldType.DoubleType
      case org.apache.spark.sql.types.DecimalType() => structField.name typed FieldType.DoubleType
      case org.apache.spark.sql.types.IntegerType => structField.name typed FieldType.IntegerType
      case org.apache.spark.sql.types.BooleanType => structField.name typed FieldType.BooleanType
      case org.apache.spark.sql.types.DateType => structField.name typed FieldType.DateType
      case org.apache.spark.sql.types.TimestampType => structField.name typed FieldType.DateType
      case org.apache.spark.sql.types.ArrayType(_,_) => structField.name typed FieldType.MultiFieldType
      case org.apache.spark.sql.types.StringType => structField.name typed FieldType.StringType index "not_analyzed"
      case _ => structField.name typed FieldType.BinaryType
    })
  }
  //scalastyle:on

}
