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

package com.stratio.sparkta.plugin.output.mongodb

import java.io.{Serializable => JSerializable}

import com.mongodb.casbah.commons.conversions.scala._
import com.stratio.sparkta.plugin.output.mongodb.dao.MongoDbDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SaveMode._
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try

class MongoDbOutput(keyName: String,
                    properties: Map[String, JSerializable],
                    operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                    bcSchema: Option[Seq[TableSchema]])
  extends Output(keyName, properties, operationTypes, bcSchema) with MongoDbDAO {

  RegisterJodaTimeConversionHelpers()

  override val isAutoCalculateId = true

  override val hosts = getConnectionConfs("hosts", "host", "port")

  override val dbName = properties.getString("dbName", "sparkta")

  override val connectionsPerHost = properties.getString("connectionsPerHost", DefaultConnectionsPerHost).toInt

  override val threadsAllowedB = properties.getString("threadsAllowedToBlock", DefaultThreadsAllowedToBlock).toInt

  override val retrySleep = properties.getString("retrySleep", DefaultRetrySleep).toInt

  override val idAsField = Try(properties.getString("idAsField").toBoolean).getOrElse(false)

  override val textIndexFields = properties.getString("textIndexFields", None).map(_.split(FieldsSeparator))

  override val language = properties.getString("language", None)

  override def setup: Unit =
    if (bcSchema.isDefined) {
      val schemasFiltered =
        bcSchema.get.filter(schemaFilter => schemaFilter.outputName == keyName).map(getTableSchemaFixedId(_))
      filterSchemaByFixedAndTimeDimensions(schemasFiltered)
        .foreach(tableSchema => createPkTextIndex(tableSchema.tableName, tableSchema.timeDimension))
    }

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val options = getDataFrameOptions(tableName, timeDimension)
    dataFrame.write
      .format("com.stratio.provider.mongodb")
      .mode(Append)
      .options(options)
      .save()
  }

  private def getDataFrameOptions(tableName: String, timeDimension: String): Map[String, String] =
    Map(
      "host" -> hosts,
      "database" -> dbName,
      "collection" -> tableName) ++ getPrimaryKeyOptions(timeDimension) ++ {
      if (language.isDefined) Map("language" -> language.get) else Map()
    }

  private def getPrimaryKeyOptions(timeDimension: String): Map[String, String] =
    if (idAsField) Map("_idField" -> Output.Id)
    else {
      if (!timeDimension.isEmpty) {
        Map("searchFields" -> Seq(Output.Id, timeDimension).mkString(","))
      } else Map()
    }
  private def getConnectionConfs(key: String, firstJsonItem: String, secondJsonItem: String): String = {
    val conObj = properties.getConnectionChain(key)
    conObj.map(c => {
      val host = c.get(firstJsonItem).getOrElse(DefaultHost)
      val port = c.get(secondJsonItem).getOrElse(DefaultPort)
      s"$host:$port"
    }).mkString(",")
  }
}

