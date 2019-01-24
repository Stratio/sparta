/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.lineage.HdfsLineage
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.common.csv.CsvBase
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class CsvOutputStep(
                     name: String,
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                   ) extends OutputStep(name, xDSession, properties) with CsvBase with HdfsLineage {

  lazy val path: String = properties.getString("path", "").trim
  lazy val header = Try(properties.getString("header", "false").toBoolean).getOrElse(false)
  lazy val inferSchema = Try(properties.getString("inferSchema", "false").toBoolean).getOrElse(false)
  lazy val delimiter = properties.getString("delimiter", None).notBlank
  lazy val charset = propertiesWithCustom.getString("charset", None).notBlank
  lazy val codecOption = propertiesWithCustom.getString("codec", None).notBlank
  lazy val compressExtension = propertiesWithCustom.getString("compressExtension", None).notBlank.getOrElse(".gz")

  override lazy val lineagePath: String = path

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

    if (delimiter.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"delimiter cannot be empty", name)
      )

    if (charset.exists(ch => !isCharsetSupported(ch)))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"encoding charset is not valid", name)
      )

    validation
  }

  override def lineageProperties(): Map[String, String] = getHdfsLineageProperties(OutputStep.StepType)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(path.nonEmpty, "Input path cannot be empty")
    require(delimiter.nonEmpty, "Delimiter field cannot be empty")

    val pathParsed = if (path.endsWith("/")) path else path + "/"
    val tableName = getTableNameFromOptions(options)
    val optionsParsed =
      Map(
        "header" -> header.toString,
        "delimiter" -> delimiter.get,
        "inferSchema" -> inferSchema.toString
      ) ++ codecOption.fold(Map.empty[String, String]) { codec => Map("codec" -> codec) }
    val fullPath = s"$pathParsed$tableName.csv"
    val pathWithExtension = codecOption.fold(fullPath) { codec => fullPath + compressExtension }

    validateSaveMode(saveMode)

    val dataFrameWriter = dataFrame.write
      .mode(getSparkSaveMode(saveMode))
      .options(optionsParsed ++ getCustomProperties)

    applyPartitionBy(options, dataFrameWriter, dataFrame.schema.fields).csv(pathWithExtension)
  }
}