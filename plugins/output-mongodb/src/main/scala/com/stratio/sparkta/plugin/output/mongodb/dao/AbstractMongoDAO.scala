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

import com.mongodb.casbah.{MongoClient, MongoClientURI, MongoDB}
import com.mongodb.{DBObject, MongoClientOptions, WriteConcern, casbah, MongoClientURI => JMongoClientURI}
import com.stratio.sparkta.plugin.output.mongodb.dao.AbstractMongoDAO._

import scala.collection.mutable

trait AbstractMongoDAO extends Closeable {

  def mongoClientUri : String
  def dbName : String

  protected def client: MongoClient = AbstractMongoDAO.client(mongoClientUri)

  protected def db(dbName: String): MongoDB = AbstractMongoDAO.db(mongoClientUri, dbName)

  protected def db(): MongoDB = db(dbName)

  protected def defaultWriteConcern = casbah.WriteConcern.withRule(w = "majority", j = true)

  def insert
  (dbName: String, collName: String, dbOjects: Iterator[DBObject], writeConcern: WriteConcern = null): Unit = {
    val coll = db(dbName).getCollection(collName)
    dbOjects.grouped(INSERT_BATCH_SIZE).map(dbObjectsBatch =>
      if (writeConcern == null) {
        coll.insert(dbObjectsBatch.toArray: _*)
      } else {
        coll.insert(dbObjectsBatch.toArray, writeConcern)
      }
    )
  }

  override def close(): Unit = {
  }

}

private object AbstractMongoDAO {

  /**
   * Too many insertions in same batch lead to the following error:
   * command ironport.$cmd command: insert { $msg: "query not recording (too large)" }
   */
  private val INSERT_BATCH_SIZE = 128

  private val clients: mutable.Map[String, MongoClient] = mutable.Map()
  private val dbs: mutable.Map[(String, String), MongoDB] = mutable.Map()
  private lazy val options = MongoClientOptions.builder()
    .connectionsPerHost(5)
    .writeConcern(com.mongodb
    .WriteConcern.UNACKNOWLEDGED)
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
