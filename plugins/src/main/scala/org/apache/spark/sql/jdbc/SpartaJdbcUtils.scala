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

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils._
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object SpartaJdbcUtils extends SLF4JLogging {

  /** Private mutable variables to optimize Streaming process **/

  private val tablesCreated = new java.util.concurrent.ConcurrentHashMap[String, StructType]()
  private val connections = new java.util.concurrent.ConcurrentHashMap[String, Connection]()

  //scalastyle:off

  /** PUBLIC METHODS **/

  /**
   * This methods override Spark JdbcUtils methods to optimize the connection and the exists check
   *
   */

  def tableExists(url: String, connectionProperties: JDBCOptions, tableName: String, schema: StructType, outputName: String): Boolean = {
    synchronized {
      if (!tablesCreated.containsKey(tableName)) {
        val conn = getConnection(connectionProperties, outputName)
        val exists = JdbcUtils.tableExists(conn, url, tableName)

        if (exists) {
          tablesCreated.put(tableName, schema)
          true
        } else createTable(url, connectionProperties, tableName, schema, outputName)
      } else true
    }
  }

  def dropTable(url: String, connectionProperties: JDBCOptions, tableName: String, outputName: String): Unit = {
    synchronized {
      val conn = getConnection(connectionProperties, outputName)
      conn.setAutoCommit(true)
      val statement = conn.createStatement
      Try(statement.executeUpdate(s"DROP TABLE $tableName")) match {
        case Success(_) =>
          log.debug(s"Dropped correctly table $tableName ")
          statement.close()
          tablesCreated.remove(tableName)
        case Failure(e) =>
          statement.close()
          log.error(s"Error dropping table $tableName ${e.getLocalizedMessage} and output $outputName", e)
      }
    }
  }

  def createTable(url: String, connectionProperties: JDBCOptions, tableName: String, schema: StructType, outputName: String): Boolean = {
    synchronized {
      val conn = getConnection(connectionProperties, outputName)
      conn.setAutoCommit(true)
      val schemaStr = schemaString(schema, url)
      val sql = s"CREATE TABLE $tableName ($schemaStr)"
      val statement = conn.createStatement

      Try(statement.executeUpdate(sql)) match {
        case Success(_) =>
          log.debug(s"Created correctly table $tableName and output $outputName")
          statement.close()
          tablesCreated.put(tableName, schema)
          true
        case Failure(e) =>
          statement.close()
          log.error(s"Error creating table $tableName and output $outputName ${e.getLocalizedMessage}", e)
          false
      }
    }
  }

  def getConnection(properties: JDBCOptions, outputName: String): Connection = {
    synchronized {
      if (!connections.containsKey(outputName) || connections.get(outputName).isClosed) {
        Try(createConnectionFactory(properties)()) match {
          case Success(conn) =>
            if (connections.containsKey(outputName)) {
              connections.get(outputName).close()
              connections.replace(outputName, conn)
            } else connections.put(outputName, conn)
          case Failure(e) =>
            log.error(s"Error creating connection on output $outputName ${e.getLocalizedMessage}", e)
        }
      }
    }
    connections.get(outputName)
  }

  def closeConnection(outputName: String): Unit = {
    synchronized {
      if (connections.containsKey(outputName)) {
        Try(connections.get(outputName).close()) match {
          case Success(_) =>
            connections.remove(outputName)
            log.info("Connection in output $outputName correctly closed")
          case Failure(e) =>
            log.error(s"Error closing connection on output $outputName, ${e.getLocalizedMessage}", e)
        }
      }
    }
  }

  def saveTable(df: DataFrame, url: String, table: String, properties: JDBCOptions, outputName: String) {
    val schema = df.schema
    val dialect = JdbcDialects.get(url)
    val nullTypes: Array[Int] = schema.fields.map { field =>
      getJdbcType(field.dataType, dialect).jdbcNullType
    }
    val batchSize = properties.batchSize
    val isolationLevel = properties.isolationLevel

    df.foreachPartition { iterator =>
      Try(savePartition(properties, table, iterator, schema, nullTypes, dialect, batchSize, isolationLevel, outputName))
      match {
        case Success(_) =>
          log.debug(s"Save partition correctly on table $table and output $outputName")
        case Failure(e) =>
          log.warn(s"Save partition with errors, attempting it creating the table $table in output $outputName and retry to save", e)
          if (tableExists(url, properties, table, schema, outputName))
            savePartition(properties, table, iterator, schema, nullTypes, dialect, batchSize, isolationLevel, outputName)
      }
    }
  }

  def upsertTable(
                   df: DataFrame,
                   url: String,
                   table: String,
                   properties: JDBCOptions,
                   searchFields: Seq[String],
                   outputName: String
                 ): Unit = {
    val schema = df.schema
    val dialect = JdbcDialects.get(url)
    val nullTypes = schema.fields.map { field =>
      getJdbcType(field.dataType, dialect).jdbcNullType
    }
    val searchTypes = schema.fields.zipWithIndex.flatMap { case (field, index) =>
      if (searchFields.contains(field.name))
        Option(index -> (searchFields.indexOf(field.name), getJdbcType(field.dataType, dialect).jdbcNullType))
      else None
    }.toMap
    val insert = insertWithExistsSql(table, schema, searchFields)
    val update = updateSql(table, schema, searchFields)
    val batchSize = properties.batchSize
    val isolationLevel = properties.isolationLevel

    df.foreachPartition { iterator =>
      Try {
        upsertPartition(properties, insert, update, iterator, schema, nullTypes, dialect, searchTypes, batchSize, isolationLevel, outputName)
      } match {
        case Success(_) =>
          log.debug(s"Upsert partition correctly on table $table in output $outputName")
        case Failure(e) =>
          log.warn(s"Upsert partition with errors, attempting it creating the table $table in output $outputName and retry to upsert", e)
          if (tableExists(url, properties, table, schema, outputName))
            upsertPartition(properties, insert, update, iterator, schema, nullTypes, dialect, searchTypes,
              batchSize, isolationLevel, outputName)
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

  // A `JDBCValueSetter` is responsible for setting a value from `Row` into a field for
  // `PreparedStatement`. The last argument `Int` means the index for the value to be set
  // in the SQL statement and also used for the value in `Row`.
  private type JDBCValueSetter = (PreparedStatement, Row, Int) => Unit

  private def makeSetter(
                          conn: Connection,
                          dialect: JdbcDialect,
                          dataType: DataType): JDBCValueSetter = dataType match {
    case IntegerType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setInt(pos + 1, row.getInt(pos))

    case LongType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setLong(pos + 1, row.getLong(pos))

    case DoubleType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setDouble(pos + 1, row.getDouble(pos))

    case FloatType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setFloat(pos + 1, row.getFloat(pos))

    case ShortType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setInt(pos + 1, row.getShort(pos))

    case ByteType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setInt(pos + 1, row.getByte(pos))

    case BooleanType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setBoolean(pos + 1, row.getBoolean(pos))

    case StringType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setString(pos + 1, row.getString(pos))

    case BinaryType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setBytes(pos + 1, row.getAs[Array[Byte]](pos))

    case TimestampType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setTimestamp(pos + 1, row.getAs[java.sql.Timestamp](pos))

    case DateType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setDate(pos + 1, row.getAs[java.sql.Date](pos))

    case t: DecimalType =>
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        stmt.setBigDecimal(pos + 1, row.getDecimal(pos))

    case ArrayType(et, _) =>
      // remove type length parameters from end of type name
      val typeName = getJdbcType(et, dialect).databaseTypeDefinition
        .toLowerCase.split("\\(")(0)
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        val array = conn.createArrayOf(
          typeName,
          row.getSeq[AnyRef](pos).toArray)
        stmt.setArray(pos + 1, array)

    case _ =>
      (_: PreparedStatement, _: Row, pos: Int) =>
        throw new IllegalArgumentException(
          s"Can't translate non-null value for field $pos")
  }

  private def savePartition(
                             properties: JDBCOptions,
                             table: String,
                             iterator: Iterator[Row],
                             rddSchema: StructType,
                             nullTypes: Array[Int],
                             dialect: JdbcDialect,
                             batchSize: Int,
                             isolationLevel: Int,
                             outputName: String
                           ): Unit = {
    val conn = getConnection(properties, outputName)
    var committed = false
    var finalIsolationLevel = Connection.TRANSACTION_NONE
    if (isolationLevel != Connection.TRANSACTION_NONE) {
      try {
        val metadata = conn.getMetaData
        if (metadata.supportsTransactions()) {
          // Update to at least use the default isolation, if any transaction level
          // has been chosen and transactions are supported
          val defaultIsolation = metadata.getDefaultTransactionIsolation
          finalIsolationLevel = defaultIsolation
          if (metadata.supportsTransactionIsolationLevel(isolationLevel)) {
            // Finally update to actually requested level if possible
            finalIsolationLevel = isolationLevel
          } else {
            log.warn(s"Requested isolation level $isolationLevel is not supported; " +
              s"falling back to default isolation level $defaultIsolation")
          }
        } else {
          log.warn(s"Requested isolation level $isolationLevel, but transactions are unsupported")
        }
      } catch {
        case NonFatal(e) => log.warn("Exception while detecting transaction support", e)
      }
    }
    val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE

    try {
      if (supportsTransactions) conn.setAutoCommit(false)
      val stmt = insertStatement(conn, table, rddSchema, dialect)
      val setters: Array[JDBCValueSetter] = rddSchema.fields.map(_.dataType).map(makeSetter(conn, dialect, _))
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
              setters(i).apply(stmt, row, i)
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

  private def upsertPartition(
                               properties: JDBCOptions,
                               insertSql: String,
                               updateSql: String,
                               iterator: Iterator[Row],
                               rddSchema: StructType,
                               nullTypes: Array[Int],
                               dialect: JdbcDialect,
                               searchTypes: Map[Int, (Int, Int)],
                               batchSize: Int,
                               isolationLevel: Int,
                               outputName: String
                             ): Unit = {
    val conn = getConnection(properties, outputName)
    var finalIsolationLevel = Connection.TRANSACTION_NONE
    if (isolationLevel != Connection.TRANSACTION_NONE) {
      try {
        val metadata = conn.getMetaData
        if (metadata.supportsTransactions()) {
          // Update to at least use the default isolation, if any transaction level
          // has been chosen and transactions are supported
          val defaultIsolation = metadata.getDefaultTransactionIsolation
          finalIsolationLevel = defaultIsolation
          if (metadata.supportsTransactionIsolationLevel(isolationLevel)) {
            // Finally update to actually requested level if possible
            finalIsolationLevel = isolationLevel
          } else {
            log.warn(s"Requested isolation level $isolationLevel is not supported; " +
              s"falling back to default isolation level $defaultIsolation")
          }
        } else {
          log.warn(s"Requested isolation level $isolationLevel, but transactions are unsupported")
        }
      } catch {
        case NonFatal(e) => log.warn("Exception while detecting transaction support", e)
      }
    }
    val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE

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
              searchTypes.find { case (index, (_, _)) => index == i }
                .foreach { case (_, (indexSearch, nullType)) =>
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
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setInt(numFields + 1 + indexSearch, row.getInt(fieldIndex))
            updateStmt.setInt(numFields + 1 + indexSearch, row.getInt(fieldIndex))
          }
      case LongType =>
        insertStmt.setLong(fieldIndex + 1, row.getLong(fieldIndex))
        updateStmt.setLong(fieldIndex + 1, row.getLong(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setLong(numFields + 1 + indexSearch, row.getLong(fieldIndex))
            updateStmt.setLong(numFields + 1 + indexSearch, row.getLong(fieldIndex))
          }
      case DoubleType =>
        insertStmt.setDouble(fieldIndex + 1, row.getDouble(fieldIndex))
        updateStmt.setDouble(fieldIndex + 1, row.getDouble(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setDouble(numFields + 1 + indexSearch, row.getDouble(fieldIndex))
            updateStmt.setDouble(numFields + 1 + indexSearch, row.getDouble(fieldIndex))
          }
      case FloatType =>
        insertStmt.setFloat(fieldIndex + 1, row.getFloat(fieldIndex))
        updateStmt.setFloat(fieldIndex + 1, row.getFloat(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setFloat(numFields + 1 + indexSearch, row.getFloat(fieldIndex))
            updateStmt.setFloat(numFields + 1 + indexSearch, row.getFloat(fieldIndex))
          }
      case ShortType =>
        insertStmt.setInt(fieldIndex + 1, row.getShort(fieldIndex))
        updateStmt.setInt(fieldIndex + 1, row.getShort(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setInt(numFields + 1 + indexSearch, row.getShort(fieldIndex))
            updateStmt.setInt(numFields + 1 + indexSearch, row.getShort(fieldIndex))
          }
      case ByteType =>
        insertStmt.setInt(fieldIndex + 1, row.getByte(fieldIndex))
        updateStmt.setInt(fieldIndex + 1, row.getByte(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setInt(numFields + 1 + indexSearch, row.getByte(fieldIndex))
            updateStmt.setInt(numFields + 1 + indexSearch, row.getByte(fieldIndex))
          }
      case BooleanType =>
        insertStmt.setBoolean(fieldIndex + 1, row.getBoolean(fieldIndex))
        updateStmt.setBoolean(fieldIndex + 1, row.getBoolean(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setBoolean(numFields + 1 + indexSearch, row.getBoolean(fieldIndex))
            updateStmt.setBoolean(numFields + 1 + indexSearch, row.getBoolean(fieldIndex))
          }
      case StringType =>
        insertStmt.setString(fieldIndex + 1, row.getString(fieldIndex))
        updateStmt.setString(fieldIndex + 1, row.getString(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setString(numFields + 1 + indexSearch, row.getString(fieldIndex))
            updateStmt.setString(numFields + 1 + indexSearch, row.getString(fieldIndex))
          }
      case BinaryType =>
        insertStmt.setBytes(fieldIndex + 1, row.getAs[Array[Byte]](fieldIndex))
        updateStmt.setBytes(fieldIndex + 1, row.getAs[Array[Byte]](fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setBytes(numFields + 1 + indexSearch, row.getAs[Array[Byte]](fieldIndex))
            updateStmt.setBytes(numFields + 1 + indexSearch, row.getAs[Array[Byte]](fieldIndex))
          }
      case TimestampType =>
        insertStmt.setTimestamp(fieldIndex + 1, row.getAs[java.sql.Timestamp](fieldIndex))
        updateStmt.setTimestamp(fieldIndex + 1, row.getAs[java.sql.Timestamp](fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setTimestamp(numFields + 1 + indexSearch, row.getAs[java.sql.Timestamp](fieldIndex))
            updateStmt.setTimestamp(numFields + 1 + indexSearch, row.getAs[java.sql.Timestamp](fieldIndex))
          }
      case DateType =>
        insertStmt.setDate(fieldIndex + 1, row.getAs[java.sql.Date](fieldIndex))
        updateStmt.setDate(fieldIndex + 1, row.getAs[java.sql.Date](fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setDate(numFields + 1 + indexSearch, row.getAs[java.sql.Date](fieldIndex))
            updateStmt.setDate(numFields + 1 + indexSearch, row.getAs[java.sql.Date](fieldIndex))
          }
      case t: DecimalType =>
        insertStmt.setBigDecimal(fieldIndex + 1, row.getDecimal(fieldIndex))
        updateStmt.setBigDecimal(fieldIndex + 1, row.getDecimal(fieldIndex))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setBigDecimal(numFields + 1 + indexSearch, row.getDecimal(fieldIndex))
            updateStmt.setBigDecimal(numFields + 1 + indexSearch, row.getDecimal(fieldIndex))
          }
      case ArrayType(et, _) =>
        val array = connection.createArrayOf(
          getJdbcType(et, dialect).databaseTypeDefinition.toLowerCase,
          row.getSeq[AnyRef](fieldIndex).toArray)
        insertStmt.setArray(fieldIndex + 1, array)
        updateStmt.setArray(fieldIndex + 1, array)
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.setArray(numFields + 1 + indexSearch, array)
            updateStmt.setArray(numFields + 1 + indexSearch, array)
          }
      case _ => throw new IllegalArgumentException(
        s"Can't translate non-null value for field $fieldIndex")
    }
}
