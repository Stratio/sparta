/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package org.apache.spark.sql.jdbc

import java.sql.{Connection, PreparedStatement, ResultSet, SQLException, Savepoint, Statement}
import java.util.Locale

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.workflow.enumerators.ConstraintType
import com.stratio.sparta.plugin.enumerations.TransactionTypes
import com.stratio.sparta.plugin.enumerations.TransactionTypes.TxType
import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils._
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

case class TxSaveMode(txType: TxType, failFast: Boolean)

case class TxOneValues(connection: Connection, savePoint: Savepoint, temporalTableName: String)

// scalastyle:off

object SpartaJdbcUtils extends SLF4JLogging {

  lazy val ShowCurrentSchemaSql = "select current_schema();"

  /** Private mutable variables to optimize Streaming process **/

  private val connections = new java.util.concurrent.ConcurrentHashMap[String, Connection]()


  /** PUBLIC METHODS **/

  /**
    * This methods override Spark JdbcUtils methods to optimize the connection and the exists check
    * Returns  tuple of  (exists table, was created in this method)
    */

  def tableExists(connectionProperties: JDBCOptions, dataFrame: DataFrame, outputName: String): (Boolean, Boolean) = {
    synchronized {
      val conn = getConnection(connectionProperties, outputName)
      val exists = spartaTableExists(conn, connectionProperties)
      if (exists) (true, false)
      else createTable(connectionProperties, dataFrame, outputName)
    }
  }

  def createSchemaIfNotExist(connectionProperties: JDBCOptions, name: String,outputName: String): Unit = {

    if(outputName.contains('.')) {
      val schema = outputName.split('.').headOption.foreach{ schema =>
      synchronized {
        val conn = getConnection(connectionProperties, name)
        conn.setAutoCommit(true)

          val sql = s"""CREATE SCHEMA IF NOT EXISTS \"${schema}\""""
          withStatement(conn){ statement =>
            Try(statement.executeUpdate(sql)).recoverWith{
              case NonFatal(e: Exception) =>
                log.error(s"Error creating $schema ${e.getLocalizedMessage}", e)
                Failure(e)
            }
          }
        }
      }
    }else{
      log.warn(s"Schema is not specified")
    }
  }

  def getSchemaAndTableName(tableNameWithSchema: String): (Option[String], String) = {
    val arr = tableNameWithSchema.split('.').slice(0, 2)
    if (arr.length == 2) (Option(arr(0)), arr(1))
    else (None, tableNameWithSchema)
  }

  private[jdbc] def spartaTableExists(conn: Connection, options: JDBCOptions) = {
    val dialect = JdbcDialects.get(options.url)
    val statement = conn.prepareStatement(dialect.getTableExistsQuery(options.table))
    var exists = false
    try {
      statement.executeQuery()
      exists = true
    } catch {
      case e: SQLException if e.getSQLState.equals("42501") => throw e
      case e: SQLException => log.warn(s"Error in table ${options.table} validation, does not exist, will be created. ${e.getMessage}")
    } finally {
      statement.close()
    }
    exists
  }

  def dropTable(connectionProperties: JDBCOptions, outputName: String, tableName: Option[String] = None): Unit = {
    synchronized {
      val conn = getConnection(connectionProperties, outputName)
      conn.setAutoCommit(true)
      val statement = conn.createStatement
      val tableToDrop = tableName.getOrElse(connectionProperties.table)
      Try(statement.executeUpdate(s"DROP TABLE $tableToDrop")) match {
        case Success(_) =>
          log.debug(s"Dropped correctly table $tableToDrop ")
          statement.close()
        case Failure(e) =>
          statement.close()
          log.error(s"Error dropping table $tableToDrop ${e.getLocalizedMessage} and output $outputName", e)
          throw e
      }
    }
  }

