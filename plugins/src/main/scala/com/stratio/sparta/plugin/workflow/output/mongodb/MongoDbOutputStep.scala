/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mongodb

import java.io.{Serializable => JSerializable}

import com.stratio.datasource.mongodb.config.MongodbConfig
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType

import scala.util.Try

object MongoDbOutputStep {

  val MongoDbSparkDatasource = "com.stratio.datasource.mongodb"
  val DefaultDatabase = ""

  def getConnectionString(properties: Map[String, JSerializable]): String = {

    val hostSeq: Seq[Map[String, String]] = Try(properties.getMapFromArrayOfValues("hosts")).getOrElse(Seq.empty)

    def mongoNodeMap2String: Map[String, String] => Option[String] = mongoNodeMap =>
      for {
        mongoHost <- mongoNodeMap.get("host").notBlank
        mongoPort <- mongoNodeMap.get("port").notBlank
      } yield s"$mongoHost:$mongoPort"

    hostSeq.map(mongoNodeMap2String).filter(_.nonEmpty).mkString(",")
  }
}


class MongoDbOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  import MongoDbOutputStep._

  val hosts: String = getConnectionString(properties)
  val dbName: String = properties.getString("dbName", None).notBlank.getOrElse(DefaultDatabase)


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    val isEmptyValidationSeq = Seq(
      hosts -> "hosts definition is empty or not valid",
      dbName -> "database name cannot be empty"
    )

    (ErrorValidations(valid = true, messages = Seq.empty) /: isEmptyValidationSeq) { case (errValidation, (value, validationMessage)) =>

      if (value.isEmpty) {
        errValidation.copy(valid = false, messages = errValidation.messages :+ WorkflowValidationMessage(validationMessage, name))
      } else {
        errValidation
      }
    }
  }

  /* DOCS
    val Append = Value("Append")
    val ErrorIfExists = Value("ErrorIfExists")
    val Ignore = Value("Ignore")
    val Overwrite = Value("Overwrite")
    val Upsert = Value("Upsert")
    val Delete = Value("Delete")*/

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(dbName.nonEmpty, "Database name cannot be empty")

    val tableName = getTableNameFromOptions(options)
    val primaryKeyOption = getPrimaryKeyOptions(options)
    val dataFrameOptions = getDataFrameOptions(tableName, dataFrame.schema, saveMode, primaryKeyOption)

    validateSaveMode(saveMode)

    dataFrame.write
      .format(MongoDbSparkDatasource)
      .mode(getSparkSaveMode(saveMode))
      .options(dataFrameOptions ++ getCustomProperties)
      .save()
  }

  private def getDataFrameOptions(tableName: String,
                                  schema: StructType,
                                  saveMode: SaveModeEnum.Value,
                                  primaryKey: Option[String]
                                 ): Map[String, String] =
    Map(
      MongodbConfig.Host -> hosts,
      MongodbConfig.Database -> dbName,
      MongodbConfig.Collection -> tableName
    ) ++ {
      saveMode match {
        case SaveModeEnum.Upsert => getUpdateFieldsOptions(schema, primaryKey)
        case _ => Map.empty
      }
    }

  private def getUpdateFieldsOptions(schema: StructType, primaryKey: Option[String]): Map[String, String] = {
    val updateFields = primaryKey.getOrElse("")
    Map(MongodbConfig.UpdateFields -> updateFields)
  }

}