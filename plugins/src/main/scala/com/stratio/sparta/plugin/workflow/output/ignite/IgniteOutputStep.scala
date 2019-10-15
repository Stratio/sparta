/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.ignite

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ErrorValidations
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.enumerations.TransactionTypes
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.helper.SecurityHelper._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}
import org.apache.spark.sql.jdbc.SpartaJdbcUtils._
import org.apache.spark.sql.jdbc._
import com.stratio.sparta.core.utils.Utils._
import org.apache.spark.sql.types.StructType

import scala.util.{Failure, Success, Try}

class IgniteOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val url = properties.getString("host")
  lazy val igniteSaveMode = TransactionTypes.withName(properties.getString("igniteSaveMode", "STATEMENT"))
  lazy val isCaseSensitive = Try(properties.getBoolean("caseSensitiveEnabled")).getOrElse(false)
  lazy val failFast = Try(properties.getBoolean("failFast")).getOrElse(false)
  lazy val fetchSize = properties.getString("fetchSize", None)
  val sparkConf = xDSession.conf.getAll
  val securityUri = igniteSecurityUri(sparkConf)
  lazy val urlWithUser = addUserToConnectionURI(spartaTenant, url)
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)
  val urlWithSSL = if (tlsEnable) urlWithUser + securityUri else url



  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    val validationSeq = Seq[(HasError,String)](
      url.isEmpty -> s"the url must be provided",
      (tlsEnable && securityUri.isEmpty) -> s"when TLS is enabled the sparkConf must contain the security options",
      (fetchSize.isEmpty || fetchSize.get.toInt <= 0) -> s"fetch size must be greater than 0"
    )
    ErrorValidationsHelper.validate(validationSeq, name)
  }

  //scalastyle:off
  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(url.nonEmpty, "Ignite url must be provided")

    if (dataFrame.schema.fields.nonEmpty) {
      val tableName = getTableNameFromOptions(options)

      val primaryKey = getPrimaryKeyOptions(options) match {
        case Some(pk) => pk.trim.split(",").toSeq
        case None => Seq.empty[String]
      }
      require(primaryKey.nonEmpty, "The primary key fields must be provided")
      require(primaryKey.forall(dataFrame.schema.fieldNames.contains(_)),
        "All the primary key fields should be present in the dataFrame schema")

      val jdbcPropertiesMap = propertiesWithCustom.mapValues(_.toString).filter(_._2.nonEmpty)

      lazy val quotedTable = "\"" + tableName + "\""

      val connectionProperties = new JDBCOptions(
        urlWithSSL,
        if (isCaseSensitive) quotedTable else tableName,
        jdbcPropertiesMap
      )

      Try {
        if (saveMode == SaveModeEnum.Overwrite)
          SpartaJdbcUtils.truncateTable(connectionProperties, name)

        synchronized {
          SpartaJdbcUtils.tableExists(connectionProperties, dataFrame, name, primaryKey, Map("TEXT" -> "VARCHAR(255)"))
        }

      } match {
        case Success((tableExists,_)) =>
          try {
            if (tableExists) {
              val txSaveMode = TxSaveMode(igniteSaveMode, failFast)

              if (saveMode == SaveModeEnum.Delete) {
                SpartaJdbcUtils.deleteTable(dataFrame, connectionProperties, primaryKey, name, txSaveMode)
              }

              else if (saveMode == SaveModeEnum.Upsert) {
                upsertIgnite(dataFrame, connectionProperties, name, txSaveMode)
              }

              else if (saveMode == SaveModeEnum.Ignore) return

              else if (saveMode == SaveModeEnum.ErrorIfExists) sys.error(s"Table $tableName already exists")

              else if (saveMode == SaveModeEnum.Append || saveMode == SaveModeEnum.Overwrite) {
                SpartaJdbcUtils.saveTable(dataFrame, connectionProperties, name, txSaveMode, None)
              }
            } else log.warn(s"Table not created: $tableName")
          } catch {
            case e: Exception =>
              closeConnection(name)
              log.error(s"Error saving data into table $tableName with Error: ${e.getLocalizedMessage}")
              throw e
          }
        case Failure(e) =>
          closeConnection(name)
          log.error(s"Error creating/dropping table $tableName with Error: ${e.getLocalizedMessage}")
          throw e
      }
    }
  }

  override def cleanUp(options: Map[String, String]): Unit = {
    log.info(s"Closing connections in Ignite Output: $name")
    closeConnection(name)
  }

  def upsertIgnite(
                    df: DataFrame,
                    properties: JDBCOptions,
                    outputName: String,
                    txSaveMode: TxSaveMode
                  ): Unit = {

  val schema = df.schema

  val tableSchema: Option[StructType] = {
    using(getConnection(properties, outputName)) { conn =>
      val schemaFromDatabase = Try(properties.asProperties.getProperty("schemaFromDatabase")).toOption
      schemaFromDatabase.flatMap { value =>
        if (Try(value.toBoolean).getOrElse(false))
          JdbcUtils.getSchemaOption(conn, properties)
        else None
      }
    }
  }

  val dialect = JdbcDialects.get(properties.url)
  val nullTypes = schema.fields.map { field =>
    getJdbcType(field.dataType, dialect).jdbcNullType
  }

    val merge = getInsertStatement(properties.table, schema, tableSchema, isCaseSensitive, dialect, "MERGE")

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
          nativeUpsertPartition(properties, merge, iterator, schema, nullTypes, dialect, schema.fields.length, outputName, txSaveMode)
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
}

object IgniteOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}