/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.jdbc

import java.io.{Serializable => JSerializable}

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils._
import org.apache.spark.sql.jdbc.{SpartaJdbcUtils, TxSaveMode}
import com.stratio.sparta.plugin.enumerations.TransactionTypes
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.helper.SecurityHelper._
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.enumerators.SaveModeEnum.SpartaSaveMode
import com.stratio.sparta.core.workflow.step.OutputStep

class JdbcOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val url = properties.getString("url")
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)
  lazy val jdbcSaveMode = TransactionTypes.withName(properties.getString("jdbcSaveMode", "STATEMENT"))
  lazy val failFast = Try(properties.getBoolean("failFast")).getOrElse(false)
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
        messages = validation.messages :+ WorkflowValidationMessage(s"when TLS is enabled the sparkConf must contain the security options", name)
      )

    validation
  }

  override def supportedSaveModes: Seq[SpartaSaveMode] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite,
      SaveModeEnum.Upsert, SaveModeEnum.Delete)

  //scalastyle:off
  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(url.nonEmpty, "JDBC url must be provided")
    validateSaveMode(saveMode)
    require(!(jdbcSaveMode == TransactionTypes.ONE_TRANSACTION && saveMode == SaveModeEnum.Delete),
      s"Writer SaveMode Delete could not be used with Jdbc save mode $jdbcSaveMode")
    if (dataFrame.schema.fields.nonEmpty) {
      val tableName = getTableNameFromOptions(options)
      val sparkSaveMode = getSparkSaveMode(saveMode)
      val connectionProperties = new JDBCOptions(urlWithSSL,
        tableName,
        propertiesWithCustom.mapValues(_.toString).filter(_._2.nonEmpty)
      )

      Try {
        if (sparkSaveMode == SaveMode.Overwrite)
          SpartaJdbcUtils.truncateTable(connectionProperties, name)


        synchronized {
          SpartaJdbcUtils.tableExists(connectionProperties, dataFrame, name)
        }
      } match {
        case Success((tableExists,_)) =>
          try {
            if (tableExists) {
              val txSaveMode = TxSaveMode(jdbcSaveMode, failFast)
              if (saveMode == SaveModeEnum.Delete) {
                val updateFields = getPrimaryKeyOptions(options) match {
                  case Some(pk) => pk.trim.split(",").toSeq
                  case None => Seq.empty[String]
                }
                require(updateFields.nonEmpty, "The primary key fields must be provided")
                require(updateFields.forall(dataFrame.schema.fieldNames.contains(_)),
                  "All the primary key fields should be present in the dataFrame schema")
                SpartaJdbcUtils.deleteTable(dataFrame, connectionProperties, updateFields, name, txSaveMode)
              } else if (saveMode == SaveModeEnum.Upsert) {
                val updateFields = getPrimaryKeyOptions(options) match {
                  case Some(pk) =>  pk.split(",").map(_.trim).toSeq
                  case None => Seq.empty[String]
                }

                require(updateFields.nonEmpty, "The primary key fields must be provided")
                require(updateFields.forall(dataFrame.schema.fieldNames.contains(_)),
                  "All the primary key fields should be present in the dataFrame schema")

                SpartaJdbcUtils.upsertTable(dataFrame, connectionProperties, updateFields, name, txSaveMode)
              }

              if (saveMode == SaveModeEnum.Ignore) return

              if (saveMode == SaveModeEnum.ErrorIfExists) sys.error(s"Table $tableName already exists")

              if (saveMode == SaveModeEnum.Append || saveMode == SaveModeEnum.Overwrite)
                SpartaJdbcUtils.saveTable(dataFrame, connectionProperties, name, txSaveMode, None)
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
    log.info(s"Closing connections in JDBC Output: $name")
    closeConnection(name)
  }
}

object JdbcOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}