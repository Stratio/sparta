/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mongodb

import java.io.{Serializable => JSerializable}

import com.stratio.datasource.mongodb.config.MongodbConfig
import com.stratio.sparta.sdk.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType

import scala.util.Try

class MongoDbOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val DefaultHost = "localhost"
  lazy val DefaultPort = "27017"
  lazy val MongoDbSparkDatasource = "com.stratio.datasource.mongodb"
  lazy val hosts =  getConnectionConfs("hosts", "host", "port")
  lazy val dbName = properties.getString("dbName", "").trim

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (hosts.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"hosts definition is empty or not valid", name)
      )

    if (dbName.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"database name cannot be empty", name)
      )

    validation
  }

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
        case _ => Map.empty[String, String]
      }
    }

  private def getUpdateFieldsOptions(schema: StructType, primaryKey: Option[String]): Map[String, String] = {
    val updateFields = primaryKey.getOrElse("")

    Map(MongodbConfig.UpdateFields -> updateFields)
  }

  private def getConnectionConfs(key: String, firstJsonItem: String, secondJsonItem: String): String = {

    if (properties(key).toString.nonEmpty && (!properties(key).toString.equals("[]"))){
      val conObj = properties.getMapFromArrayOfValues(key)
      conObj.map(c => {
        val host = Try(Option(c.getString(firstJsonItem)).notBlank.map(_.toString)).getOrElse(None)
        val port = Try(Option(c.getString(secondJsonItem)).notBlank.map(_.toString)).getOrElse(None)
        if(host.isDefined && port.isDefined)
          s"${host.get}:${port.get}"
        else ""
      }).filterNot(_.isEmpty).mkString(",")
    }
    else ""
  }
}
