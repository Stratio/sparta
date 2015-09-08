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

trait CassandraDAO extends Closeable with Logging {

  val DefaultAnalyzer = "english"
  val DefaultDateFormat = "yyyy/MM/dd"
  val DefaultRefreshSeconds = "1"
  val IndexPrefix = "index_"
  val MaxTableNameLength = 48

  val compactStorage: Option[String] = None

  def keyspace: String

  def keyspaceClass: String

  def replicationFactor: String

  def clusteringPrecisions: Option[Array[String]]

  def indexFields: Option[Array[String]]

  def textIndexFields: Option[Array[String]]

  def textIndexName: String

  def analyzer: String

  def dateFormat: String

  def refreshSeconds: String

  def createKeypace(connector: CassandraConnector): Boolean = doCreateKeyspace(connector)

  def createTables(connector: CassandraConnector,
                   tSchemas: Seq[TableSchema],
                   isAutoCalculateId: Boolean): Boolean =
    doCreateTables(connector, tSchemas, isAutoCalculateId)

  def createIndexes(connector: CassandraConnector,
                    tSchemas: Seq[TableSchema],
                    isAutoCalculateId: Boolean): Boolean =
    doCreateIndexes(connector, tSchemas, isAutoCalculateId)

  def createTextIndexes(connector: CassandraConnector,
                        tSchemas: Seq[TableSchema]): Boolean =
    doCreateTextIndexes(connector, tSchemas)

  protected def doCreateKeyspace(conn: CassandraConnector): Boolean = {
    executeCommand(conn,
      s"CREATE KEYSPACE IF NOT EXISTS $keyspace " +
        s"WITH REPLICATION = {'class': '$keyspaceClass', " +
        s"'replication_factor': $replicationFactor }")
  }

  protected def doCreateTables(conn: CassandraConnector,
                               tSchemas: Seq[TableSchema],
                               isAutoCalculateId: Boolean): Boolean = {
    tSchemas.map(tableSchema =>
      createTable(conn, tableSchema.tableName, tableSchema.schema, tableSchema.timeDimension, isAutoCalculateId))
      .forall(result => result)
  }

  protected def createTable(conn: CassandraConnector,
                            table: String,
                            schema: StructType,
                            clusteringTime: String,
                            isAutoCalculateId: Boolean): Boolean = {
    val tableName = if(table.size > MaxTableNameLength) table.substring(0,MaxTableNameLength) else table
    val schemaPkCloumns: Option[String] = schemaToPkCcolumns(schema, clusteringTime, isAutoCalculateId)
    val compactSt = compactStorage match {
      case None => ""
      case Some(compact) => s" WITH $compact"
    }
    schemaPkCloumns match {
      case None => false
      case Some(pkColumns) => executeCommand(conn,
        s"CREATE TABLE IF NOT EXISTS $keyspace.$tableName $pkColumns $compactSt")
    }
  }

  protected def doCreateIndexes(conn: CassandraConnector,
                                tSchemas: Seq[TableSchema],
                                isAutoCalculateId: Boolean): Boolean = {
    indexFields match {
      case Some(fields) => {
        val seqResults = for {
          tableSchema <- tSchemas
          indexField <- fields
          primaryKey = getPartitionKey(tableSchema.schema, tableSchema.timeDimension, isAutoCalculateId)
          created = if (!primaryKey.contains(indexField)) {
            createIndex(conn, tableSchema.tableName, indexField)
          } else {
            log.info(s"The indexed field: $indexField is part of primary key.")
            false
          }
        } yield created
        seqResults.forall(result => result)
      }
      case None => false
    }
  }

  protected def createIndex(conn: CassandraConnector, tableName: String, field: String): Boolean = {
    val indexName = s"$IndexPrefix${tableName}_$field"
    executeCommand(conn, s"CREATE INDEX IF NOT EXISTS $indexName ON $keyspace.$tableName ($field)")
  }


  protected def executeCommand(conn: CassandraConnector, command: String): Boolean = {
    conn.withSessionDo(session => session.execute(command))
    //TODO check if is correct now all true
    true
  }

