/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.parquet

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputStep}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession


/**
  * This output saves as a parquet file the information received from the stream.
  *
  * @param name
  * @param properties
  */
class ParquetOutputStep(
                        name : String,
                        xDSession: XDSession,
                        properties: Map[String, JSerializable]
                        ) extends OutputStep(name, xDSession, properties){

  lazy val path: String = properties.getString("path", "").trim

  override def supportedSaveModes : Seq[SaveModeEnum.Value] = {
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the destination path can not be empty"
      )

    validation
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String,String]): Unit = {
    require(path.nonEmpty, "Input path can not be empty")
    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)

    applyPartitionBy(
      options,
      dataFrame.write.options(getCustomProperties).mode(getSparkSaveMode(saveMode)),
      dataFrame.schema.fields
    ).parquet(s"$path/$tableName")
  }
}
