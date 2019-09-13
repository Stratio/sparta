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

  //lazy val location: Option[String] = properties.getString("location",None).notBlank
  lazy val dataAddress: Option[String] = properties.getString("dataAddress",None).notBlank
  lazy val sheetName:  Option[String] = properties.getString("sheetName", None).notBlank
  val sheetKey="sheetName"
  lazy val location: String = properties.getString("location", "").trim
  lazy val useHeader = Try(properties.getString("header", "false").toBoolean).getOrElse(false)
  lazy val inferSchema = Try(properties.getString("inferSchema", "false").toBoolean).getOrElse(false)
//  lazy val codecOption = propertiesWithCustom.getString("codec", None).notBlank
  lazy val compressExtension = propertiesWithCustom.getString("compressExtension", None).notBlank.getOrElse(".gz")

  override lazy val lineagePath: String = location

  override lazy val lineageResourceSuffix: Option[String] = None

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    val validationSeq = Seq(
      location.isEmpty -> "destination path cannot be empty",
      dataAddress.isEmpty -> "he location of data needs cannot be empty"
    )
    ErrorValidationsHelper.validate(validationSeq, name)


//    if (charset.exists(ch => !isCharsetSupported(ch)))
//      validation = ErrorValidations(
//        valid = false,
//        messages = validation.messages :+ WorkflowValidationMessage(s"encoding charset is not valid", name)
//      )

    validation
  }

  override def lineageProperties(): Map[String, String] = getHdfsLineageProperties(OutputStep.StepType)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(location.nonEmpty, "Input location cannot be empty")
//    require(dataAddress.nonEmpty, "The location of data needs to be specified")
    val locationParsed = if (location.endsWith("/")) location else location + "/"
    val tableName = getTableNameFromOptions(options)
    val optionsParsed =
      Map(
        "useHeader" -> useHeader.toString,
        "inferSchema" -> inferSchema.toString,
        "dataAddress" -> dataAddress.getOrElse(throw new RuntimeException("fesf"))
      ) //++ codecOption.fold(Map.empty[String, String]) { codec => Map("codec" -> codec) }

    val fullLocation = s"$locationParsed$tableName.xls"
    //val pathWithExtension = codecOption.fold(fullPath) { codec => fullPath + compressExtension }
    validateSaveMode(saveMode)
    val dataFrameWriter = dataFrame.write.format("com.crealytics.spark.excel").options(optionsParsed).mode(getSparkSaveMode(saveMode))


    applyPartitionBy(options, dataFrameWriter, dataFrame.schema.fields).save(fullLocation)
  }
}