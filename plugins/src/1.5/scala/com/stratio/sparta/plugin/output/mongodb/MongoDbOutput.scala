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
import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.StructType

class MongoDbOutput(keyName: String,
                    version: Option[Int],
                    properties: Map[String, JSerializable],
                    schemas: Seq[SpartaSchema])
  extends Output(keyName, version, properties, schemas) {

  val DefaultHost = "localhost"
  val DefaultPort = "27017"
  val MongoDbSparkDatasource = "com.stratio.datasource.mongodb"
  val hosts = getConnectionConfs("hosts", "host", "port")
  val dbName = properties.getString("dbName", "sparta")

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val timeDimension = getTimeFromOptions(options)
    val dataFrameOptions = getDataFrameOptions(tableName, dataFrame.schema, timeDimension, saveMode)

    validateSaveMode(saveMode)

      dataFrame.write
        .format(MongoDbSparkDatasource)
        .mode(getSparkSaveMode(saveMode))
        .options(dataFrameOptions ++ getCustomProperties)
        .save()
    }

  private def getDataFrameOptions(tableName: String,
                                  schema: StructType,
                                  timeDimension: Option[String],
                                  saveMode: SaveModeEnum.Value): Map[String, String] =
    Map(
      MongodbConfig.Host -> hosts,
      MongodbConfig.Database -> dbName,
      MongodbConfig.Collection -> tableName
    ) ++ {
      saveMode match {
        case SaveModeEnum.Upsert => getPrimaryKeyOptions(schema, timeDimension)
        case _ => Map.empty[String, String]
      }
    }

  private def getPrimaryKeyOptions(schema: StructType,
                                   timeDimension: Option[String]): Map[String, String] = {
    val updateFields =
      schema.fields.filter(stField => stField.metadata.contains(Output.PrimaryKeyMetadataKey)).map(_.name).mkString(",")

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