  def truncateTable(connectionProperties: JDBCOptions, outputName: String, tableName: Option[String] = None): Unit = {
    synchronized {
      val conn = getConnection(connectionProperties, outputName)
      conn.setAutoCommit(true)
      val statement = conn.createStatement
      val tableToTruncate = tableName.getOrElse(connectionProperties.table)

      if (spartaTableExists(conn, connectionProperties)) {
        Try(statement.executeUpdate(s"TRUNCATE TABLE $tableToTruncate")) match {
          case Success(_) =>
            log.debug(s"Table $tableToTruncate has been properly truncated")
            statement.close()
          case Failure(e) =>
            statement.close()
            log.error(s"Error truncating table $tableToTruncate ${e.getLocalizedMessage} and output $outputName", e)
            throw e
        }
      }
    }
  }

  def createTable(connectionProperties: JDBCOptions, dataFrame: DataFrame, outputName: String): (Boolean, Boolean) = {
    if (dataFrame.schema.fields.nonEmpty) {
      synchronized {
        val tableName = connectionProperties.table
        val conn = getConnection(connectionProperties, outputName)
        conn.setAutoCommit(true)
        val schemaStr = JdbcUtils.schemaString(dataFrame, connectionProperties.url, connectionProperties.createTableColumnTypes)
          .replace("FLOAT8[]", "DOUBLE PRECISION[]")
        val sql = s"CREATE TABLE $tableName ($schemaStr)"
        val statement = conn.createStatement

        Try(statement.executeUpdate(sql)) match {
          case Success(_) =>
            log.debug(s"Created correctly table $tableName and output $outputName")
            statement.close()
            (true, true)
          case Failure(e) =>
            statement.close()
            log.error(s"Error creating table $tableName and output $outputName ${e.getLocalizedMessage}", e)
            throw e
        }
      }
    } else {
      log.debug(s"Empty schema fields on ${connectionProperties.table} and output $outputName")
      (false, false)
    }
  }

  /**
    * Temporal table creation
    *
    * @return Tuple of connection and savepoint for rollback
    */
  def createTemporalTable(connectionProperties: JDBCOptions): (Connection, Savepoint, String) = {
    synchronized {
      val conn = getConnection(connectionProperties, s"${connectionProperties.table}_temporal")
      conn.setAutoCommit(false)
      lazy val savePointTempTable: Savepoint = conn.setSavepoint(s"${connectionProperties.table}_temporal_savepoint")
      val temporalTableName = s"${connectionProperties.table}_tmp_${System.currentTimeMillis()}"
      val sql = s"CREATE table $temporalTableName AS SELECT * FROM ${connectionProperties.table} WHERE 1 = 0"
      val stmt = conn.prepareStatement(sql)
      Try(stmt.execute()) match {
        case Success(_) =>
          log.debug(s"Created correctly temporal table $temporalTableName")
          stmt.close()
          conn.commit()
          (conn, savePointTempTable, temporalTableName)
        case Failure(e) =>
          stmt.close()
          conn.rollback()
          log.error(s"Error creating temporal table $temporalTableName", e)
          throw e
      }
    }
  }

