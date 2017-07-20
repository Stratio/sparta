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

package com.stratio.sparta.plugin.output.crossdata

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

class CrossdataOutput(
                       name: String,
                       sparkSession: XDSession,
                       properties: Map[String, Serializable]
                     ) extends Output(name, sparkSession, properties) {

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)
    val tempTableName = tableName + "Temp"
    val saveModeCondition = if (saveMode == SaveModeEnum.Overwrite) "OVERWRITE" else ""

    if (sparkSession.catalog.tableExists(tableName)) {
      val tempFields = dataFrame.schema.fields.map(field => (field.name, field.dataType.catalogString))
      val catalogFields = sparkSession.catalog.listColumns(tableName).collect()
        .map(column => (column.name, column.dataType))

      if ((tempFields.length == catalogFields.length) &&
        catalogFields.forall(col => tempFields.contains(col))) {
        dataFrame.createOrReplaceTempView(tempTableName)
        sparkSession.sql(s"INSERT $saveModeCondition INTO TABLE $tableName SELECT * FROM $tempTableName")
      } else throw new RuntimeException(s"Incorrect schemas in the catalog table and the temporal table." +
        s"\tCatalog Schema: ${catalogFields.map(fields => s"${fields._1}-${fields._2}").mkString(",")}" +
        s"\tTemporal fields: ${tempFields.map(fields => s"${fields._1}-${fields._2}").mkString(",")}")
    } else throw new RuntimeException(s"The table name $tableName does not exist in the Crossdata catalog")
  }
}

object CrossdataOutput {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataSourceSecurityConf(configuration)
  }
}
