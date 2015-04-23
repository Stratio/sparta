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
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf
import org.apache.spark.sql.types._

import com.stratio.sparkta.sdk.TableSchema

trait AbstractCassandraDAO extends Closeable {

  def connectionSeeds : String
  def keyspace : String
  def keyspaceClass : String
  def replicationFactor : String
  def compactStorage : Option[String] = None
  def analyzer : String
  def clusteringBuckets : Array[String]
  def textIndexFields : Array[String]
  def timestampFieldName : String = "eventTime"
  def textIndexFieldsName : String = "lucene"
  def fieldsSeparator : String = ","
  def sparkConf : Option[SparkConf] = None
  def tablesCreated : Boolean
  def keyspaceCreated : Boolean

  protected def connector: Option[CassandraConnector] = sparkConf.map(CassandraConnector(_))

  def createKeypace : Boolean = {
    connector match {
      case None => false
      case Some(conn) => {
        conn.withSessionDo(session => session.execute(
          s"CREATE KEYSPACE IF NOT EXISTS $keyspace " +
            s"WITH REPLICATION = {'class': '$keyspaceClass', " +
            s"'replication_factor': $replicationFactor }"))
        true
      }
    }
  }

  def createTables(tSchemas : Seq[TableSchema],
                             keyName : String,
                             timeBucket : String,
                             granularity : String) : Boolean = {
      val clusteringField = fixedTimeField(timeBucket, granularity)
      filterByFixedTime(tSchemas, keyName, clusteringField)
        .map(tableSchema => createTable(tableSchema.tableName, tableSchema.schema, clusteringField))
        .reduce((a, b) => (if (!a || !b) false else true))
  }

  protected def executeCommand(conn : CassandraConnector, command : String) : Boolean = {
    conn.withSessionDo(session => session.execute(command))
    //TODO check if is correct now all true
    true
  }

  protected def createTable(table : String, schema : StructType, clusteringField : Option[String]) : Boolean = {
    connector match {
      case None => false
      case Some(conn) => {
        val schemaPkCloumns: Option[String] = schemaToFieldsPkCcolumns(schema, clusteringField)
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
    }
  }

  protected def dataTypeToCassandraType(dataType : DataType): String = {
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

  protected def pkConditions(field : StructField, clusteringTime : Option[String]) : Boolean =
    !field.nullable && clusteringTime.forall(field.name != _) && !clusteringBuckets.contains(field.name)

  protected def clusteringConditions(field : StructField, clusteringTime : Option[String]) : Boolean =
    !field.nullable && (clusteringTime.exists(field.name == _) || clusteringBuckets.contains(field.name))

  protected def schemaToFieldsPkCcolumns(schema : StructType, clusteringTime : Option[String]) : Option[String] = {
    val fields = schema.map(field => field.name + " " + dataTypeToCassandraType(field.dataType)).mkString(",")
    val primaryKey = schema.filter(field => pkConditions(field, clusteringTime)).map(_.name).mkString (",")
    val clusteringColumns =
      schema.filter(field => clusteringConditions(field, clusteringTime)).map(_.name).mkString (",")
    val pkClustering = if(clusteringColumns.isEmpty) s"($primaryKey)" else s"(($primaryKey), $clusteringColumns)"

    if(!primaryKey.isEmpty && !fields.isEmpty) Some(s"($fields, PRIMARY KEY $pkClustering)") else None
  }

  protected def fixedTimeField(timeBucket : String, granularity : String): Option[String] =
    if (!timeBucket.isEmpty && !granularity.isEmpty) Some (timeBucket) else None

  protected def filterByFixedTime(tSchemas : Seq[TableSchema],
                        keyName : String,
                        fixedField : Option[String]): Seq[TableSchema] =
    tSchemas.filter(schemaFilter => schemaFilter.outputName == keyName &&
      fixedField.forall(schemaFilter.schema.fieldNames.contains(_) &&
        schemaFilter.schema.filter(!_.nullable).length > 1))

  def close(): Unit = {}

}

