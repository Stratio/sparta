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
package com.stratio.sparta.plugin.output.mongodb

import java.io.{Serializable => JSerializable}

import com.mongodb.casbah.commons.conversions.scala._
import com.stratio.datasource.mongodb.MongodbConfig
import com.stratio.sparta.plugin.output.mongodb.dao.MongoDbDAO
import com.stratio.sparta.sdk.Output._
import com.stratio.sparta.sdk.ValidatingPropertyMap._
import com.stratio.sparta.sdk._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql.types.StructType

class MongoDbOutput(keyName: String,
                    version: Option[Int],
                    properties: Map[String, JSerializable],
                    schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas) with MongoDbDAO {

    RegisterJodaTimeConversionHelpers()

    override val hosts = getConnectionConfs("hosts", "host", "port")

    override val dbName = properties.getString("dbName", "sparta")

    override val connectionsPerHost = properties.getString("connectionsPerHost", DefaultConnectionsPerHost).toInt

    override val threadsAllowedB = properties.getString("threadsAllowedToBlock", DefaultThreadsAllowedToBlock).toInt

    override val retrySleep = properties.getString("retrySleep", DefaultRetrySleep).toInt

    override val textIndexFields = properties.getString("textIndexFields", None).map(_.split(FieldsSeparator))

    override val language = properties.getString("language", None)

    override def setup(options: Map[String, String]): Unit = {
      val db = connectToDatabase

      schemas.foreach(tableSchema => createPkTextIndex(db, tableSchema))
      db.close()
    }

    override def upsert(dataFrame: DataFrame, options: Map[String, String]): Unit = {
      val tableName = getTableNameFromOptions(options)
      val isAutoCalculatedId = getIsAutoCalculatedIdFromOptions(options)
      val timeDimension = getTimeFromOptions(options)
      val dataFrameOptions = getDataFrameOptions(tableName, dataFrame.schema, timeDimension, isAutoCalculatedId)

      dataFrame.write
        .format(MongoDbSparkDatasource)
        .mode(Append)
        .options(dataFrameOptions)
        .save()
    }

    private def getDataFrameOptions(tableName: String,
                                    schema: StructType,
                                    timeDimension: Option[String],
                                    isAutoCalculatedId: Boolean): Map[String, String] =
      Map(
        MongodbConfig.Host -> hosts,
        MongodbConfig.Database -> dbName,
        MongodbConfig.Collection -> tableName
      ) ++ getPrimaryKeyOptions(schema, timeDimension, isAutoCalculatedId) ++ {
        if (language.isDefined) Map(MongodbConfig.Language -> language.get) else Map.empty
      }

    private def getPrimaryKeyOptions(schema: StructType,
                                     timeDimension: Option[String],
                                     isAutoCalculatedId: Boolean): Map[String, String] = {
      val updateFields = if (isAutoCalculatedId) Output.Id
        else schema.fields.filter(stField =>
        !stField.nullable && !stField.metadata.contains(Output.MeasureMetadataKey))
        .map(_.name).mkString(",")

      Map(MongodbConfig.UpdateFields -> updateFields)
    }

    private def getConnectionConfs(key: String, firstJsonItem: String, secondJsonItem: String): String = {
      val conObj = properties.getMapFromJsoneyString(key)
      conObj.map(c => {
        val host = c.getOrElse(firstJsonItem, DefaultHost)
        val port = c.getOrElse(secondJsonItem, DefaultPort)
        s"$host:$port"
      }).mkString(",")
    }
  }
