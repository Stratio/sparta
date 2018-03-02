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

import java.sql.{Connection, PreparedStatement, SQLException}
import java.util.Locale

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

  def tableExists(connectionProperties: JDBCOptions, dataFrame: DataFrame, outputName: String): Boolean = {
    synchronized {
      if (!tablesCreated.containsKey(connectionProperties.table)) {
        val conn = getConnection(connectionProperties, outputName)
        val exists = JdbcUtils.tableExists(conn, connectionProperties)

        if (exists) {
          tablesCreated.put(connectionProperties.table, dataFrame.schema)
          true
        } else createTable(connectionProperties, dataFrame, outputName)
      } else true
    }
  }

  def dropTable(connectionProperties: JDBCOptions, outputName: String): Unit = {
    synchronized {
      val conn = getConnection(connectionProperties, outputName)
      conn.setAutoCommit(true)
      val statement = conn.createStatement
      Try(statement.executeUpdate(s"DROP TABLE ${connectionProperties.table}")) match {
        case Success(_) =>
          log.debug(s"Dropped correctly table ${connectionProperties.table} ")
          statement.close()
          tablesCreated.remove(connectionProperties.table)
        case Failure(e) =>
          statement.close()
          log.error(s"Error dropping table ${connectionProperties.table} ${e.getLocalizedMessage} and output $outputName", e)
          throw e
      }
    }
  }

  def createTable(connectionProperties: JDBCOptions, dataFrame: DataFrame, outputName: String): Boolean = {
    synchronized {
      val tableName = connectionProperties.table
      val conn = getConnection(connectionProperties, outputName)
      conn.setAutoCommit(true)
      val schemaStr = JdbcUtils.schemaString(dataFrame, connectionProperties.url, connectionProperties.createTableColumnTypes)
      val sql = s"CREATE TABLE $tableName ($schemaStr)"
      val statement = conn.createStatement

      Try(statement.executeUpdate(sql)) match {
        case Success(_) =>
          log.debug(s"Created correctly table $tableName and output $outputName")
          statement.close()
          tablesCreated.put(tableName, dataFrame.schema)
          true
        case Failure(e) =>
          statement.close()
          log.error(s"Error creating table $tableName and output $outputName ${e.getLocalizedMessage}", e)
          throw e
      }
    }
  }

  def getConnection(properties: JDBCOptions, outputName: String): Connection = {
    synchronized {
      if (!connections.containsKey(outputName) || connections.get(outputName).isClosed) {
        log.debug(s"Connecting to database with url: ${properties.url}")
        Try(Option(createConnectionFactory(properties)())) match {
          case Success(Some(conn)) =>
            if (connections.containsKey(outputName)) {
              connections.get(outputName).close()
              connections.replace(outputName, conn)
            } else connections.put(outputName, conn)
          case Success(None) =>
            val information = s"Error creating connection on output $outputName see logs for full message"
            log.error(information)
            throw new Exception(information)
          case Failure(e) =>
            log.error(s"Error creating connection on output $outputName ${e.getLocalizedMessage}", e)
            throw e
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

  def saveTable(df: DataFrame, properties: JDBCOptions, outputName: String) {
    val schema = df.schema
    val dialect = JdbcDialects.get(properties.url)
    val batchSize = properties.batchSize
    val isolationLevel = properties.isolationLevel
    val isCaseSensitive = df.sqlContext.conf.caseSensitiveAnalysis
    val repartitionedDF = properties.numPartitions match {
      case Some(n) if n <= 0 => throw new IllegalArgumentException(
        s"Invalid value `$n` for parameter `${JDBCOptions.JDBC_NUM_PARTITIONS}` in table writing " +
          "via JDBC. The minimum value is 1.")
      case Some(n) if n < df.rdd.getNumPartitions => df.coalesce(n)
      case _ => df
    }
    repartitionedDF.foreachPartition { iterator =>
      if (iterator.hasNext) {
        Try {
          savePartition(properties, iterator, schema, dialect, batchSize, isolationLevel, outputName, isCaseSensitive)
        } match {
          case Success(_) =>
            log.debug(s"Save partition correctly on table ${properties.table} and output $outputName")
          case Failure(e) =>
            log.error(s"Save partition with errors on table ${properties.table} and output $outputName." +
              s"Error: ${e.getLocalizedMessage}")
            throw e
        }
      } else log.debug(s"Save partition with empty rows")
    }
  }

  def upsertTable(
                   df: DataFrame,
                   properties: JDBCOptions,
                   searchFields: Seq[String],
                   outputName: String
                 ): Unit = {
    val schema = df.schema
    val dialect = JdbcDialects.get(properties.url)
    val nullTypes = schema.fields.map { field =>
      getJdbcType(field.dataType, dialect).jdbcNullType
    }
    val searchTypes = schema.fields.zipWithIndex.flatMap { case (field, index) =>
      if (searchFields.contains(field.name))
        Option(index -> (searchFields.indexOf(field.name), getJdbcType(field.dataType, dialect).jdbcNullType))
      else None
    }.toMap
    val updateTypes = schema.fields.zipWithIndex.flatMap { case (field, index) =>
      if (!searchFields.contains(field.name))
        Option(index -> getJdbcType(field.dataType, dialect).jdbcNullType)
      else None
    }.toMap.zipWithIndex.map { case ((index, nullType), updateIndex) => index -> (updateIndex, nullType) }
    val insert = insertWithExistsSql(properties.table, schema, searchFields)
    val update = updateSql(properties.table, schema, searchFields)
    val repartitionedDF = properties.numPartitions match {
      case Some(n) if n <= 0 => throw new IllegalArgumentException(
        s"Invalid value `$n` for parameter `${JDBCOptions.JDBC_NUM_PARTITIONS}` in table writing " +
          "via JDBC. The minimum value is 1.")
      case Some(n) if n < df.rdd.getNumPartitions => df.coalesce(n)
      case _ => df
    }

    repartitionedDF.foreachPartition { iterator =>
      if (iterator.hasNext) {
        Try {
          upsertPartition(properties, insert, update, iterator, schema, nullTypes, dialect, searchTypes, updateTypes, outputName)
        } match {
          case Success(_) =>
            log.debug(s"Upsert partition correctly on table ${properties.table} and output $outputName")
          case Failure(e) =>
            log.error(s"Upsert partition with errors on table ${properties.table} and output $outputName." +
              s" Error: ${e.getLocalizedMessage}")
            throw e
        }
      } else log.debug(s"Upsert partition with empty rows")
    }
  }

  /** PRIVATE METHODS **/

  private def getJdbcType(dt: DataType, dialect: JdbcDialect): JdbcType = {
    dialect.getJDBCType(dt).orElse(getCommonJDBCType(dt)).getOrElse(
      throw new IllegalArgumentException(s"can not get JDBC type for ${dt.simpleString}"))
  }

  private def updateSql(table: String, rddSchema: StructType, searchFields: Seq[String]): String = {
    val valuesPlaceholders = rddSchema.fields.filter(field => !searchFields.contains(field.name))
      .map(field => s"${field.name} = ?")
      .mkString(", ")
    val wherePlaceholders = searchFields.map(field => s"$field = ?").mkString(" AND ")

    if(valuesPlaceholders.nonEmpty && wherePlaceholders.nonEmpty)
      s"UPDATE $table SET $valuesPlaceholders WHERE $wherePlaceholders"
    else ""
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
        .toLowerCase(Locale.ROOT).split("\\(")(0)
      (stmt: PreparedStatement, row: Row, pos: Int) =>
        val array = conn.createArrayOf(
          typeName,
          row.getSeq[AnyRef](pos).toArray)
        stmt.setArray(pos + 1, array)

    case _ =>
      (_: PreparedStatement, _: Row, pos: Int) =>
        throw new IllegalArgumentException(
          s"can not translate non-null value for field $pos")
  }

  private def savePartition(
                             properties: JDBCOptions,
                             iterator: Iterator[Row],
                             rddSchema: StructType,
                             dialect: JdbcDialect,
                             batchSize: Int,
                             isolationLevel: Int,
                             outputName: String,
                             isCaseSensitive: Boolean
                           ): Iterator[Byte] = {
    val conn = getConnection(properties, outputName)
    val schemaFromDatabase = Try(properties.asProperties.getProperty("schemaFromDatabase")).toOption
    val tableSchema = schemaFromDatabase.flatMap { value =>
        if(Try(value.toBoolean).getOrElse(false))
          JdbcUtils.getSchemaOption(conn, properties)
        else None
    }
    val insertStmt = getInsertStatement(properties.table, rddSchema, tableSchema, isCaseSensitive, dialect)
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
        case NonFatal(e) => log.warn(s"Exception while detecting transaction support. ${e.getLocalizedMessage}")
      }
    }
    val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE

    try {
      if (supportsTransactions){
        if(conn.getAutoCommit)
          conn.setAutoCommit(false)
        if(conn.getTransactionIsolation != finalIsolationLevel)
          conn.setTransactionIsolation(finalIsolationLevel)
      }
      val stmt = conn.prepareStatement(insertStmt)
      val setters = rddSchema.fields.map(f => makeSetter(conn, dialect, f.dataType))
      val nullTypes = rddSchema.fields.map(f => getJdbcType(f.dataType, dialect).jdbcNullType)
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
      Iterator.empty
    } catch {
      case e: SQLException =>
        val cause = e.getNextException
        if (cause != null && e.getCause != cause) {
          // If there is no cause already, set 'next exception' as cause. If cause is null,
          // it *may* be because no cause was set yet
          if (e.getCause == null) {
            try {
              e.initCause(cause)
            } catch {
              // Or it may be null because the cause *was* explicitly initialized, to *null*,
              // in which case this fails. There is no other way to detect it.
              // addSuppressed in this case as well.
              case _: IllegalStateException => e.addSuppressed(cause)
            }
          } else {
            e.addSuppressed(cause)
          }
        }
        throw e
    } finally {
      if (!committed) {
        if (supportsTransactions && !conn.getAutoCommit) {
          log.warn(s"Transaction not succeeded, rollback it")
          conn.rollback()
        }
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
                               updateTypes: Map[Int, (Int, Int)],
                               outputName: String
                             ): Unit = {
    val conn = getConnection(properties, outputName)
    val isolationLevel = properties.isolationLevel
    val batchSize = properties.batchSize
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
        case NonFatal(e) => log.warn(s"Exception while detecting transaction support. ${e.getLocalizedMessage}")
      }
    }
    val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE

    try {
      if (supportsTransactions){
        if(conn.getAutoCommit)
          conn.setAutoCommit(false)
        if(conn.getTransactionIsolation != finalIsolationLevel)
          conn.setTransactionIsolation(finalIsolationLevel)
      }
      val insertStmt = if(insertSql.nonEmpty) Option(conn.prepareStatement(insertSql)) else None
      val updateStmt = if(updateSql.nonEmpty) Option(conn.prepareStatement(updateSql)) else None
      try {
        var updatesCount = 0
        while (iterator.hasNext) {
          val row = iterator.next()
          val numFields = rddSchema.fields.length
          var i = 0
          while (i < numFields) {
            if (row.isNullAt(i)) {
              insertStmt.foreach(st => st.setNull(i + 1, nullTypes(i)))
              searchTypes.find { case (index, (_, _)) => index == i }
                .foreach { case (_, (indexSearch, nullType)) =>
                  updateStmt.foreach(st => st.setNull(updateTypes.size + 1 + indexSearch, nullType))
                  insertStmt.foreach(st => st.setNull(numFields + 1 + indexSearch, nullType))
                }
              updateTypes.find { case (index, (_, _)) => index == i }
                .foreach { case (_, (indexSearch, nullType)) =>
                  updateStmt.foreach(st => st.setNull(indexSearch + 1, nullType))
                }
            } else
              addInsertUpdateStatement(
                rddSchema, i, numFields, row, searchTypes, updateTypes, conn, insertStmt, updateStmt, dialect)
            i = i + 1
          }
          if(insertStmt.isDefined) {
            updatesCount += 1
            insertStmt.foreach(_.addBatch())
          }
          if(updateStmt.isDefined) {
            insertStmt.foreach(_.addBatch())
            updatesCount += 1
          }
          if (updatesCount >= batchSize) {
            if(insertStmt.isDefined)
              insertStmt.foreach(_.executeBatch())
            if(updateStmt.isDefined)
              updateStmt.foreach(_.executeBatch())
            updatesCount = 0
          }
        }
        if (updatesCount > 0) {
          if(insertStmt.isDefined)
            insertStmt.foreach(_.executeBatch())
          if(updateStmt.isDefined)
            updateStmt.foreach(_.executeBatch())
        }
      } finally {
        updateStmt.foreach(_.close())
        updateStmt.foreach(_.close())
      }
    } finally {
      try {
        if (supportsTransactions) conn.commit()
      } catch {
        case e: Exception =>
          if (supportsTransactions && !conn.getAutoCommit){
            log.warn(s"Transaction not succeeded, rollback it. ${e.getLocalizedMessage}")
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
                                       updateTypes: Map[Int, (Int, Int)],
                                       connection: Connection,
                                       insertStmt: Option[PreparedStatement],
                                       updateStmt: Option[PreparedStatement],
                                       dialect: JdbcDialect): Unit =
    schema.fields(fieldIndex).dataType match {
      case IntegerType =>
        insertStmt.foreach(st => st.setInt(fieldIndex + 1, row.getInt(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setInt(numFields + 1 + indexSearch, row.getInt(fieldIndex)))
            updateStmt.foreach(st => st.setInt(updateTypes.size + 1 + indexSearch, row.getInt(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setInt(indexSearch + 1, row.getInt(fieldIndex)))
          }
      case LongType =>
        insertStmt.foreach(st => st.setLong(fieldIndex + 1, row.getLong(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setLong(numFields + 1 + indexSearch, row.getLong(fieldIndex)))
            updateStmt.foreach(st => st.setLong(updateTypes.size + 1 + indexSearch, row.getLong(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setLong(indexSearch + 1, row.getLong(fieldIndex)))
          }
      case DoubleType =>
        insertStmt.foreach(st => st.setDouble(fieldIndex + 1, row.getDouble(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setDouble(numFields + 1 + indexSearch, row.getDouble(fieldIndex)))
            updateStmt.foreach(st => st.setDouble(updateTypes.size + 1 + indexSearch, row.getDouble(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setDouble(indexSearch + 1, row.getDouble(fieldIndex)))
          }
      case FloatType =>
        insertStmt.foreach(st => st.setFloat(fieldIndex + 1, row.getFloat(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setFloat(numFields + 1 + indexSearch, row.getFloat(fieldIndex)))
            updateStmt.foreach(st => st.setFloat(updateTypes.size + 1 + indexSearch, row.getFloat(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setFloat(indexSearch + 1, row.getFloat(fieldIndex)))
          }
      case ShortType =>
        insertStmt.foreach(st => st.setShort(fieldIndex + 1, row.getShort(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setShort(numFields + 1 + indexSearch, row.getShort(fieldIndex)))
            updateStmt.foreach(st => st.setShort(updateTypes.size + 1 + indexSearch, row.getShort(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setShort(indexSearch + 1, row.getShort(fieldIndex)))
          }
      case ByteType =>
        insertStmt.foreach(st => st.setByte(fieldIndex + 1, row.getByte(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setByte(numFields + 1 + indexSearch, row.getByte(fieldIndex)))
            updateStmt.foreach(st => st.setByte(updateTypes.size + 1 + indexSearch, row.getByte(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setByte(indexSearch + 1, row.getByte(fieldIndex)))
          }
      case BooleanType =>
        insertStmt.foreach(st => st.setBoolean(fieldIndex + 1, row.getBoolean(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setBoolean(numFields + 1 + indexSearch, row.getBoolean(fieldIndex)))
            updateStmt.foreach(st => st.setBoolean(updateTypes.size + 1 + indexSearch, row.getBoolean(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setBoolean(indexSearch + 1, row.getBoolean(fieldIndex)))
          }
      case StringType =>
        insertStmt.foreach(st => st.setString(fieldIndex + 1, row.getString(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setString(numFields + 1 + indexSearch, row.getString(fieldIndex)))
            updateStmt.foreach(st => st.setString(updateTypes.size + 1 + indexSearch, row.getString(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setString(indexSearch + 1, row.getString(fieldIndex)))
          }
      case BinaryType =>
        insertStmt.foreach(st => st.setBytes(fieldIndex + 1, row.getAs[Array[Byte]](fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setBytes(numFields + 1 + indexSearch, row.getAs[Array[Byte]](fieldIndex)))
            updateStmt.foreach(st => st.setBytes(updateTypes.size + 1 + indexSearch, row.getAs[Array[Byte]](fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setBytes(indexSearch + 1, row.getAs[Array[Byte]](fieldIndex)))
          }
      case TimestampType =>
        insertStmt.foreach(st => st.setTimestamp(fieldIndex + 1, row.getAs[java.sql.Timestamp](fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setTimestamp(numFields + 1 + indexSearch, row.getAs[java.sql.Timestamp](fieldIndex)))
            updateStmt.foreach(st => st.setTimestamp(updateTypes.size + 1 + indexSearch, row.getAs[java.sql.Timestamp](fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setTimestamp(indexSearch + 1, row.getAs[java.sql.Timestamp](fieldIndex)))
          }
      case DateType =>
        insertStmt.foreach(st => st.setDate(fieldIndex + 1, row.getAs[java.sql.Date](fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setDate(numFields + 1 + indexSearch, row.getAs[java.sql.Date](fieldIndex)))
            updateStmt.foreach(st => st.setDate(updateTypes.size + 1 + indexSearch, row.getAs[java.sql.Date](fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setDate(indexSearch + 1, row.getAs[java.sql.Date](fieldIndex)))
          }
      case t: DecimalType =>
        insertStmt.foreach(st => st.setBigDecimal(fieldIndex + 1, row.getDecimal(fieldIndex)))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setBigDecimal(numFields + 1 + indexSearch, row.getDecimal(fieldIndex)))
            updateStmt.foreach(st => st.setBigDecimal(updateTypes.size + 1 + indexSearch, row.getDecimal(fieldIndex)))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setBigDecimal(indexSearch + 1, row.getDecimal(fieldIndex)))
          }
      case ArrayType(et, _) =>
        val array = connection.createArrayOf(
          getJdbcType(et, dialect).databaseTypeDefinition.toLowerCase,
          row.getSeq[AnyRef](fieldIndex).toArray)
        insertStmt.foreach(st => st.setArray(fieldIndex + 1, array))
        searchTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            insertStmt.foreach(st => st.setArray(numFields + 1 + indexSearch, array))
            updateStmt.foreach(st => st.setArray(updateTypes.size + 1 + indexSearch, array))
          }
        updateTypes.find { case (index, (_, _)) => index == fieldIndex }
          .foreach { case (_, (indexSearch, _)) =>
            updateStmt.foreach(st => st.setArray(indexSearch + 1, array))
          }
      case _ => throw new IllegalArgumentException(
        s"can not translate non-null value for field $fieldIndex")
    }
}
