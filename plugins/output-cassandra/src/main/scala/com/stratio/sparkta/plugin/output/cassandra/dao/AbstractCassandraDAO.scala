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
package com.stratio.sparkta.plugin.output.cassandra.dao

import java.io.Closeable
import scala.collection.mutable

trait AbstractCassandraDAO extends Closeable {


  def connectionHosts : String
  def keyspace : String
  def analyzer : String
  def textIndexFields : Array[String]
  def languageFieldName: String = "language"
  def timestampFieldName : String = "eventTime"
  def textIndexFieldsName : String = "lucene"
  def fieldsSeparator : String = ","
  def indexNameSeparator : String = "_"

  protected def client: MongoClient = AbstractMongoDAO.client(mongoClientUri)

  protected def db(dbName: String): MongoDB = AbstractMongoDAO.db(mongoClientUri, dbName)

  protected def db(): MongoDB = db(dbName)

  protected def createTextIndex(table : String,
                       indexName : String,
                       indexColumn :String,
                       indexFields : Array[String],
                       analyzer : String) : Unit = {

    if(table.nonEmpty && indexColumn.nonEmpty && indexFields.nonEmpty && indexName.nonEmpty){

      )
    }
  }

  protected def createIndex(table : String,
                  indexName : String,
                  indexField : String) : Unit = {

    if(table.nonEmpty && indexField.nonEmpty && indexName.nonEmpty){

    }
  }

  protected def insert(dbName: String, collName: String, dbOjects: Iterator[DBObject],
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

private object AbstractCassandraDAO {

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
