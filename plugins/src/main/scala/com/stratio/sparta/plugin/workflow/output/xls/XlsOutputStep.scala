/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.xls

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.common.csv.CsvBase
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.workflow.lineage.HdfsLineage
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class XlsOutputStep(
                     name: String,
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                   ) extends OutputStep(name, xDSession, properties) with CsvBase with HdfsLineage {


  lazy val dataRange: Option[String] = properties.getString("dataRange",None).notBlank
  lazy val sheetName:  Option[String] = properties.getString("sheetName", None).notBlank
  lazy val location: String = properties.getString("location", "").trim
  lazy val useHeader = Try(properties.getString("header", "false").toBoolean).getOrElse(false)
  lazy val treatEmptyValuesAsNulls: Option[String] = properties.getString("treatEmptyValuesAsNulls",None).notBlank
  lazy val inferSchema = Try(properties.getString("inferSchema", "false").toBoolean).getOrElse(false)
  lazy val dateFormat = properties.getString("dateFormat", None)
  lazy val timestampFormat = properties.getString("timestampFormat", None)
  override lazy val lineagePath: String = location
  lazy val sheetData: Option[String] = for {
    sh_name <- sheetName
    range <- dataRange
  } yield s"'$sh_name'!$range"

  override lazy val lineageResourceSuffix: Option[String] = None

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    val validationSeq = Seq(
      location.isEmpty -> "Destination path cannot be empty",
      dataRange.isEmpty -> "The location of data  cannot be empty",
      sheetName.isEmpty -> "Sheet name cannot be empty"
    )
    ErrorValidationsHelper.validate(validationSeq, name)
    validation
  }

  override def lineageProperties(): Map[String, String] = getHdfsLineageProperties(OutputStep.StepType)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(location.nonEmpty, "The input is not defined")
    require(dataRange.nonEmpty, "Data range is not defined (start_cell:end_cell)")
    require(sheetName.nonEmpty, "Sheet name is not defined")

    val locationParsed = if (location.endsWith("/")) location else location + "/"
    val tableName = getTableNameFromOptions(options)


    val templateOptions: Map[String, String] = Map(
      "useHeader" -> Some(useHeader.toString),
      "treatEmptyValuesAsNulls" -> treatEmptyValuesAsNulls,
      "dataAddress" -> sheetData,
      "dateFormat" -> dateFormat,
      "inferschema" ->Some(inferSchema.toString),
      "timestampFormat" -> timestampFormat
    ).flatMap {
      case (k, v) => v.map(value => Option(k -> value))
    }.flatten.toMap

    val userOptions: Map[String, String] = propertiesWithCustom
      .flatMap {
        case (key, value) if value.toString.checkIfEmpty => Option(key -> value.toString)
        case (_,_) => None
      }

    val fullLocation = s"$locationParsed$tableName.xls"
    validateSaveMode(saveMode)
    val dataFrameWriter = dataFrame.write.format("com.crealytics.spark.excel").options(templateOptions ++ userOptions).mode(getSparkSaveMode(saveMode))

    applyPartitionBy(options, dataFrameWriter, dataFrame.schema.fields).save(fullLocation)
  }
}