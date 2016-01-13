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
import com.stratio.datasource.mongodb.MongodbConfig
import com.stratio.sparkta.plugin.output.mongodb.dao.MongoDbDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SaveMode._
import org.apache.spark.streaming.dstream.DStream

class MongoDbOutput[T](keyName: String,
                    version: Option[Int],
                    properties: Map[String, JSerializable],
                    operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                    bcSchema: Option[Seq[TableSchema]])
  extends Output[T](keyName, version, properties, operationTypes, bcSchema) with MongoDbDAO {

  RegisterJodaTimeConversionHelpers()

  override val isAutoCalculateId = true

  override val hosts = getConnectionConfs("hosts", "host", "port")

  override val dbName = properties.getString("dbName", "sparkta")

  override val connectionsPerHost = properties.getString("connectionsPerHost", DefaultConnectionsPerHost).toInt

  override val threadsAllowedB = properties.getString("threadsAllowedToBlock", DefaultThreadsAllowedToBlock).toInt

  override val retrySleep = properties.getString("retrySleep", DefaultRetrySleep).toInt

  override val textIndexFields = properties.getString("textIndexFields", None).map(_.split(FieldsSeparator))

  override val language = properties.getString("language", None)

  override def setup: Unit = {
    if (bcSchema.isDefined) {
      val db = connectToDatabase
      val schemasFiltered =
        bcSchema.get.filter(schemaFilter => schemaFilter.outputName == keyName).map(getTableSchemaFixedId(_))
      filterSchemaByFixedAndTimeDimensions(schemasFiltered)
        .foreach(tableSchema => createPkTextIndex(db, tableSchema.tableName, tableSchema.timeDimension))
      db.close()
    }
  }

  override def doPersist(stream : DStream[(T, MeasuresValues)]) : Unit = {
    persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val options = getDataFrameOptions(tableName, timeDimension)
    dataFrame.write
      .format(MongoDbSparkDatasource)
      .mode(Append)
      .options(options)
      .save()
  }

  private def getDataFrameOptions(tableName: String, timeDimension: String): Map[String, String] =
    Map(
      MongodbConfig.Host -> hosts,
      MongodbConfig.Database -> dbName,
      MongodbConfig.Collection -> tableName) ++ getPrimaryKeyOptions(timeDimension) ++ {
      if (language.isDefined) Map(MongodbConfig.Language -> language.get) else Map()
    }

  private def getPrimaryKeyOptions(timeDimension: String): Map[String, String] =
    if (!timeDimension.isEmpty) {
      Map(MongodbConfig.UpdateFields -> Seq(Output.Id, timeDimension).mkString(","))
    } else Map(MongodbConfig.UpdateFields -> Output.Id)

  private def getConnectionConfs(key : String, firstJsonItem : String, secondJsonItem : String) : String = {
    val conObj = properties.getMapFromJsoneyString(key)
    conObj.map(c => {
      val host = c.getOrElse(firstJsonItem, DefaultHost)
      val port = c.getOrElse(secondJsonItem, DefaultPort)
      s"$host:$port"
    }).mkString(",")
  }
}
