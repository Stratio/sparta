/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.postgres

import java.io.{InputStream, Serializable => JSerializable}
import java.sql.SQLException
import scala.util.{Failure, Success, Try}

import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils._
import org.apache.spark.sql.jdbc._
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection

import com.stratio.sparta.plugin.enumerations.TransactionTypes
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.helper.SecurityHelper._
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.enumerators.SaveModeEnum.SpartaSaveMode
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.enumerators.ConstraintType
import com.stratio.sparta.core.workflow.step.OutputStep

class PostgresOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val url = properties.getString("url", "")
  lazy val delimiter = properties.getString("delimiter", "\t")
  lazy val newLineSubstitution = properties.getString("newLineSubstitution", " ")
  lazy val quotesSubstitution = properties.getString("newQuotesSubstitution", """\b""")
  lazy val encoding = properties.getString("encoding", "UTF8")
  lazy val postgresSaveMode = TransactionTypes.withName(properties.getString("postgresSaveMode", "CopyIn").toUpperCase)
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)
  lazy val failFast = Try(properties.getBoolean("failFast")).getOrElse(false)
  lazy val dropTemporalTableSuccess = Try(properties.getBoolean("dropTemporalTableSuccess")).getOrElse(true)
  lazy val dropTemporalTableFailure = Try(properties.getBoolean("dropTemporalTableFailure")).getOrElse(false)

  val sparkConf = xDSession.conf.getAll
  val securityUri = getDataStoreUri(sparkConf)
  val urlWithSSL = if (tlsEnable) url + securityUri else url

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (url.isEmpty)
      validation = ErrorValidations(valid = false, messages = validation.messages :+ WorkflowValidationMessage(s"the url must be provided", name))
    if (tlsEnable && securityUri.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"when TLS is enabled, the security options inside sparkConf must be filled", name)
      )

    validation
  }

  override def supportedSaveModes: Seq[SpartaSaveMode] =
    Seq(SaveModeEnum.Append, SaveModeEnum.Overwrite, SaveModeEnum.Upsert, SaveModeEnum.Delete)

  //scalastyle:off
  private[postgres] def constraintExists(connectionProperties: JDBCOptions, uniqueConstraintName: String, outputName: String, dialect: JdbcDialect): Boolean = {
    synchronized {
      val conn = getConnection(connectionProperties, outputName)
      var exists = false
      val statement = conn.prepareStatement(s"SELECT true FROM pg_indexes WHERE schemaname != 'pg_catalog' AND tablename = '${connectionProperties.table}' AND indexname = '$uniqueConstraintName'")
      try {
        val rs = statement.executeQuery()
        while (rs.next())
          exists = rs.getBoolean(1)
      } catch {
        case e: SQLException =>
          log.error(s"Unique Constraint $uniqueConstraintName does not exist in table ${connectionProperties.table}, will be created", e)
      } finally {
        statement.close()
      }
      exists
    }
  }

  //scalastyle:off
  private[postgres] def constraintSql(df: DataFrame, properties: JDBCOptions, searchFields: Seq[String], uniqueConstraintName: String, uniqueConstraintFields: String, outputName: String,
                                      isNewTable: Boolean, dialect: JdbcDialect)(placeHolders: String) = {
    val schema = df.schema
    val columns = schema.fields.map(field => dialect.quoteIdentifier(field.name)).mkString(",")

    val valuesPlaceholders = schema.fields.map(field => s"${dialect.quoteIdentifier(field.name)} = EXCLUDED.${dialect.quoteIdentifier(field.name)}").mkString(",")

    if (uniqueConstraintName.nonEmpty) {
      //If is a new table OR constraint does not exists, constraint is created with constraint fields
      val constraintName = if (isNewTable || !constraintExists(properties, uniqueConstraintName, outputName, dialect)) {
        SpartaJdbcUtils.createConstraint(properties, outputName, uniqueConstraintName, uniqueConstraintFields, ConstraintType.Unique)
      } else {
        uniqueConstraintName
      }

      s"INSERT INTO ${properties.table}($columns) $placeHolders ON CONFLICT ON CONSTRAINT $constraintName " +
        s"DO UPDATE SET $valuesPlaceholders"
    } else {
      //If is a new table, writer primaryKey is used for pk index creation, with a random name to avoid failures when upsert will we executed
      if (isNewTable) {
        val constraintFields = searchFields.map(field => dialect.quoteIdentifier(field)).mkString(",")
        SpartaJdbcUtils.createConstraint(properties, outputName, s"pk_${properties.table}_${uniqueConstraintName}_${System.currentTimeMillis()}", constraintFields, ConstraintType.PrimaryKey)
      }
      s"INSERT INTO ${properties.table}($columns) $placeHolders ON CONFLICT (${searchFields.map(field => dialect.quoteIdentifier(field)).mkString(",")}) " +
        s"DO UPDATE SET $valuesPlaceholders"
    }
  }

  //scalastyle:on

  //scalastyle:off
  private def upsert(df: DataFrame, properties: JDBCOptions, searchFields: Seq[String], uniqueConstraintName: String, uniqueConstraintFields: String, outputName: String, txSaveMode: TxSaveMode,
                     isNewTable: Boolean, dialect: JdbcDialect): Unit = {
    //only pk
    val schema = df.schema
    val nullTypes = schema.fields.map { field =>
      SpartaJdbcUtils.getJdbcType(field.dataType, dialect).jdbcNullType
    }
    val placeHolders = s"VALUES(${schema.fields.map(_ => "?").mkString(",")})"
    val upsertSql = constraintSql(df, properties, searchFields, uniqueConstraintName, uniqueConstraintFields, outputName, isNewTable, dialect)(placeHolders)

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
          SpartaJdbcUtils.nativeUpsertPartition(properties, upsertSql, iterator, schema, nullTypes, dialect, schema.fields.length, outputName, txSaveMode)
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

  //scalastyle:on

  //scalastyle:off
  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(url.nonEmpty, "Postgres url must be provided")
    require(!((postgresSaveMode == TransactionTypes.COPYIN || postgresSaveMode == TransactionTypes.ONE_TRANSACTION) && saveMode == SaveModeEnum.Delete),
      s"Writer SaveMode Delete could not be used with Postgres save mode $postgresSaveMode")
    validateSaveMode(saveMode)
    require(saveMode != SaveModeEnum.Ignore, s"Postgres saveMode $saveMode not supported")
    if (dataFrame.schema.fields.nonEmpty) {
      val tableName = getTableNameFromOptions(options)
      val sparkSaveMode = getSparkSaveMode(saveMode)
      val connectionProperties = new JDBCOptions(urlWithSSL,
        tableName,
        propertiesWithCustom.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
      )

      val dialect = JdbcDialects.get(connectionProperties.url)
      Try {
        if (sparkSaveMode == SaveMode.Overwrite)
          SpartaJdbcUtils.truncateTable(connectionProperties, name)

        synchronized {
          SpartaJdbcUtils.tableExists(connectionProperties, dataFrame, name)
        }
      } match {
        case Success((tableExists, isNewTable)) =>
          try {
            if (tableExists) {
              lazy val updatePrimaryKeyFields = getPrimaryKeyOptions(options) match {
                case Some(pk) => pk.split(",").map(_.trim).toSeq
                case None => Seq.empty[String]
              }
              val uniqueConstraintName = getUniqueConstraintNameOptions(options) match {
                case Some(pk) => pk.trim
                case None => ""
              }
              val uniqueConstraintFields = getUniqueConstraintFieldsOptions(options)
              match {
                case Some(pk) => pk.split(",").map(f => dialect.quoteIdentifier(f.trim)).mkString(",").trim
                case None => ""
              }

              val txSaveMode = TxSaveMode(postgresSaveMode, failFast)
              if (saveMode == SaveModeEnum.Delete) {
                require(updatePrimaryKeyFields.nonEmpty, "The primary key fields must be provided")
                require(updatePrimaryKeyFields.forall(dataFrame.schema.fieldNames.contains(_)),
                  "All the primary key fields should be present in the dataFrame schema")
                SpartaJdbcUtils.deleteTable(dataFrame, connectionProperties, updatePrimaryKeyFields, name, txSaveMode)
              } else if (saveMode == SaveModeEnum.Upsert && postgresSaveMode != TransactionTypes.ONE_TRANSACTION) {
                if (uniqueConstraintName.isEmpty && updatePrimaryKeyFields.isEmpty)
                  require(uniqueConstraintName.nonEmpty, "The Unique Constraint Name must be provided")
                else if (uniqueConstraintName.nonEmpty && !constraintExists(connectionProperties, uniqueConstraintName, name, dialect))
                  require(uniqueConstraintFields.nonEmpty, "The Unique Constraint Fields must be provided, because constraint does not exist in database and should be created")
                else if (updatePrimaryKeyFields.nonEmpty) {
                  require(updatePrimaryKeyFields.nonEmpty, "The primary key fields must be provided")
                  require(updatePrimaryKeyFields.forall(dataFrame.schema.fieldNames.contains(_)), "All the primary key fields should be present in the dataFrame schema")
                }
                upsert(dataFrame, connectionProperties, updatePrimaryKeyFields, uniqueConstraintName, uniqueConstraintFields, name, txSaveMode, isNewTable, dialect)
              }
              else if (postgresSaveMode == TransactionTypes.COPYIN) {
                val schemaFieldsCount = dataFrame.schema.fields.length
                dataFrame.foreachPartition { rows =>
                  val conn = getConnection(connectionProperties, name)
                  val cm = new CopyManager(conn.asInstanceOf[BaseConnection])

                  cm.copyIn(
                    s"""COPY $tableName FROM STDIN WITH (NULL 'null', ENCODING '$encoding', FORMAT CSV, DELIMITER E'$delimiter', QUOTE E'$quotesSubstitution')""",
                    rowsToInputStream(rows, schemaFieldsCount)
                  )
                }
              } else {
                val txOne = if (txSaveMode.txType == TransactionTypes.ONE_TRANSACTION) {
                  val tempTable = SpartaJdbcUtils.createTemporalTable(connectionProperties)
                  Some(TxOneValues(tempTable._1, tempTable._2, tempTable._3))
                } else None
                Try {
                  SpartaJdbcUtils.saveTable(dataFrame, connectionProperties, name, txSaveMode, txOne.map(_.temporalTableName))
                } match {
                  //If all partitions were ok, and is oneTx type, drop temp table
                  case Success(_) =>
                    if (txSaveMode.txType == TransactionTypes.ONE_TRANSACTION) {
                      try {
                        val sqlUpsert =
                          if (saveMode == SaveModeEnum.Upsert) {
                            val placeHolders = s" SELECT * FROM ${txOne.map(_.temporalTableName).get} "
                            constraintSql(dataFrame, connectionProperties, updatePrimaryKeyFields, uniqueConstraintName, uniqueConstraintFields, name, isNewTable, dialect)(placeHolders)
                          }
                          else
                            s"INSERT INTO ${connectionProperties.table} SELECT * FROM ${txOne.map(_.temporalTableName).get} ON CONFLICT DO NOTHING"
                        txOne.map(_.connection).get.prepareStatement(sqlUpsert).execute()
                        txOne.map(_.connection).get.commit()
                      } catch {
                        case e: Exception =>
                          if (txSaveMode.txType == TransactionTypes.ONE_TRANSACTION)
                            txOne.map(_.connection).get.rollback(txOne.map(_.savePoint).get)
                          throw e
                      } finally {
                        try {
                          if (txSaveMode.txType == TransactionTypes.ONE_TRANSACTION && dropTemporalTableSuccess)
                            SpartaJdbcUtils.dropTable(connectionProperties, name, Some(txOne.map(_.temporalTableName).get))
                        } catch {
                          case e: Exception =>
                            throw e
                        } finally {
                          closeConnection(s"${connectionProperties.table}_temporal")
                          closeConnection(name)
                        }
                      }
                    }
                  case Failure(e) =>
                    if (txSaveMode.txType == TransactionTypes.ONE_TRANSACTION) {
                      try {
                        txOne.map(_.connection).get.rollback(txOne.map(_.savePoint).get)
                        if (dropTemporalTableFailure)
                          SpartaJdbcUtils.dropTable(connectionProperties, name, Some(txOne.map(_.temporalTableName).get))
                      } catch {
                        case e: Exception =>
                          throw e
                      } finally {
                        closeConnection(s"${connectionProperties.table}_temporal")
                      }
                    } else closeConnection(name)
                    throw e
                }
              }
            } else log.debug(s"Table not created in Postgres: $tableName")
          } catch {
            case e: Exception =>
              closeConnection(name)
              log.error(s"Error saving data into Postgres table $tableName with Error: ${e.getLocalizedMessage}")
              throw e
          } finally {
            closeConnection(name)
          }
        case Failure(e) =>
          closeConnection(name)
          log.error(s"Error creating/dropping table $tableName with Error: ${e.getLocalizedMessage}")
          throw e
      }
    }
  }

  //scalastyle:on

  def rowsToInputStream(rows: Iterator[Row], fieldsCount: Int): InputStream = {
    val bytes: Iterator[Byte] = rows.flatMap { row =>
      val text = (row.mkString(delimiter).replace("\n", newLineSubstitution) + "\n")
      if (text.split(delimiter).length != fieldsCount)
        throw new RuntimeException(s"Row [$text] contains selected delimiter $delimiter in one or more fields")
      text.getBytes(encoding)
    }

    new InputStream {
      override def read(): Int =
        if (bytes.hasNext) bytes.next & 0xff
        else -1
    }
  }

  override def cleanUp(options: Map[String, String]): Unit = {
    log.info(s"Closing connections in Postgres Output: $name")
    closeConnection(name)
  }
}

object PostgresOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}