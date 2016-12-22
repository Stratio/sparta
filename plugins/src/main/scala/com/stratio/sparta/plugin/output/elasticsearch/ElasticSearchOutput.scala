/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparta.plugin.output.elasticsearch

import java.io.{Serializable => JSerializable}

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.stratio.sparta.plugin.output.elasticsearch.dao.ElasticSearchDAO
import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.{SpartaSchema, TypeOp}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.elasticsearch.common.settings._

/**
 *
 * Check for possible Elasticsearch-dataframe settings
 *
 * org/elasticsearch/hadoop/cfg/Settings.java
 * org/elasticsearch/hadoop/cfg/ConfigurationOptions.java
 *
 */
class ElasticSearchOutput(keyName: String,
                          version: Option[Int],
                          properties: Map[String, JSerializable],
                          schemas: Seq[SpartaSchema])
  extends Output(keyName, version, properties, schemas) with ElasticSearchDAO {

  override val idField = properties.getString("idField", None)

  override val mappingType = properties.getString("indexMapping", DefaultIndexType)

  override val clusterName = properties.getString("clusterName", DefaultCluster)

  override val tcpNodes = getHostPortConfs(NodesName, DefaultNode, DefaultTcpPort, NodeName, TcpPortName)

  override val httpNodes = getHostPortConfs(NodesName, DefaultNode, DefaultHttpPort, NodeName, HttpPortName)

  @transient private lazy val elasticClient = {
    val settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build()
    if (isLocalhost) ElasticClient.local(settings)
    else {
      val hostsPorts = tcpNodes.map { case (host, port) => s"$host:$port" }.mkString(",")
      val uri = s"elasticsearch://$hostsPorts"
      ElasticClient.remote(settings, ElasticsearchClientUri(uri))
    }
  }

  lazy val isLocalhost: Boolean = LocalhostPattern.matcher(tcpNodes.head._1).matches

  lazy val mappingName = versionedTableName(mappingType).toLowerCase

  override def setup(options: Map[String, String]): Unit = createIndices

  private def createIndices: Unit = {
    schemas.map(tableSchemaTime => createIndexAccordingToSchema(tableSchemaTime))
    elasticClient.close
  }

  private def createIndexAccordingToSchema(tableSchemaTime: SpartaSchema) =
    elasticClient.execute {
      create index tableSchemaTime.tableName.toLowerCase shards 5 replicas 1 mappings (
        mappingName as getElasticsearchFields(tableSchemaTime))
    }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val dataFrameSchema = dataFrame.schema
    val timeDimension = dataFrameSchema.fields.filter(stField => stField.metadata.contains(Output.TimeDimensionKey))
      .map(_.name).headOption
    getTimeFromOptions(options)
    val sparkConfig = getSparkConfig(timeDimension, saveMode)


    //Necessary this dataFrame transformation because ES not support java.sql.TimeStamp in the row values: use
    // dateType in the cube writer options and set to long, date or dateTime
    val newDataFrame = if (dataFrameSchema.fields.exists(stField => stField.dataType == TimestampType)) {
      val rdd = dataFrame.map(row => {
        val seqOfValues = row.toSeq.map { value =>
          value match {
            case value: java.sql.Timestamp => value.getTime
            case _ => value
          }
        }
        Row.fromSeq(seqOfValues)
      })
      val newSchema = StructType(dataFrameSchema.map(structField =>
        if (structField.dataType == TimestampType) structField.copy(dataType = LongType)
        else structField)
      )
      SQLContext.getOrCreate(dataFrame.rdd.sparkContext).createDataFrame(rdd, newSchema)
    }
    else dataFrame

    validateSaveMode(saveMode)

    dataFrame.write
      .format("org.elasticsearch.spark.sql")
      .mode(getSparkSaveMode(saveMode))
      .options(sparkConfig)
      .save(indexNameType(tableName))
  }

  def indexNameType(tableName: String): String = s"${tableName.toLowerCase}/$mappingName"

  //scalastyle:off
  def getElasticsearchFields(tableSchemaTime: SpartaSchema): Seq[TypedFieldDefinition] = {
    tableSchemaTime.schema.map(structField =>
      filterDateTypeMapping(structField, tableSchemaTime.timeDimension, tableSchemaTime.dateType).dataType match {
        case LongType => structField.name typed FieldType.LongType
        case DoubleType => structField.name typed FieldType.DoubleType
        case DecimalType() => structField.name typed FieldType.DoubleType
        case IntegerType => structField.name typed FieldType.IntegerType
        case BooleanType => structField.name typed FieldType.BooleanType
        case DateType => structField.name typed FieldType.DateType
        case TimestampType => structField.name typed FieldType.LongType
        case ArrayType(_, _) => structField.name typed FieldType.MultiFieldType
        case MapType(_, _, _) => structField.name typed FieldType.ObjectType
        case StringType => structField.name typed FieldType.StringType index "not_analyzed"
        case _ => structField.name typed FieldType.BinaryType
      })
  }

  //scalastyle:on

  def filterDateTypeMapping(structField: StructField,
                            timeField: Option[String],
                            dateType: TypeOp.Value): StructField = {
    timeField match {
      case Some(timeFieldValue) if structField.name.equals(timeFieldValue) =>
        Output.getTimeFieldType(dateType, structField.name, structField.nullable)
      case _ => structField
    }
  }

  def getHostPortConfs(key: String,
                       defaultHost: String,
                       defaultPort: String,
                       nodeName: String,
                       portName: String): Seq[(String, Int)] = {
    properties.getMapFromJsoneyString(key).map(c =>
      (c.getOrElse(nodeName, defaultHost), c.getOrElse(portName, defaultPort).toInt))
  }
}
