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
package com.stratio.sparta.plugin.output.cassandra.dao

import java.io.Closeable

import com.datastax.spark.connector.cql.CassandraConnector
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import org.apache.spark.Logging
import org.apache.spark.sql.types._

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

  def tableVersion: Option[Int]

  def getTableName(table : String) : String = {
    val tableNameCut = if(table.length > MaxTableNameLength - 3) table.substring(0,MaxTableNameLength - 3) else table
    tableVersion match {
      case Some(v) => s"$tableNameCut${Output.Separator}v$v"
      case None => tableNameCut
    }
  }

  def createKeypace(connector: CassandraConnector): Boolean = doCreateKeyspace(connector)

  def createTables(connector: CassandraConnector, tSchemas: Seq[SpartaSchema]): Boolean =
    doCreateTables(connector, tSchemas)

  def createIndexes(connector: CassandraConnector, tSchemas: Seq[SpartaSchema]): Boolean =
    doCreateIndexes(connector, tSchemas)

  def createTextIndexes(connector: CassandraConnector,
                        tSchemas: Seq[SpartaSchema]): Boolean =
    doCreateTextIndexes(connector, tSchemas)

  protected def doCreateKeyspace(conn: CassandraConnector): Boolean = {
    executeCommand(conn,
      s"CREATE KEYSPACE IF NOT EXISTS $keyspace " +
        s"WITH REPLICATION = {'class': '$keyspaceClass', " +
        s"'replication_factor': $replicationFactor }")
  }

  protected def doCreateTables(conn: CassandraConnector,
                               tSchemas: Seq[SpartaSchema]): Boolean = {
    tSchemas.map(tableSchema =>
        createTable(conn,
          tableSchema.tableName,
          tableSchema.schema,
          tableSchema.timeDimension
        )
    ).forall(result => result)
  }

  protected def createTable(conn: CassandraConnector,
                            table: String,
                            schema: StructType,
                            clusteringTime: Option[String]): Boolean = {
    val tableName = getTableName(table)
    val schemaPkCloumns: Option[String] = schemaToPkColumns(schema)
    val compactSt = compactStorage match {
      case None => ""
      case Some(compact) => if (compact.toBoolean) " WITH COMPACT STORAGE" else ""
    }
    schemaPkCloumns match {
      case None => false
      case Some(pkColumns) => executeCommand(conn,
        s"CREATE TABLE IF NOT EXISTS $keyspace.$tableName $pkColumns $compactSt")
    }
  }

  protected def doCreateIndexes(conn: CassandraConnector,
                                tSchemas: Seq[SpartaSchema]): Boolean = {
    indexFields match {
      case Some(fields) => {
        val seqResults = for {
          tableSchema <- tSchemas
          indexField <- fields
          primaryKey = getPartitionKey(tableSchema.schema)
          created = if (!primaryKey.contains(indexField)) {
            createIndex(conn, getTableName(tableSchema.tableName), indexField)
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

  protected def doCreateTextIndexes(conn: CassandraConnector, tSchemas: Seq[SpartaSchema]): Boolean = {
    textIndexFields match {
      case Some(textFields) => {
        val seqResults = for {
          tableSchema <- tSchemas
          tableName = getTableName(tableSchema.tableName)
          fields = textFields.filter(textField => tableSchema.schema.fieldNames.contains(textField.split(":").head))
          indexName = s"$IndexPrefix$tableName"
          command = s"CREATE CUSTOM INDEX IF NOT EXISTS $indexName " +
            s"ON $keyspace.$tableName ($textIndexName) USING 'com.stratio.cassandra.index.RowIndex' " +
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

  protected def pkConditions(field: StructField): Boolean =
    field.metadata.contains(Output.PrimaryKeyMetadataKey) &&
      !field.metadata.contains(Output.TimeDimensionKey) &&
      clusteringPrecisions.forall(!_.contains(field.name))

  protected def clusteringConditions(field: StructField): Boolean =
      field.metadata.contains(Output.TimeDimensionKey) || clusteringPrecisions.exists(_.contains(field.name))

  //scalastyle:off
  protected def schemaToPkColumns(schema: StructType): Option[String] = {
    val fields = schema.map(field => field.name + " " + dataTypeToCassandraType(field.dataType)).mkString(",")
    val partitionKey = getPartitionKey(schema).mkString(",")
    val clusteringColumns = schema.filter(field => clusteringConditions(field)).map(_.name).mkString(",")
    val pkColumns = clusteringColumns match {
      case clusteringCol if !clusteringCol.isEmpty && partitionKey.isEmpty => s"($clusteringColumns)"
      case clusteringCol if clusteringCol.isEmpty && !partitionKey.isEmpty => s"($partitionKey)"
      case clusteringCol if !clusteringCol.isEmpty && !partitionKey.isEmpty => s"(($partitionKey), $clusteringColumns)"
      case _ => ""
    }

    if (!fields.isEmpty && !pkColumns.isEmpty) Some(s"($fields, PRIMARY KEY $pkColumns)")
    else None
  }
  //scalastyle:on

  protected def getPartitionKey(schema: StructType): Seq[String] = {
    val pkfields = schema.filter(field => pkConditions(field))
    pkfields.map(_.name)
  }

  override def close(): Unit = {
  }
}

