/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.output.mongodb.dao

import java.io.Closeable

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI, MongoDB}
import com.mongodb.{DBObject, MongoClientOptions, WriteConcern, casbah, MongoClientURI => JMongoClientURI}

import scala.collection.mutable

trait AbstractMongoDAO extends Closeable {

  def mongoClientUri : String
  def dbName : String
  def language : String
  def textIndexFields : Array[String]
  def languageFieldName: String = "language"
  def eventTimeFieldName : String = "eventTime"
  def idDefaultFieldName : String = "_id"
  def idSeparator : String = "_"

  protected def client: MongoClient = AbstractMongoDAO.client(mongoClientUri)

  protected def db(dbName: String): MongoDB = AbstractMongoDAO.db(mongoClientUri, dbName)

  protected def db(): MongoDB = db(dbName)

  protected def defaultWriteConcern = casbah.WriteConcern.Unacknowledged

  def indexExists(collection : String, indexName : String) : Boolean = {

    var indexExists = false
    val itObjects = db.getCollection(collection).getIndexInfo().iterator()

    while(itObjects.hasNext && !indexExists){
      val indexObject = itObjects.next()
      if(indexObject.containsField("name") && (indexObject.get("name") == indexName)) indexExists = true
    }
    indexExists
  }

  def createTextIndex(collection : String,
                       indexName : String,
                       indexFields : Array[String],
                       language : String) : Unit = {

    if(collection.nonEmpty && indexFields.nonEmpty && indexName.nonEmpty && !indexExists(collection, indexName)){
      val fields = indexFields.map(_ -> "text").toList
      val options = MongoDBObject.newBuilder
      options += "name" -> indexName
      options += "background" -> true
      if(language != "") options += "default_language" -> language

      db.getCollection(collection).createIndex(MongoDBObject(fields), options.result
      )
    }
  }

  def createIndex(collection : String,
                  indexName : String,
                  indexFields : Map[String, Int],
                  unique : Boolean,
                  background : Boolean) : Unit = {

    if(collection.nonEmpty && indexFields.nonEmpty && indexName.nonEmpty && !indexExists(collection, indexName)){
      val fields = indexFields.map(field => field._1 -> field._2).toList
      val options = MongoDBObject.newBuilder
      options += "name" -> indexName
      options += "background" -> background
      options += "unique" -> unique

      db.getCollection(collection).createIndex(MongoDBObject(fields), options.result)
    }
  }

  def insert(dbName: String, collName: String, dbOjects: Iterator[DBObject],
             writeConcern: Option[WriteConcern] = None): Unit = {
    val coll = db(dbName).getCollection(collName)
    val builder = coll.initializeUnorderedBulkOperation

    dbOjects.map(dbObjectsBatch =>
        builder.insert(dbObjectsBatch)
    )
    if(writeConcern.isEmpty) builder.execute(defaultWriteConcern) else builder.execute(writeConcern.get)

  }

  override def close(): Unit = {
  }

}

private object AbstractMongoDAO {

  private val clients: mutable.Map[String, MongoClient] = mutable.Map()
  private val dbs: mutable.Map[(String, String), MongoDB] = mutable.Map()
  private lazy val options = MongoClientOptions.builder()
    .connectionsPerHost(5)
    .writeConcern(casbah.WriteConcern.Unacknowledged)
    .threadsAllowedToBlockForConnectionMultiplier(10)

  private def client(mongoClientUri: String): MongoClient = {
    if (!clients.contains(mongoClientUri)) {
      val jURI = new JMongoClientURI(mongoClientUri, options)
      clients.put(mongoClientUri, MongoClient(new MongoClientURI(jURI)))
    }
    clients(mongoClientUri)
  }

  private def db(mongoClientUri: String, dbName: String): MongoDB = {
    val key = (mongoClientUri, dbName)
    if (!dbs.contains(key)) dbs.put(key, client(mongoClientUri).getDB(dbName))

    dbs(key)
  }

}
