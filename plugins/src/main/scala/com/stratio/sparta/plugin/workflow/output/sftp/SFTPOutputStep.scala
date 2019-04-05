/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.sftp

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import com.stratio.sparta.serving.core.models.enumerators.SftpFileTypeEnum
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions.{col, concat_ws}

import scala.util.Try

class SFTPOutputStep(name: String,
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                    ) extends OutputStep(name, xDSession, properties) {

  lazy val path: Option[String] = properties.getString("path", None).map(_.trim)
  lazy val host: Option[String] = properties.getString("host", None)
  lazy val port: Option[String] = properties.getString("port", None)
  lazy val username: Option[String] = properties.getString("sftpServerUsername", None)
  lazy val password: Option[String] = properties.getString("password", None)
  lazy val fileType: Option[String] = properties.getString(key = "fileType", None)
  lazy val header: Boolean = Try(getCustomProperties.getOrElse("header", "false").toBoolean).getOrElse(false)
  lazy val inferSchema: Boolean = Try(getCustomProperties.getOrElse("inferSchema", "false").toBoolean).getOrElse(false)
  lazy val delimiter: String = getCustomProperties.getOrElse("delimiter", ",")
  lazy val codecOption: Option[String] = getCustomProperties.get("codec")
  lazy val compressExtension: String = getCustomProperties.getOrElse("compressExtension", ".gz")
  lazy val rowTag: Option[String] = getCustomProperties.get("rowTag")
  lazy val rootTag: String = getCustomProperties.getOrElse("rootTag", "ROWS")
  lazy val tlsEnable: Boolean = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)
  lazy val FieldName = "extractedData"

  lazy val sparkConf = xDSession.conf.getAll
  lazy val pemOption = if(tlsEnable) {
    SecurityHelper.getPemUri(sparkConf).fold(Map.empty[String, String]) { pemUri => Map("pem" -> pemUri) }
  } else Map.empty[String, String]

  lazy val commonOptions: Map[String, String] = Map(
    "path" -> path,
    "host" -> host,
    "port" -> port,
    "username" -> username,
    "fileType" -> fileType,
    "password" -> password,
    "delimiter" -> properties.getString("delimiter", None),
    "rowTag" -> properties.getString("rowTag", None)
  ).filter { case (key, value) => value.isDefined}.map { case (key, optValue) => (key, optValue.get)} ++ pemOption



  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {

    val minPort = 0
    val maxPort = 65535

    val validationSeq = Seq[(HasError, String)](
      fileType.isEmpty -> "The input file type is not chosen.",
      host.isEmpty -> "The host value is not defined.",
      port.isEmpty -> "The port value is not defined.",
      (port.nonEmpty && Try(port.get.toInt).isSuccess && (port.get.toInt <= minPort || port.get.toInt >= maxPort)) -> "The port value is not valid.",
      path.isEmpty -> "The path value is not defined.",
      username.isEmpty -> "The username for the sftp server is not defined.",
      (fileType.get == SftpFileTypeEnum.xml.toString && rowTag.isEmpty) -> "The user should specify a row tag of the xml files to treat as a row",
      ((fileType.get == SftpFileTypeEnum.csv.toString) && delimiter.isEmpty) -> "Delimiter is not defined."

    )

    ErrorValidationsHelper.validate(validationSeq, name)
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(path.nonEmpty, "Input path cannot be empty")
    require(fileType.nonEmpty, "File type field cannot be empty")

    val fileName = getTableNameFromOptions(options)
    val newCommonOptions = commonOptions + ("fileName" -> fileName)

    validateSaveMode(saveMode)
    val pathParsed = if(path.get.endsWith("/")) path.get else path.get + "/"

    /* Save according to the file types (csv,json,avro,parquet,txt,xml)*/
    if (fileType.get == SftpFileTypeEnum.csv.toString) {

      val fullPath = s"$pathParsed${fileName}.csv"
      val pathWithExtension = codecOption.fold(fullPath) { codec => fullPath + compressExtension }

      validateSaveMode(saveMode)

      val dataFrameWriter = dataFrame.write
        .mode(getSparkSaveMode(saveMode))
        .options(getCustomProperties ++ newCommonOptions )

      applyPartitionBy(options,
        dataFrameWriter,
        dataFrame.schema.fields
      ).format("com.springml.spark.sftp").save(pathWithExtension)
    }
    else if (fileType.get == SftpFileTypeEnum.json.toString) {
      val fullPath = s"$pathParsed${fileName}.json"

      applyPartitionBy(
        options,
        dataFrame.write.mode(getSparkSaveMode(saveMode)).options(getCustomProperties ++ newCommonOptions),
        dataFrame.schema.fields
      ).format("com.springml.spark.sftp").save(fullPath)
    }
    else if (fileType.get == SftpFileTypeEnum.avro.toString) {
      val fullPath = s"$pathParsed${fileName}.avro"

      val dataFrameWriter = dataFrame.write
        .options(getCustomProperties ++ newCommonOptions)
        .mode(getSparkSaveMode(saveMode))

      applyPartitionBy(options,
        dataFrameWriter,
        dataFrame.schema.fields
      ).format("com.springml.spark.sftp").save(fullPath)
    }
    else if (fileType.get == SftpFileTypeEnum.parquet.toString) {
      val fullPath = s"$pathParsed${fileName}.parquet"

      applyPartitionBy(
        options,
        dataFrame.write.options(getCustomProperties ++ newCommonOptions).mode(getSparkSaveMode(saveMode)),
        dataFrame.schema.fields
      ).format("com.springml.spark.sftp").save(fullPath)
    }
    else if (fileType.get == SftpFileTypeEnum.txt.toString) {
      val df = dataFrame.withColumn(
        FieldName,
        concat_ws(delimiter, dataFrame.schema.fields.flatMap(field => Some(col(field.name))).toSeq: _*)
      ).select(FieldName)

      val fullPath = s"$pathParsed${fileName}.txt"

      applyPartitionBy(
        options,
        df.write.mode(getSparkSaveMode(saveMode)).options(getCustomProperties ++ newCommonOptions),
        df.schema.fields
      ).format("com.springml.spark.sftp").save(fullPath)
    }
    else if (fileType.get == SftpFileTypeEnum.xml.toString) {
      val fullPath = s"$pathParsed${fileName}.xml"

      val dataFrameWriter = dataFrame.write
        .mode(getSparkSaveMode(saveMode))
        .options(getCustomProperties ++ newCommonOptions)

      applyPartitionBy(
        options,
        dataFrameWriter,
        dataFrame.schema.fields
      ).format("com.springml.spark.sftp").save(fullPath)
    }
  }

}

object SFTPOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }

}