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
package com.stratio.sparta.plugin.output.mongodb.dao

import java.net.UnknownHostException

import com.mongodb.casbah
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.stratio.sparta.sdk._
import org.apache.spark.Logging

import scala.util.{Failure, Success, Try}

trait MongoDbDAO extends Logging {

  final val DefaultConnectionsPerHost = "5"
  final val DefaultThreadsAllowedToBlock = "10"
  final val DefaultRetrySleep = "1000"
  final val LanguageFieldName = "language"
  final val DefaultId = "_id"
  final val DefaultWriteConcern = casbah.WriteConcern.Unacknowledged
  final val DefaultHost = "localhost"
  final val DefaultPort = "27017"
  final val MongoDbSparkDatasource = "com.stratio.datasource.mongodb"

  def hosts: String

  def dbName: String

  def connectionsPerHost: Int

  def threadsAllowedB: Int

  def language: Option[String]

  def textIndexFields: Option[Array[String]]

  def retrySleep: Int

  protected def connectToDatabase: MongoClient = {
    val addresses = mongoAddresses(hosts.split(","))
    Try(MongoClient(addresses, clientOptions)) match {
      case Success(database) => database
      case Failure(e) => {
        Thread.sleep(retrySleep)
        Try(MongoClient(addresses, clientOptions)).getOrElse({
          log.error(e.getMessage)
          throw new MongoException(e.getMessage)
        })
      }
    }
  }

  protected def mongoAddresses(addresses: Seq[String]): List[Imports.ServerAddress] = {
    addresses.flatMap(address => {
      Try(new ServerAddress(address)) match {
        case Success(serverAddress) => Some(serverAddress)
        case Failure(e: IllegalArgumentException) =>
          log.warn("EndPoint " + address + e.getMessage)
          None
        case Failure(e: UnknownHostException) =>
          log.warn("Unable to connect to " + address + e.getMessage)
          None
      }
    }).toList
  }

  protected def clientOptions: MongoClientOptions = MongoClientOptions(connectionsPerHost = connectionsPerHost,
    writeConcern = casbah.WriteConcern.Unacknowledged,
    threadsAllowedToBlockForConnectionMultiplier = threadsAllowedB)

  protected def createPkTextIndex(mongoDatabase: MongoClient, tableSchema: TableSchema): Unit = {
    if (textIndexFields.isDefined && language.isDefined) {
      if (textIndexFields.get.length > 0) {
        createTextIndex(mongoDatabase,
          tableSchema.tableName,
          textIndexFields.mkString(Output.Separator),
          textIndexFields.get,
          language.get
        )
      }
    }

    if (tableSchema.isAutoCalculatedId)
      createIndex(mongoDatabase, tableSchema.tableName, Output.Id, Map(Output.Id -> 1), true, true)
    else {
      val fields = tableSchema.schema.filter(stField =>
        !stField.nullable && !stField.metadata.contains(Output.MeasureMetadataKey))
        .map(stField => stField.name -> 1).toMap
      createIndex(mongoDatabase, tableSchema.tableName, fields.keySet.mkString(Output.Separator), fields, true, true)
    }
  }

  protected def indexExists(mongoDatabase: MongoClient, collection: String, indexName: String): Boolean = {
    var indexExists = false
    val itObjects = mongoDatabase.getDB(dbName).getCollection(collection).getIndexInfo.iterator()

    while (itObjects.hasNext && !indexExists) {
      val indexObject = itObjects.next()
      if (indexObject.containsField("name") && (indexObject.get("name") == indexName)) indexExists = true
    }
    indexExists
  }

  protected def createTextIndex(mongoDatabase: MongoClient,
                                collection: String,
                                indexName: String,
                                indexFields: Array[String],
                                language: String): Unit = {
    if (collection.nonEmpty &&
      indexFields.nonEmpty &&
      indexName.nonEmpty &&
      !indexExists(mongoDatabase, collection, indexName)) {
      val fields = indexFields.map(_ -> "text").toList
      val options = MongoDBObject.newBuilder

      options += "name" -> indexName
      options += "background" -> true
      if (language != "") options += "default_language" -> language
      mongoDatabase.getDB(dbName).getCollection(collection).createIndex(MongoDBObject(fields), options.result)
    }
  }

  protected def createIndex(mongoDatabase: MongoClient,
                            collection: String,
                            indexName: String,
                            indexFields: Map[String, Int],
                            unique: Boolean,
                            background: Boolean): Unit = {
    if (collection.nonEmpty &&
      indexFields.nonEmpty &&
      indexName.nonEmpty &&
      !indexExists(mongoDatabase, collection, indexName)) {
      val fields = indexFields.map { case (field, value) => field -> value }.toList
      val options = MongoDBObject.newBuilder

      options += "name" -> indexName
      options += "background" -> background
      options += "unique" -> unique
      mongoDatabase.getDB(dbName).getCollection(collection).createIndex(MongoDBObject(fields), options.result)
    }
  }
}

