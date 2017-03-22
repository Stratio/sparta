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

package org.apache.spark.sql.jdbc

import java.sql.{Connection, PreparedStatement}
import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils
import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object SpartaJdbcUtils extends SLF4JLogging {

  /** Private mutable variables to optimize Streaming process **/

  private val tablesCreated = new java.util.concurrent.ConcurrentHashMap[String, StructType]()
  private var connection: Option[Connection] = None

  /** PUBLIC METHODS **/

  /**
   * This methods override Spark JdbcUtils methods to optimize the connection and the exists check
   *
   */

  def tableExists(url: String, connectionProperties: Properties, tableName: String, schema: StructType): Boolean = {
    if (!tablesCreated.contains(tableName)) {
      val conn = getConnection(url, connectionProperties)
      val exists = JdbcUtils.tableExists(conn, url, tableName)

      if (exists) {
        tablesCreated.putIfAbsent(tableName, schema)
        true
      }
      else createTable(url, connectionProperties, tableName, schema)
    } else true
  }

  def dropTable(url: String, connectionProperties: Properties, tableName: String): Unit = {
    val conn = getConnection(url, connectionProperties)
    conn.setAutoCommit(true)
    val statement = conn.createStatement
    Try(statement.executeUpdate(s"DROP TABLE $tableName")) match {
      case Success(_) =>
        log.debug(s"Dropped correctly table $tableName")
        statement.close()
        tablesCreated.remove(tableName)
      case Failure(e) =>
        statement.close()
        log.error(s"Error dropping table $tableName ${e.getLocalizedMessage}", e)
    }
  }

  def createTable(url: String, connectionProperties: Properties, tableName: String, schema: StructType): Boolean = {
    val conn = getConnection(url, connectionProperties)
    conn.setAutoCommit(true)
    val schemaStr = schemaString(schema, url)
    val sql = s"CREATE TABLE $tableName ($schemaStr)"
    val statement = conn.createStatement

    Try(statement.executeUpdate(sql)) match {
      case Success(_) =>
        log.debug(s"Created correctly table $tableName")
        statement.close()
        tablesCreated.putIfAbsent(tableName, schema)
        true
      case Failure(e) =>
        statement.close()
        log.error(s"Error creating table $tableName ${e.getLocalizedMessage}", e)
        false
    }
  }

  def getConnection(url: String, properties: Properties): Connection = {
    synchronized {
      if (connection.isEmpty)
        connection = Try(createConnectionFactory(url, properties)()) match {
          case Success(conn) =>
            Option(conn)
          case Failure(e) =>
            log.error(s"Error creating postgres connection ${e.getLocalizedMessage}")
            None
        }
    }
    connection.getOrElse(throw new IllegalStateException("The connection is empty"))
  }

  def closeConnection(): Unit = {
    synchronized {
      Try(connection.foreach(_.close())) match {
        case Success(_) => log.info("Connection correctly closed")
        case Failure(e) => log.error(s"Error closing connection, ${e.getLocalizedMessage}", e)
      }
      connection = None
    }
  }

  def saveTable(df: DataFrame, url: String, table: String, properties: Properties) {
    val schema = df.schema
    val dialect = JdbcDialects.get(url)
    val nullTypes: Array[Int] = schema.fields.map { field =>
      getJdbcType(field.dataType, dialect).jdbcNullType
    }

    df.foreachPartition { iterator =>
      Try(savePartition(url, properties, table, iterator, schema, nullTypes, dialect)) match {
        case Success(_) =>
          log.debug(s"Save partition correctly on table $table")
        case Failure(e) =>
          log.debug(s"Save partition with errors, attempting it creating the table $table and retry to save", e)
          createTable(url, properties, table, schema)
          savePartition(url, properties, table, iterator, schema, nullTypes, dialect)
      }
    }
  }

  def upsertTable(df: DataFrame,
                  url: String,
                  table: String,
                  properties: Properties,
                  searchFields: Seq[String]): Unit = {
    val schema = df.schema
    val dialect = JdbcDialects.get(url)
    val nullTypes = schema.fields.map { field =>
      getJdbcType(field.dataType, dialect).jdbcNullType
    }
    val searchTypes = schema.fields.zipWithIndex.flatMap { case (field, index) =>
      if (searchFields.contains(field.name))
        Option(index ->(searchFields.indexOf(field.name), getJdbcType(field.dataType, dialect).jdbcNullType))
      else None
    }.toMap
    val insert = insertWithExistsSql(table, schema, searchFields)
    val update = updateSql(table, schema, searchFields)

    df.foreachPartition { iterator =>
      Try(upsertPartition(url, properties, insert, update, iterator, schema, nullTypes, dialect, searchTypes)) match {
        case Success(_) =>
          log.debug(s"Upsert partition correctly on table $table")
        case Failure(e) =>
          log.debug(s"Upsert partition with errors, attempting it creating the table $table and retry to upsert", e)
          createTable(url, properties, table, schema)
          upsertPartition(url, properties, insert, update, iterator, schema, nullTypes, dialect, searchTypes)
      }
    }
  }

  /** PRIVATE METHODS **/

  private def schemaString(schema: StructType, url: String): String = {
    val sb = new StringBuilder()
    val dialect = JdbcDialects.get(url)
    schema.fields foreach { field => {
      val name = field.name
      val typ: String = getJdbcType(field.dataType, dialect).databaseTypeDefinition
      val nullable = if (field.nullable) "" else "NOT NULL"
      sb.append(s", $name $typ $nullable")
    }
    }
    if (sb.length < 2) "" else sb.substring(2)
  }

  private def getJdbcType(dt: DataType, dialect: JdbcDialect): JdbcType = {
    dialect.getJDBCType(dt).orElse(getCommonJDBCType(dt)).getOrElse(
      throw new IllegalArgumentException(s"Can't get JDBC type for ${dt.simpleString}"))
  }

  private def updateSql(table: String, rddSchema: StructType, searchFields: Seq[String]): String = {
    val valuesPlaceholders = rddSchema.fields.map(field => s"${field.name} = ?").mkString(", ")
    val wherePlaceholders = searchFields.map(field => s"$field = ?").mkString(" AND ")
    s"UPDATE $table SET $valuesPlaceholders WHERE $wherePlaceholders"
  }

  private def insertWithExistsSql(table: String, rddSchema: StructType, searchFields: Seq[String]): String = {
    val columns = rddSchema.fields.map(_.name).mkString(",")
    val placeholders = rddSchema.fields.map(_ => "?").mkString(",")
    val wherePlaceholders = searchFields.map(field => s"$field = ?").mkString(" AND ")
    s"INSERT INTO $table ($columns)" +
      s" SELECT $placeholders WHERE NOT EXISTS (SELECT 1 FROM $table WHERE $wherePlaceholders)"
  }

  //scalastyle:off
  private def savePartition(url: String,
                            properties: Properties,
                            table: String,
                            iterator: Iterator[Row],
                            rddSchema: StructType,
                            nullTypes: Array[Int],
                            dialect: JdbcDialect): Unit = {
    val conn = getConnection(url, properties)
    val batchSize = properties.getProperty("batchsize", "1000").toInt
    var committed = false
    val supportsTransactions = try {
      conn.getMetaData.supportsDataManipulationTransactionsOnly() ||
        conn.getMetaData.supportsDataDefinitionAndDataManipulationTransactions()
    } catch {
      case NonFatal(e) =>
        log.warn("Exception while detecting transaction support", e)
        true
    }

    try {
      if (supportsTransactions) conn.setAutoCommit(false)
      val stmt = insertStatement(conn, table, rddSchema)
      try {
        var rowCount = 0
        while (iterator.hasNext) {
          val row = iterator.next()
          val numFields = rddSchema.fields.length
          var i = 0
          while (i < numFields) {
            if (row.isNullAt(i)) {
              stmt.setNull(i + 1, nullTypes(i))
            } else {
              rddSchema.fields(i).dataType match {
                case IntegerType => stmt.setInt(i + 1, row.getInt(i))
                case LongType => stmt.setLong(i + 1, row.getLong(i))
                case DoubleType => stmt.setDouble(i + 1, row.getDouble(i))
                case FloatType => stmt.setFloat(i + 1, row.getFloat(i))
                case ShortType => stmt.setInt(i + 1, row.getShort(i))
                case ByteType => stmt.setInt(i + 1, row.getByte(i))
                case BooleanType => stmt.setBoolean(i + 1, row.getBoolean(i))
                case StringType => stmt.setString(i + 1, row.getString(i))
                case BinaryType => stmt.setBytes(i + 1, row.getAs[Array[Byte]](i))
                case TimestampType => stmt.setTimestamp(i + 1, row.getAs[java.sql.Timestamp](i))
                case DateType => stmt.setDate(i + 1, row.getAs[java.sql.Date](i))
                case t: DecimalType => stmt.setBigDecimal(i + 1, row.getDecimal(i))
                case ArrayType(et, _) =>
                  val array = conn.createArrayOf(
                    getJdbcType(et, dialect).databaseTypeDefinition.toLowerCase,
                    row.getSeq[AnyRef](i).toArray)
                  stmt.setArray(i + 1, array)
                case _ => throw new IllegalArgumentException(
                  s"Can't translate non-null value for field $i")
              }
            }
            i = i + 1
          }
          stmt.addBatch()
          rowCount += 1
          if (rowCount % batchSize == 0) {
            stmt.executeBatch()
            rowCount = 0
          }
        }
        if (rowCount > 0) {
          stmt.executeBatch()
        }
      } finally {
        stmt.close()
      }
      if (supportsTransactions) conn.commit()
      committed = true
    } finally {
      if (!committed) {
        if (supportsTransactions) conn.rollback()
      }
    }
  }

  private def upsertPartition(url: String,
                              properties: Properties,
                              insertSql: String,
                              updateSql: String,
                              iterator: Iterator[Row],
                              rddSchema: StructType,
                              nullTypes: Array[Int],
                              dialect: JdbcDialect,
                              searchTypes: Map[Int, (Int, Int)]): Unit = {
    val conn = getConnection(url, properties)
    val supportsTransactions = try {
      conn.getMetaData.supportsDataManipulationTransactionsOnly() ||
        conn.getMetaData.supportsDataDefinitionAndDataManipulationTransactions()
    } catch {
      case NonFatal(e) =>
        log.warn("Exception while detecting transaction support", e)
        true
    }
    val batchSize = properties.getProperty("batchsize", "1000").toInt
    try {
      if (supportsTransactions) conn.setAutoCommit(false)
      val insertStmt = conn.prepareStatement(insertSql)
      val updateStmt = conn.prepareStatement(updateSql)
      try {
        var updatesCount = 0
        while (iterator.hasNext) {
          val row = iterator.next()
          val numFields = rddSchema.fields.length
          var i = 0
          while (i < numFields) {
            if (row.isNullAt(i)) {
              updateStmt.setNull(i + 1, nullTypes(i))
              insertStmt.setNull(i + 1, nullTypes(i))
              searchTypes.find { case (index, (indexSearch, nullType)) => index == i }
                .foreach { case (index, (indexSearch, nullType)) =>
                  updateStmt.setNull(numFields + 1 + indexSearch, nullType)
                  insertStmt.setNull(numFields + 1 + indexSearch, nullType)
                }
            } else
              addInsertUpdateStatement(rddSchema, i, numFields, row, searchTypes, conn, insertStmt, updateStmt, dialect)
            i = i + 1
          }
          insertStmt.addBatch()
          updateStmt.addBatch()
          updatesCount += 2
          if (updatesCount >= batchSize) {
            insertStmt.executeBatch()
            updateStmt.executeBatch()
            updatesCount = 0
          }
        }
        if (updatesCount > 0) {
          insertStmt.executeBatch()
          updateStmt.executeBatch()
        }
      } finally {
        insertStmt.close()
        updateStmt.close()
      }
    } finally {
      try {
        if (supportsTransactions) conn.commit()
      } catch {
        case e: Exception =>
          if (supportsTransactions) {
            log.warn("Transaction not succeeded, rollback it", e)
            conn.rollback()
          }
      }
    }
  }

  private def addInsertUpdateStatement(schema: StructType,
                                       fieldIndex: Int,
                                       numFields: Int,
                                       row: Row,
                                       searchTypes: Map[Int, (Int, Int)],
                                       connection: Connection,
                                       insertStmt: PreparedStatement,
                                       updateStmt: PreparedStatement,
                                       dialect: JdbcDialect): Unit =
    schema.fields(fieldIndex).dataType match {
      case IntegerType =>
        insertStmt.setInt(fieldIndex + 1, row.getInt(fieldIndex))
        updateStmt.setInt(fieldIndex + 1, row.getInt(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setInt(numFields + 1 + indexSearch, row.getInt(fieldIndex))
            updateStmt.setInt(numFields + 1 + indexSearch, row.getInt(fieldIndex))
          }
      case LongType =>
        insertStmt.setLong(fieldIndex + 1, row.getLong(fieldIndex))
        updateStmt.setLong(fieldIndex + 1, row.getLong(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setLong(numFields + 1 + indexSearch, row.getLong(fieldIndex))
            updateStmt.setLong(numFields + 1 + indexSearch, row.getLong(fieldIndex))
          }
      case DoubleType =>
        insertStmt.setDouble(fieldIndex + 1, row.getDouble(fieldIndex))
        updateStmt.setDouble(fieldIndex + 1, row.getDouble(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setDouble(numFields + 1 + indexSearch, row.getDouble(fieldIndex))
            updateStmt.setDouble(numFields + 1 + indexSearch, row.getDouble(fieldIndex))
          }
      case FloatType =>
        insertStmt.setFloat(fieldIndex + 1, row.getFloat(fieldIndex))
        updateStmt.setFloat(fieldIndex + 1, row.getFloat(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setFloat(numFields + 1 + indexSearch, row.getFloat(fieldIndex))
            updateStmt.setFloat(numFields + 1 + indexSearch, row.getFloat(fieldIndex))
          }
      case ShortType =>
        insertStmt.setInt(fieldIndex + 1, row.getShort(fieldIndex))
        updateStmt.setInt(fieldIndex + 1, row.getShort(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setInt(numFields + 1 + indexSearch, row.getShort(fieldIndex))
            updateStmt.setInt(numFields + 1 + indexSearch, row.getShort(fieldIndex))
          }
      case ByteType =>
        insertStmt.setInt(fieldIndex + 1, row.getByte(fieldIndex))
        updateStmt.setInt(fieldIndex + 1, row.getByte(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setInt(numFields + 1 + indexSearch, row.getByte(fieldIndex))
            updateStmt.setInt(numFields + 1 + indexSearch, row.getByte(fieldIndex))
          }
      case BooleanType =>
        insertStmt.setBoolean(fieldIndex + 1, row.getBoolean(fieldIndex))
        updateStmt.setBoolean(fieldIndex + 1, row.getBoolean(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setBoolean(numFields + 1 + indexSearch, row.getBoolean(fieldIndex))
            updateStmt.setBoolean(numFields + 1 + indexSearch, row.getBoolean(fieldIndex))
          }
      case StringType =>
        insertStmt.setString(fieldIndex + 1, row.getString(fieldIndex))
        updateStmt.setString(fieldIndex + 1, row.getString(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setString(numFields + 1 + indexSearch, row.getString(fieldIndex))
            updateStmt.setString(numFields + 1 + indexSearch, row.getString(fieldIndex))
          }
      case BinaryType =>
        insertStmt.setBytes(fieldIndex + 1, row.getAs[Array[Byte]](fieldIndex))
        updateStmt.setBytes(fieldIndex + 1, row.getAs[Array[Byte]](fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setBytes(numFields + 1 + indexSearch, row.getAs[Array[Byte]](fieldIndex))
            updateStmt.setBytes(numFields + 1 + indexSearch, row.getAs[Array[Byte]](fieldIndex))
          }
      case TimestampType =>
        insertStmt.setTimestamp(fieldIndex + 1, row.getAs[java.sql.Timestamp](fieldIndex))
        updateStmt.setTimestamp(fieldIndex + 1, row.getAs[java.sql.Timestamp](fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setTimestamp(numFields + 1 + indexSearch, row.getAs[java.sql.Timestamp](fieldIndex))
            updateStmt.setTimestamp(numFields + 1 + indexSearch, row.getAs[java.sql.Timestamp](fieldIndex))
          }
      case DateType =>
        insertStmt.setDate(fieldIndex + 1, row.getAs[java.sql.Date](fieldIndex))
        updateStmt.setDate(fieldIndex + 1, row.getAs[java.sql.Date](fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setDate(numFields + 1 + indexSearch, row.getAs[java.sql.Date](fieldIndex))
            updateStmt.setDate(numFields + 1 + indexSearch, row.getAs[java.sql.Date](fieldIndex))
          }
      case t: DecimalType =>
        insertStmt.setBigDecimal(fieldIndex + 1, row.getDecimal(fieldIndex))
        updateStmt.setBigDecimal(fieldIndex + 1, row.getDecimal(fieldIndex))
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setBigDecimal(numFields + 1 + indexSearch, row.getDecimal(fieldIndex))
            updateStmt.setBigDecimal(numFields + 1 + indexSearch, row.getDecimal(fieldIndex))
          }
      case ArrayType(et, _) =>
        val array = connection.createArrayOf(
          getJdbcType(et, dialect).databaseTypeDefinition.toLowerCase,
          row.getSeq[AnyRef](fieldIndex).toArray)
        insertStmt.setArray(fieldIndex + 1, array)
        updateStmt.setArray(fieldIndex + 1, array)
        searchTypes.find { case (index, (indexSearch, nullType)) => index == fieldIndex }
          .foreach { case (index, (indexSearch, nullType)) =>
            insertStmt.setArray(numFields + 1 + indexSearch, array)
            updateStmt.setArray(numFields + 1 + indexSearch, array)
          }
      case _ => throw new IllegalArgumentException(
        s"Can't translate non-null value for field $fieldIndex")
    }
}