  def createConstraint(connectionProperties: JDBCOptions, outputName: String, uniqueConstraintName: String, uniqueConstraintFieldsSql: String, constraintType: ConstraintType.Value): String = {
    synchronized {
      val connection = getConnection(connectionProperties, s"constraint_${connectionProperties.table}_$outputName")
      connection.setAutoCommit(false)
      val constraintSql = constraintType match {
        case ConstraintType.Unique =>
          s"ALTER TABLE  ${connectionProperties.table} ADD CONSTRAINT $uniqueConstraintName UNIQUE ($uniqueConstraintFieldsSql)"
        case ConstraintType.PrimaryKey =>
          s"ALTER TABLE  ${connectionProperties.table} ADD CONSTRAINT $uniqueConstraintName PRIMARY KEY ($uniqueConstraintFieldsSql)"
      }
      val stmt = connection.prepareStatement(constraintSql)
      Try(stmt.execute()) match {
        case Success(_) =>
          log.debug(s"Created correctly constraint on table ${connectionProperties.table} ")
          stmt.close()
          connection.commit()
          uniqueConstraintName
        case Failure(e) =>
          stmt.close()
          connection.rollback()
          log.error(s"Error creating constraint on table ${connectionProperties.table} ", e)
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

  def saveTable(df: DataFrame, properties: JDBCOptions, outputName: String, txSaveMode: TxSaveMode,
                tempName: Option[String]) {
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
          savePartition(properties, iterator, schema, dialect, batchSize, isolationLevel, outputName, isCaseSensitive, txSaveMode, tempName)
        } match {
          case Success(txFail) =>
            if (txFail)
              throw new Exception(s"Error saving partition on table ${properties.table} and output $outputName")
            else
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
                   outputName: String,
                   txSaveMode: TxSaveMode
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
    val updateFields = schema.fields.filter(field => !searchFields.contains(field.name))
    val updateTypes = schema.fields.zipWithIndex.flatMap { case (field, index) =>
      if (!searchFields.contains(field.name))
        Option(index -> (updateFields.indexOf(field), getJdbcType(field.dataType, dialect).jdbcNullType))
      else None
    }.toMap
    val insert = insertWithExistsSql(properties.table, schema, searchFields, dialect)
    val update = updateSql(properties.table, schema, searchFields, dialect)
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
          upsertPartition(properties, insert, update, iterator, schema, nullTypes, dialect, searchTypes, updateTypes, outputName, txSaveMode)
        } match {
          case Success(txFail) =>
            if (txFail)
              throw new Exception(s"Error in upsert partition on table ${properties.table} and output $outputName")
            else
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

  def getJdbcType(dt: DataType, dialect: JdbcDialect): JdbcType = {
    dialect.getJDBCType(dt).orElse(getCommonJDBCType(dt)).getOrElse(
      throw new IllegalArgumentException(s"can not get JDBC type for ${dt.simpleString}"))
  }

  private def updateSql(
                         table: String,
                         rddSchema: StructType,
                         searchFields: Seq[String],
                         dialect: JdbcDialect
                       ): String = {
    val valuesPlaceholders = rddSchema.fields.filter(field => !searchFields.contains(field.name))
      .map(field => s"${dialect.quoteIdentifier(field.name)} = ?")
      .mkString(", ")
    val wherePlaceholders = searchFields.map(field => s"${dialect.quoteIdentifier(field)} = ?").mkString(" AND ")

    if (valuesPlaceholders.nonEmpty && wherePlaceholders.nonEmpty)
      s"UPDATE $table SET $valuesPlaceholders WHERE $wherePlaceholders"
    else ""
  }

  private def insertWithExistsSql(
                                   table: String,
                                   rddSchema: StructType,
                                   searchFields: Seq[String],
                                   dialect: JdbcDialect
                                 ): String = {
    val columns = rddSchema.fields.map(field => dialect.quoteIdentifier(field.name)).mkString(",")
    val placeholders = rddSchema.fields.map(_ => "?").mkString(",")
    val wherePlaceholders = searchFields.map(field => s"${dialect.quoteIdentifier(field)} = ?").mkString(" AND ")
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
                             isCaseSensitive: Boolean,
                             txSaveMode: TxSaveMode,
                             tempName: Option[String]
                           ): Boolean = {
    val conn = getConnection(properties, outputName)
    val schemaFromDatabase = Try(properties.asProperties.getProperty("schemaFromDatabase")).toOption
    val tableSchema = schemaFromDatabase.flatMap { value =>
      if (Try(value.toBoolean).getOrElse(false))
        JdbcUtils.getSchemaOption(conn, properties)
      else None
    }

    var committed = false
    var txFail = false

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

    try {

      val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE
      if (supportsTransactions) {
        if (conn.getAutoCommit)
          conn.setAutoCommit(false)
        if (conn.getTransactionIsolation != finalIsolationLevel)
          conn.setTransactionIsolation(finalIsolationLevel)
      }

      def insertStmt()(tableName: String) = getInsertStatement(tableName, rddSchema, tableSchema, isCaseSensitive, dialect)

      lazy val insert: String => String = insertStmt()

      val setters = rddSchema.fields.map(f => makeSetter(conn, dialect, f.dataType))
      val nullTypes = rddSchema.fields.map(f => getJdbcType(f.dataType, dialect).jdbcNullType)

      def applySettersStmt(row: Row)(implicit stmt: PreparedStatement) = {
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
      }

      txSaveMode.txType match {

        case TransactionTypes.SINGLE_STATEMENT =>
          implicit lazy val stmt = conn.prepareStatement(insert(properties.table))
          while (iterator.hasNext) {
            try {
              applySettersStmt(iterator.next())
              stmt.execute()
              if (supportsTransactions) conn.commit()
            } catch {
              case e: SQLException =>
                if (supportsTransactions && !conn.getAutoCommit) {
                  log.warn(s"Transaction ${TransactionTypes.SINGLE_STATEMENT} on table ${properties.table} not succeeded, rollback it")
                  conn.rollback()
                }
                if (txSaveMode.failFast)
                  throw e
                else
                  log.error(s"Transaction ${TransactionTypes.SINGLE_STATEMENT} on table ${properties.table} not succeeded", e)
                txFail = true
            }
          }
        case TransactionTypes.STATEMENT =>
          try {
            implicit lazy val stmt = conn.prepareStatement(insert(properties.table))
            var rowCount = 0
            while (iterator.hasNext) {
              applySettersStmt(iterator.next())
              stmt.addBatch()
              rowCount += 1
              if (rowCount % batchSize == 0) {
                stmt.executeBatch()
                rowCount = 0
              }
            }
            if (rowCount > 0)
              stmt.executeBatch()
            if (supportsTransactions) conn.commit()
            committed = true
          } catch {
            case e: SQLException =>
              if (supportsTransactions && !conn.getAutoCommit) {
                log.warn(s"Transaction ${TransactionTypes.STATEMENT} on table ${properties.table} not succeeded, rollback it")
                conn.rollback()
              }
              if (txSaveMode.failFast)
                throw e
              else
                log.error(s"Transaction ${TransactionTypes.STATEMENT} on table ${properties.table} not succeeded", e)
              txFail = true
          }
        case TransactionTypes.ONE_TRANSACTION =>
          try {
            implicit lazy val stmt = conn.prepareStatement(insert(tempName.getOrElse(s"${outputName}_tmp")))
            var rowCount = 0
            while (iterator.hasNext) {
              applySettersStmt(iterator.next())
              stmt.addBatch()
              rowCount += 1
              if (rowCount % batchSize == 0) {
                stmt.executeBatch()
                rowCount = 0
              }
            }
            if (rowCount > 0)
              stmt.executeBatch()

            conn.commit()
          } catch {
            case e: Exception =>
              log.error(s"Transaction ${TransactionTypes.ONE_TRANSACTION} on table ${properties.table} not succeeded, escalate. ${e.getLocalizedMessage}", e)
              throw e
          }
      }
      txFail
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
    }
  }

  private def connectionIsolationConfig(properties: JDBCOptions, outputName: String): (Connection, Int) = {
    val conn = getConnection(properties, outputName)
    val isolationLevel = properties.isolationLevel
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
    (conn, finalIsolationLevel)
  }

  def nativeUpsertPartition(
                             properties: JDBCOptions,
                             upsertSql: String,
                             iterator: Iterator[Row],
                             rddSchema: StructType,
                             nullTypes: Array[Int],
                             dialect: JdbcDialect,
                             numFields: Int,
                             outputName: String,
                             txSaveMode: TxSaveMode
                           ): Boolean = {
    val batchSize = properties.batchSize
    val (conn, finalIsolationLevel) = connectionIsolationConfig(properties, outputName)

    val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE
    var txFail = false

    try {
      if (supportsTransactions) {
        if (conn.getAutoCommit)
          conn.setAutoCommit(false)
        if (conn.getTransactionIsolation != finalIsolationLevel)
          conn.setTransactionIsolation(finalIsolationLevel)
      }
      val upsertStmt = if (upsertSql.nonEmpty) Option(conn.prepareStatement(upsertSql)) else None

      try {
        var updatesCount = 0
        while (iterator.hasNext) {
          val row = iterator.next()
          var i = 0
          while (i < numFields) {
            if (row.isNullAt(i)) {
              upsertStmt.foreach(st => st.setNull(i + 1, nullTypes(i)))
            } else
              addUpsertStatement(
                rddSchema, i, numFields, row, conn, upsertStmt, dialect)
            i = i + 1
          }
          if (upsertStmt.isDefined) {
            upsertStmt.foreach(_.addBatch())
            updatesCount += 1
          }
          if (updatesCount >= batchSize) {
            upsertStmt.foreach(_.executeBatch())
            updatesCount = 0
          }
        }
        if (updatesCount > 0)
          upsertStmt.foreach(_.executeBatch())
      }
      catch {
        case e: SQLException =>
          if (txSaveMode.failFast)
            throw e
          else
            log.error(s"Transaction upsert on table ${properties.table} not succeeded", e)
          txFail = true
      }
      finally {
        upsertStmt.foreach(_.close())
      }
      txFail
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
          } else e.addSuppressed(cause)
        }
        throw e
    } finally {
      try {
        if (supportsTransactions) conn.commit()
      } catch {
        case e: Exception =>
          if (supportsTransactions && !conn.getAutoCommit) {
            log.warn(s"Transaction not succeeded, rollback it. ${e.getLocalizedMessage}")
            conn.rollback()
          }
      }
    }
  }

  private def addUpsertStatement(schema: StructType,
                                 fieldIndex: Int,
                                 numFields: Int,
                                 row: Row,
                                 connection: Connection,
                                 upsertStmt: Option[PreparedStatement],
                                 dialect: JdbcDialect): Unit =
    schema.fields(fieldIndex).dataType match {
      case IntegerType =>
        upsertStmt.foreach(st => st.setInt(fieldIndex + 1, row.getInt(fieldIndex)))
      case LongType =>
        upsertStmt.foreach(st => st.setLong(fieldIndex + 1, row.getLong(fieldIndex)))
      case DoubleType =>
        upsertStmt.foreach(st => st.setDouble(fieldIndex + 1, row.getDouble(fieldIndex)))
      case FloatType =>
        upsertStmt.foreach(st => st.setFloat(fieldIndex + 1, row.getFloat(fieldIndex)))
      case ShortType =>
        upsertStmt.foreach(st => st.setShort(fieldIndex + 1, row.getShort(fieldIndex)))
      case ByteType =>
        upsertStmt.foreach(st => st.setByte(fieldIndex + 1, row.getByte(fieldIndex)))
      case BooleanType =>
        upsertStmt.foreach(st => st.setBoolean(fieldIndex + 1, row.getBoolean(fieldIndex)))
      case StringType =>
        upsertStmt.foreach(st => st.setString(fieldIndex + 1, row.getString(fieldIndex)))
      case BinaryType =>
        upsertStmt.foreach(st => st.setBytes(fieldIndex + 1, row.getAs[Array[Byte]](fieldIndex)))
      case TimestampType =>
        upsertStmt.foreach(st => st.setTimestamp(fieldIndex + 1, row.getAs[java.sql.Timestamp](fieldIndex)))
      case DateType =>
        upsertStmt.foreach(st => st.setDate(fieldIndex + 1, row.getAs[java.sql.Date](fieldIndex)))
      case t: DecimalType =>
        upsertStmt.foreach(st => st.setBigDecimal(fieldIndex + 1, row.getDecimal(fieldIndex)))
      case ArrayType(et, _) =>
        val array = connection.createArrayOf(
          getJdbcType(et, dialect).databaseTypeDefinition.toLowerCase,
          row.getSeq[AnyRef](fieldIndex).toArray)
        upsertStmt.foreach(st => st.setArray(fieldIndex + 1, array))
      case _ => throw new IllegalArgumentException(
        s"can not translate non-null value for field $fieldIndex")
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
                               outputName: String,
                               txSaveMode: TxSaveMode
                             ): Boolean = {
    val batchSize = properties.batchSize
    val (conn, finalIsolationLevel) = connectionIsolationConfig(properties, outputName)

    val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE
    var txFail = false

    try {
      if (supportsTransactions) {
        if (conn.getAutoCommit)
          conn.setAutoCommit(false)
        if (conn.getTransactionIsolation != finalIsolationLevel)
          conn.setTransactionIsolation(finalIsolationLevel)
      }
      val insertStmt = if (insertSql.nonEmpty) Option(conn.prepareStatement(insertSql)) else None
      val updateStmt = if (updateSql.nonEmpty) Option(conn.prepareStatement(updateSql)) else None
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
          if (insertStmt.isDefined) {
            insertStmt.foreach(_.addBatch())
            updatesCount += 1
          }
          if (updateStmt.isDefined) {
            updateStmt.foreach(_.addBatch())
            updatesCount += 1
          }
          if (updatesCount >= batchSize) {
            insertStmt.foreach(_.executeBatch())
            updateStmt.foreach(_.executeBatch())
            updatesCount = 0
          }
        }
        if (updatesCount > 0) {
          insertStmt.foreach(_.executeBatch())
          updateStmt.foreach(_.executeBatch())
        }
      } catch {
        case e: SQLException =>
          if (txSaveMode.failFast)
            throw e
          else
            log.error(s"Transaction upsert on table ${properties.table} not succeeded", e)
          txFail = true
      }
      finally {
        insertStmt.foreach(_.close())
        updateStmt.foreach(_.close())
      }
      txFail
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
          } else e.addSuppressed(cause)
        }
        throw e
    } finally {
      try {
        if (supportsTransactions) conn.commit()
      } catch {
        case e: Exception =>
          if (supportsTransactions && !conn.getAutoCommit) {
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

  private def deleteSql(table: String, rddSchema: StructType, searchFields: Seq[String]): String = {
    val wherePlaceholders = searchFields.map(field => s"$field = ?").mkString(" AND ")
    if (wherePlaceholders.nonEmpty)
      s"DELETE FROM $table WHERE $wherePlaceholders"
    else
      ""
  }

  def deleteTable(
                   df: DataFrame,
                   properties: JDBCOptions,
                   searchFields: Seq[String],
                   outputName: String,
                   txSaveMode: TxSaveMode
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
    val delete = deleteSql(properties.table, schema, searchFields)
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
          deletePartition(properties, delete, iterator, schema, nullTypes, dialect, searchTypes, outputName, txSaveMode)
        } match {
          case Success(txFail) =>
            if (txFail)
              throw new Exception(s"Error in delete partition on table ${properties.table} and output $outputName")
            else
              log.debug(s"Delete partition correctly on table ${properties.table} and output $outputName")
          case Failure(e) =>
            log.error(s"Delete partition with errors on table ${properties.table} and output $outputName." +
              s" Error: ${e.getLocalizedMessage}")
            throw e
        }
      } else log.debug(s"Delete partition with empty rows")
    }
  }

  def deletePartition(
                       properties: JDBCOptions,
                       deleteSql: String,
                       iterator: Iterator[Row],
                       rddSchema: StructType,
                       nullTypes: Array[Int],
                       dialect: JdbcDialect,
                       searchTypes: Map[Int, (Int, Int)],
                       outputName: String,
                       txSaveMode: TxSaveMode
                     ): Boolean = {
    val batchSize = properties.batchSize
    val conn = getConnection(properties, outputName)
    val isolationLevel = properties.isolationLevel
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
    var txFail = false
    try {
      if (supportsTransactions) {
        if (conn.getAutoCommit)
          conn.setAutoCommit(false)
        if (conn.getTransactionIsolation != finalIsolationLevel)
          conn.setTransactionIsolation(finalIsolationLevel)
      }
      val deleteStmt = if (deleteSql.nonEmpty) Option(conn.prepareStatement(deleteSql)) else None
      txSaveMode.txType match {

        case TransactionTypes.SINGLE_STATEMENT =>
          while (iterator.hasNext) {
            try {
              var i = 0
              val row = iterator.next()
              searchTypes.foreach(t => {
                if (row.isNullAt(t._1)) {
                  deleteStmt.foreach(st => st.setNull(i + 1, nullTypes(t._1)))
                } else
                  addDeleteStatement(rddSchema, i, t._1, row, conn, deleteStmt, dialect)
                i = i + 1
              })
              deleteStmt.foreach(_.execute())
              if (supportsTransactions) conn.commit()
            } catch {
              case e: SQLException =>
                if (supportsTransactions && !conn.getAutoCommit) {
                  log.warn(s"Delete ${TransactionTypes.SINGLE_STATEMENT} on table ${properties.table} not succeeded, rollback it")
                  conn.rollback()
                }
                if (txSaveMode.failFast)
                  throw e
                else
                  log.error(s"Delete ${TransactionTypes.SINGLE_STATEMENT} on table ${properties.table} not succeeded", e)
                txFail = true
            }
          }
        case TransactionTypes.STATEMENT =>
          try {
            var updatesCount = 0
            while (iterator.hasNext) {
              var i = 0
              val row = iterator.next()
              searchTypes.foreach(t => {
                if (row.isNullAt(t._1)) {
                  deleteStmt.foreach(st => st.setNull(i + 1, nullTypes(t._1)))
                } else
                  addDeleteStatement(rddSchema, i, t._1, row, conn, deleteStmt, dialect)
                i = i + 1
              })
              if (deleteStmt.isDefined) {
                deleteStmt.foreach(_.addBatch())
                updatesCount += 1
              }
              if (updatesCount >= batchSize) {
                deleteStmt.foreach(_.executeBatch())
                updatesCount = 0
              }
            }
            if (updatesCount > 0)
              deleteStmt.foreach(_.executeBatch())
            if (supportsTransactions) conn.commit()
          } catch {
            case e: SQLException =>
              if (supportsTransactions && !conn.getAutoCommit) {
                log.warn(s"Delete ${TransactionTypes.STATEMENT} on table ${properties.table} not succeeded, rollback it")
                conn.rollback()
              }
              if (txSaveMode.failFast)
                throw e
              else
                log.error(s"Delete ${TransactionTypes.STATEMENT} on table ${properties.table} not succeeded", e)
              txFail = true
          }
      } //match
      txFail
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
    }
  }

  private def addDeleteStatement(schema: StructType,
                                 fieldIndex: Int,
                                 rowIndex: Int,
                                 row: Row,
                                 connection: Connection,
                                 deleteStmt: Option[PreparedStatement],
                                 dialect: JdbcDialect): Unit = {
    schema.fields(rowIndex).dataType match {
      case IntegerType =>
        deleteStmt.foreach(st => st.setInt(fieldIndex + 1, row.getInt(rowIndex)))
      case LongType =>
        deleteStmt.foreach(st => st.setLong(fieldIndex + 1, row.getLong(rowIndex)))
      case DoubleType =>
        deleteStmt.foreach(st => st.setDouble(fieldIndex + 1, row.getDouble(rowIndex)))
      case FloatType =>
        deleteStmt.foreach(st => st.setFloat(fieldIndex + 1, row.getFloat(rowIndex)))
      case ShortType =>
        deleteStmt.foreach(st => st.setShort(fieldIndex + 1, row.getShort(rowIndex)))
      case ByteType =>
        deleteStmt.foreach(st => st.setByte(fieldIndex + 1, row.getByte(rowIndex)))
      case BooleanType =>
        deleteStmt.foreach(st => st.setBoolean(fieldIndex + 1, row.getBoolean(rowIndex)))
      case StringType =>
        deleteStmt.foreach(st => st.setString(fieldIndex + 1, row.getString(rowIndex)))
      case BinaryType =>
        deleteStmt.foreach(st => st.setBytes(fieldIndex + 1, row.getAs[Array[Byte]](rowIndex)))
      case TimestampType =>
        deleteStmt.foreach(st => st.setTimestamp(fieldIndex + 1, row.getAs[java.sql.Timestamp](rowIndex)))
      case DateType =>
        deleteStmt.foreach(st => st.setDate(fieldIndex + 1, row.getAs[java.sql.Date](rowIndex)))
      case t: DecimalType =>
        deleteStmt.foreach(st => st.setBigDecimal(fieldIndex + 1, row.getDecimal(rowIndex)))
      case ArrayType(et, _) =>
        val array = connection.createArrayOf(
          getJdbcType(et, dialect).databaseTypeDefinition.toLowerCase,
          row.getSeq[AnyRef](rowIndex).toArray)
        deleteStmt.foreach(st => st.setArray(fieldIndex + 1, array))
      case _ => throw new IllegalArgumentException(
        s"can not translate non-null value for field $fieldIndex")
    }
  }


  def quoteTable(tableName: String, connection: => Connection): String =
    if (tableName.exists(_.isUpper)) {
      "\"" + inferSchema(tableName, connection) +"\".\""+ inferTable(tableName) + "\""
    } else {
      tableName
    }

  def inferSchema(tableName: String, connection: Connection, dialect: SpartaJDBCDialect = SpartaPostgresDialect): String =
    if (tableName.contains('.')){
      tableName.split('.')(0).replaceAll("^\"|\"$", "")
    } else {
      Try(getDatabaseDefaultSchema(connection).get).recover{
        case NonFatal(exception) =>
          log.warn(s"Default schema not found for table $tableName", exception)
          dialect.defaultSchema
      }.get
    }

  private def getDatabaseDefaultSchema(connection: Connection): Option[String] = {
    connection.setAutoCommit(true)
    val schema: Option[String] =
      withStatement(connection){ statement =>
        withResultSet(statement.executeQuery(ShowCurrentSchemaSql)){resultSet =>
          if (resultSet.next()) {
            Option(resultSet.getString("current_schema"))
          } else {
            None
          }
        }
      }
    schema
  }

  def inferTable(maybeQualifiedTableName: String): String =
    if (maybeQualifiedTableName.contains('.')){
      Try(maybeQualifiedTableName.split('.')(1).replaceAll("^\"|\"$", "")).getOrElse(throw new RuntimeException("Invalid dbtable found:" + maybeQualifiedTableName))
    } else {
      maybeQualifiedTableName
    }

  private def withStatement[T](connection: Connection)(block: Statement => T): T = {
    var statementOpt: Option[Statement] = None
    try {
      val statement = connection.createStatement(); statementOpt = Option(statement)
      block(statement)
    } finally {
      statementOpt.foreach(_.close)
    }
  }

  private def withResultSet[T](resultSet: ResultSet)(block: ResultSet => T): T = {
    try {
      block(resultSet)
    } finally {
      resultSet.close()
    }
  }

}
