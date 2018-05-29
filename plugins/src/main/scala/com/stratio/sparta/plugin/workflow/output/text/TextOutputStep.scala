/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.text

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions.{col, concat_ws}

import scala.util.Try

class TextOutputStep(
                      name: String,
                      xDSession: XDSession,
                      properties: Map[String, JSerializable]
                    ) extends OutputStep(name, xDSession, properties) {

  lazy val FieldName = "extractedData"
  lazy val path: String = properties.getString("path", "").trim
  lazy val delimiter: String = properties.getString("delimiter", ",")

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the destination path can not be empty", name)
      )

    validation
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(path.nonEmpty, "Input path can not be empty")
    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)
    val df = dataFrame.withColumn(
        FieldName,
        concat_ws(delimiter, dataFrame.schema.fields.flatMap(field => Some(col(field.name))).toSeq: _*)
      ).select(FieldName)

    applyPartitionBy(
      options,
      df.write.mode(getSparkSaveMode(saveMode)).options(getCustomProperties),
      df.schema.fields
    ).text(s"$path/$tableName")
  }
}
