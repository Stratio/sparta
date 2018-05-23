/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.avro

import java.io.{Serializable => JSerializable}

import com.databricks.spark.avro._
import com.stratio.sparta.sdk.models.ErrorValidations
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession


/**
  * This output save as avro file the information.
  */
class AvroOutputStep(
                      name: String,
                      xDSession: XDSession,
                      properties: Map[String, JSerializable]
                    ) extends OutputStep(name, xDSession, properties) {

  lazy val path: String = properties.getString("path", "").trim

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = {
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: destination path cannot be empty"
      )

    validation
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(path.nonEmpty, "Input path cannot be empty")
    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)
    val dataFrameWriter = dataFrame.write
      .options(getCustomProperties)
      .mode(getSparkSaveMode(saveMode))

    applyPartitionBy(options, dataFrameWriter, dataFrame.schema.fields).avro(s"$path/$tableName")
  }
}
