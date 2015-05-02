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

package com.stratio.sparkta.plugin.output.cassandra.dao

import java.io.Closeable

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.sql.types._
import org.apache.spark.{Logging, SparkConf}

import com.stratio.sparkta.sdk.{Output, TableSchema}

trait AbstractCassandraDAO extends Closeable with Logging {

  val connector: Option[CassandraConnector] = None

  def fieldsSeparator: String = ","

  def connectionHost: String

  def cluster: String

  def keyspace: String

  def keyspaceClass: String

  def replicationFactor: String

  def compactStorage: Option[String] = None

  def clusteringBuckets: Array[String]

  def indexFields: Array[String]

  def textIndexFields: Array[String]

  def textIndexName: String

  def analyzer: Option[String] = None

  def configConnector(sparkConfig: SparkConf): Option[CassandraConnector] =
    Some(CassandraConnector(sparkConfig.set("spark.cassandra.connection.host", connectionHost)))

  def createKeypace: Boolean = connector.exists(doCreateKeyspace(_))

  def createTables(tSchemas: Seq[TableSchema], clusteringField: Option[String], autoCalculateId: Boolean): Boolean =
    connector.exists(doCreateTables(_, tSchemas, clusteringField, autoCalculateId))

  def createIndexes(tSchemas: Seq[TableSchema], clusteringField: Option[String], autoCalculateId: Boolean): Boolean =
    connector.exists(doCreateIndexes(_, tSchemas, clusteringField, autoCalculateId))

  protected def doCreateKeyspace(conn: CassandraConnector): Boolean = {
    executeCommand(conn,
      s"CREATE KEYSPACE IF NOT EXISTS $keyspace " +
        s"WITH REPLICATION = {'class': '$keyspaceClass', " +
        s"'replication_factor': $replicationFactor }")
  }

  def doCreateTables(conn: CassandraConnector,
                     tSchemas: Seq[TableSchema],
                     clusteringField: Option[String],
                     autoCalculateId: Boolean): Boolean = {
    tSchemas.map(tableSchema =>
      createTable(conn, tableSchema.tableName, tableSchema.schema, clusteringField, autoCalculateId))
      .reduce((a, b) => (if (!a || !b) false else true))
  }

  protected def createTable(conn: CassandraConnector,
                            table: String,
                            schema: StructType,
                            clusteringField: Option[String],
                            autoCalculateId: Boolean): Boolean = {
    val schemaPkCloumns: Option[String] = schemaToPkCcolumns(schema, clusteringField, autoCalculateId)
    val compactSt = compactStorage match {
      case None => ""
      case Some(compact) => s" WITH $compact"
    }
    schemaPkCloumns match {
      case None => false
      case Some(pkColumns) => executeCommand(conn,
        s"CREATE TABLE IF NOT EXISTS $keyspace.$table $pkColumns $compactSt")
    }
  }

  protected def doCreateIndexes(conn: CassandraConnector,
                                tSchemas: Seq[TableSchema],
                                clusteringTime: Option[String],
                                autoCalculateId: Boolean): Boolean = {
    val seqResults = for {
      tableSchema <- tSchemas
      indexField <- indexFields
      primaryKey = getPrimaryKey(tableSchema.schema, clusteringTime, autoCalculateId)
      created = if (!primaryKey.contains(indexField)) {
        createIndex(conn, tableSchema.tableName, indexField)
      } else {
        log.info(s"The indexed field: $indexField is part of primary key.")
        false
      }
    } yield created
    seqResults.reduce((a, b) => (if (!a || !b) false else true))
  }

  protected def createIndex(conn: CassandraConnector, tableName: String, field: String): Boolean =
    executeCommand(conn, s"CREATE INDEX IF NOT EXISTS index_$field ON $keyspace.$tableName ($field)")

  protected def executeCommand(conn: CassandraConnector, command: String): Boolean = {
    conn.withSessionDo(session => session.execute(command))
    //TODO check if is correct now all true
    true
  }

  protected def dataTypeToCassandraType(dataType: DataType): String = {
    dataType match {
      case StringType => "text"
      case LongType => "bigint"
      case DoubleType => "double"
      case IntegerType => "int"
      case BooleanType => "boolean"
      case DateType | TimestampType => "timestamp"
      case _ => "blob"
    }
  }

  protected def pkConditions(field: StructField, clusteringTime: Option[String]): Boolean =
    !field.nullable && clusteringTime.forall(field.name != _) && !clusteringBuckets.contains(field.name)

  protected def clusteringConditions(field: StructField, clusteringTime: Option[String]): Boolean =
    !field.nullable && (clusteringTime.exists(field.name == _) || clusteringBuckets.contains(field.name))

  protected def schemaToPkCcolumns(schema: StructType,
                                   clusteringTime: Option[String],
                                   autoCalculateId: Boolean): Option[String] = {
    val fields = schema.map(field => field.name + " " + dataTypeToCassandraType(field.dataType)).mkString(",")
    val primaryKey = getPrimaryKey(schema, clusteringTime, autoCalculateId).mkString(",")
    val clusteringColumns =
      schema.filter(field => clusteringConditions(field, clusteringTime)).map(_.name).mkString(",")
    val pkCcolumns = if (clusteringColumns.isEmpty) s"($primaryKey)" else s"(($primaryKey), $clusteringColumns)"

    if (!primaryKey.isEmpty && !fields.isEmpty) Some(s"($fields, PRIMARY KEY $pkCcolumns)") else None
  }

  protected def getPrimaryKey(schema: StructType,
                              clusteringTime: Option[String],
                              autoCalculateId: Boolean): Seq[String] = {
    val pkfields = if (autoCalculateId) {
      schema.filter(field => field.name == Output.ID)
    } else {
      schema.filter(field => pkConditions(field, clusteringTime))
    }
    pkfields.map(_.name)
  }

  def close(): Unit = {}
}

