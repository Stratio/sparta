/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.xml

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.lineage.HdfsLineage
import com.stratio.sparta.core.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

class XMLOutputStep (name: String,
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                    ) extends OutputStep(name, xDSession, properties) with HdfsLineage {

  lazy val path: Option[String] = properties.getString("path", None).notBlank
  lazy val rowTag = properties.getString("rowTag", None)

  // val DEFAULT_ROOT_TAG = "ROWS", we cannot refer to it since it belongs to a private class
  lazy val rootTag = properties.getString("rootTag", "ROWS")

  override lazy val lineagePath: String = path.getOrElse("")

  override lazy val lineageResourceSuffix: Option[String] = None

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"destination path cannot be empty", name)
      )

    if (rowTag.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"RowTag cannot be empty." +
          s"The user should specify a row tag of the xml files to treat as a row", name)
      )

    validation
  }

  override def lineageProperties(): Map[String, String] = getHdfsLineageProperties

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(path.nonEmpty, "Output path cannot be empty")
    require(rowTag.nonEmpty, "RowTag cannot be empty")


    val pathParsed = if (path.get.endsWith("/")) path.get else path.get + "/"
    val tableName = getTableNameFromOptions(options)
    val optionsParsed = Map(
      "rowTag" -> rowTag.get,
      "rootTag" -> rootTag
    )
    val fullPath = s"$pathParsed$tableName.xml"

    validateSaveMode(saveMode)

    val dataFrameWriter = dataFrame.write
      .mode(getSparkSaveMode(saveMode))
      .options(optionsParsed ++ getCustomProperties)

    applyPartitionBy(options, dataFrameWriter, dataFrame.schema.fields)
      .format("com.databricks.spark.xml").save(fullPath)
  }
}