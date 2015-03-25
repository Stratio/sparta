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
  def textIndexName : String
  def languageFieldName = "language"
  def eventTimeFieldName = "eventTime"
  def idFieldName = "_id"
  def idSeparator = "__"

  protected def client: MongoClient = AbstractMongoDAO.client(mongoClientUri)

  protected def db(dbName: String): MongoDB = AbstractMongoDAO.db(mongoClientUri, dbName)

  protected def db(): MongoDB = db(dbName)

  protected def defaultWriteConcern = casbah.WriteConcern.Majority

  def indexExists(collection : String, indexName : String) : Boolean = {

    var indexExists = false
    val itObjects = db.getCollection(collection).getIndexInfo().iterator()

    while(itObjects.hasNext && !indexExists){
      val indexObject = itObjects.next()
      if(indexObject.containsField("name") && (indexObject.get("name") == indexName))
        indexExists = true
    }
    indexExists
  }

  def createTextIndex(collection : String, indexName : String, indexField : String, language : String) : Unit = {

    if(!indexExists(collection, indexName)){
      val options = MongoDBObject.newBuilder
      options += "name" -> indexName
      options += "background" -> true
      if((language != null) && (language != ""))
        options += "default_language" -> language

      db.getCollection(collection).createIndex(
        MongoDBObject(indexField -> "text"),
        options.result
      )
    }
  }

  def createIndex(collection : String, indexName : String, indexField : String, order : Int) : Unit = {

    if(!indexExists(collection, indexName)){
      val options = MongoDBObject.newBuilder
      options += "name" -> (indexName + "_" + order)
      options += "background" -> true

      db.getCollection(collection).createIndex(
        MongoDBObject(indexField -> order),
        options.result
      )
    }
  }

  def insert(dbName: String, collName: String, dbOjects: Iterator[DBObject], writeConcern: WriteConcern = null): Unit = {
    val coll = db(dbName).getCollection(collName)
    val builder = coll.initializeUnorderedBulkOperation

    dbOjects.map(dbObjectsBatch =>
        builder.insert(dbObjectsBatch)
    )
    if(writeConcern == null)
      builder.execute(defaultWriteConcern)
    else builder.execute(writeConcern)
  }

  override def close(): Unit = {
  }

}

private object AbstractMongoDAO {

  private val clients: mutable.Map[String, MongoClient] = mutable.Map()
  private val dbs: mutable.Map[(String, String), MongoDB] = mutable.Map()
  private lazy val options = MongoClientOptions.builder()
    .connectionsPerHost(5)
    .writeConcern(com.mongodb.WriteConcern.UNACKNOWLEDGED)
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
    if (!dbs.contains(key)) {
      dbs.put(key, client(mongoClientUri).getDB(dbName))
    }
    dbs(key)
  }

}
