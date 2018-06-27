/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.crossdata

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.core.workflow.step.OutputStep._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

class CrossdataOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)
    val tempTableName = tableName + "Temp"
    val saveModeCondition = if (saveMode == SaveModeEnum.Overwrite) "OVERWRITE" else ""

    if (xDSession.catalog.tableExists(tableName)) {
      val tempFields = dataFrame.schema.fields.map(field => (field.name, field.dataType.catalogString))
      val catalogFields = xDSession.catalog.listColumns(tableName).collect()
        .map(column => (column.name, column.dataType))

      if ((tempFields.length == catalogFields.length) &&
        catalogFields.forall(col => tempFields.contains(col))) {
        dataFrame.createOrReplaceTempView(tempTableName)
        xDSession.sql(s"INSERT $saveModeCondition INTO TABLE $tableName SELECT * FROM $tempTableName")
      } else throw new RuntimeException(s"Incorrect schemas in the catalog table and the temporal table." +
        s"\tCatalog Schema: ${catalogFields.map(fields => s"${fields._1}-${fields._2}").mkString(",")}" +
        s"\tTemporal fields: ${tempFields.map(fields => s"${fields._1}-${fields._2}").mkString(",")}")
    } else throw new RuntimeException(s"The table name $tableName does not exist in the Crossdata catalog")
  }
}

object CrossdataOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}