  protected def doCreateTextIndexes(conn: CassandraConnector, tSchemas: Seq[TableSchema]): Boolean = {
    textIndexFields match {
      case Some(textFields) => {
        val seqResults = for {
          tableSchema <- tSchemas
          fields = textFields.filter(textField => tableSchema.schema.fieldNames.contains(textField.split(":").head))
          indexName = s"$IndexPrefix${tableSchema.tableName}"
          command = s"CREATE CUSTOM INDEX IF NOT EXISTS $indexName " +
            s"ON $keyspace.${tableSchema.tableName} ($textIndexName) USING 'com.stratio.cassandra.index.RowIndex' " +
            s"WITH OPTIONS = { 'refresh_seconds' : '$refreshSeconds', ${getTextIndexSentence(fields)} }"
          created = executeCommand(conn, command)
        } yield created
        seqResults.forall(result => result)
      }
      case None => false
    }
  }

  protected def getTextIndexSentence(fields: Array[String]): String = {
    val fieldsSentence = fields.map(field => {
      val fieldDescompose = field.split(":")
      val endSentence = fieldDescompose.last match {
        case "text" => s", analyzer : \042$analyzer\042}"
        case "date" => s", pattern : \042$dateFormat\042}"
        case _ => "}"
      }
      s"${fieldDescompose.head.toLowerCase} : {type : \042${fieldDescompose.last}\042 $endSentence"
    }).mkString(",")
    s"'schema' :'{ fields : { $fieldsSentence } }'"
  }

  //scalastyle:off
  protected def dataTypeToCassandraType(dataType: DataType): String = {
    dataType match {
      case StringType => "text"
      case LongType => "int"
      case DoubleType => "double"
      case FloatType => "float"
      case IntegerType => "int"
      case BooleanType => "boolean"
      case MapType(StringType, LongType, _) => "map<text,int>"
      case ArrayType(DoubleType, _) => "set<double>"
      case DateType | TimestampType => "timestamp"
      case _ => "blob"
    }
  }
  //scalastyle:on

  protected def pkConditions(field: StructField, clusteringTime: String): Boolean =
    !field.nullable && field.name != clusteringTime && clusteringPrecisions.forall(!_.contains(field.name))

  protected def clusteringConditions(field: StructField, clusteringTime: String): Boolean =
    !field.nullable && (field.name == clusteringTime || clusteringPrecisions.exists(_.contains(field.name)))

  //scalastyle:off
  protected def schemaToPkCcolumns(schema: StructType,
                                   clusteringTime: String,
                                   isAutoCalculateId: Boolean): Option[String] = {
    val fields = schema.map(field => field.name + " " + dataTypeToCassandraType(field.dataType)).mkString(",")
    val partitionKey = getPartitionKey(schema, clusteringTime, isAutoCalculateId).mkString(",")
    val clusteringColumns =
      schema.filter(field => clusteringConditions(field, clusteringTime)).map(_.name).mkString(",")
    val pkCcolumns = clusteringColumns match {
      case clusteringCol if !clusteringCol.isEmpty && partitionKey.isEmpty => s"($clusteringColumns)"
      case clusteringCol if clusteringCol.isEmpty && !partitionKey.isEmpty => s"($partitionKey)"
      case clusteringCol if !clusteringCol.isEmpty && !partitionKey.isEmpty => s"(($partitionKey), $clusteringColumns)"
    }

    if (!fields.isEmpty && !pkCcolumns.isEmpty) Some(s"($fields, PRIMARY KEY $pkCcolumns)")
    else None
  }
  //scalastyle:on

  protected def getPartitionKey(schema: StructType,
                              clusteringTime: String,
                              isAutoCalculateId: Boolean): Seq[String] = {
    val pkfields = if (isAutoCalculateId) {
      schema.filter(field => field.name == Output.Id)
    } else {
      schema.filter(field => pkConditions(field, clusteringTime))
    }
    pkfields.map(_.name)
  }

  override def close(): Unit = {
  }
}

