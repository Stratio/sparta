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

import com.lucidworks.spark.SolrRelation
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.{HttpSolrClient, CloudSolrClient}
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try

class SolrOutput(keyName: String,
                          properties: Map[String, JSerializable],
                          @transient sparkContext: SparkContext,
                          operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                          bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema) {

  final val DefaultNode = "localhost"
  final val DefaultPort = "9983"

  val zkHost = properties.getString("zkHost", s"$DefaultNode:$DefaultPort")

  val createSchema = Try(properties.getString("createSchema").toBoolean).getOrElse(false)

  val isCloud = Try(properties.getString("isCloud").toBoolean).getOrElse(true)

  private val solrClients: Map[String, SolrClient] = {
    bcSchema.get.value.filter(tschema => (tschema.outputName == keyName)).map(tschemaFiltered => {
      if (isCloud)
        tschemaFiltered.tableName -> new CloudSolrClient(zkHost)
      else
        tschemaFiltered.tableName -> new HttpSolrClient("http://" + zkHost + "/solr")
    }).toMap
  }

  override def setup: Unit = createCores

  private def createCores : Unit = {

  }

  override val isAutoCalculateId = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(true)

  val idField = properties.getString("idField", None)

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val slrRelation = new SolrRelation(sqlContext, getConfig(zkHost, tableName))
    slrRelation.insert(dataFrame, true)
  }

  private def getConfig(host: String, collection: String): Map[String, String] =
    Map("zkhost" -> host, "collection" -> collection)

  //scalastyle:off
  def getSolrFieldType(structField: StructField): String = {
    structField.dataType match {
      case org.apache.spark.sql.types.LongType => "long"
      case org.apache.spark.sql.types.DoubleType => "double"
      case org.apache.spark.sql.types.DecimalType() => "double"
      case org.apache.spark.sql.types.IntegerType => "int"
      case org.apache.spark.sql.types.BooleanType => "boolean"
      case org.apache.spark.sql.types.DateType => "dateTime"
      case org.apache.spark.sql.types.TimestampType => "dateTime"
      case org.apache.spark.sql.types.ArrayType(_, _) => "string"
      case org.apache.spark.sql.types.StringType => "string"
      case _ => "string"
    }
  }
  //scalastyle:on
}
