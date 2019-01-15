/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{ErrorsManagement, OutputOptions}
import com.stratio.sparta.core.properties.Parameterizable
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, DataFrameWriter, Row, SaveMode}


abstract class OutputStep[Underlying[Row]](
                                            val name: String,
                                            @transient private[sparta] val xDSession: XDSession,
                                            properties: Map[String, JSerializable]
                                          ) extends Parameterizable(properties) with GraphStep with SLF4JLogging {

  import OutputStep._

  override lazy val customKey = DefaultCustomKey
  override lazy val customPropertyKey = DefaultCustomPropertyKey
  override lazy val customPropertyValue = DefaultCustomPropertyValue

  /**
    * Generic write function that receives the stream data and passes it to the dataFrame, calling the save
    * function afterwards.
    *
    * @param inputData     Input stream data to save
    * @param outputOptions Options to save
    */
  private[sparta] def writeTransform(
                                      inputData: DistributedMonad[Underlying],
                                      outputOptions: OutputOptions,
                                      errorsManagement: ErrorsManagement,
                                      errorOutputs: Seq[OutputStep[Underlying]],
                                      predecessors: Seq[String]
                                    ): Unit =
    inputData.write(outputOptions, xDSession, errorsManagement, errorOutputs, predecessors)(save)

  /**
    * Save function that implements the plugins.
    *
    * @param dataFrame The dataFrame to save
    * @param saveMode  The sparta save mode selected
    * @param options   Options to save the data (partitionBy, primaryKey ... )
    */
  def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit

  /** PRIVATE METHODS **/

  private[sparta] def supportedSaveModes: Seq[SaveModeEnum.Value] = SaveModeEnum.allSaveModes

  private[sparta] def validateSaveMode(saveMode: SaveModeEnum.Value): Unit = {
    if (!supportedSaveModes.contains(saveMode))
      log.warn(s"Save mode $saveMode selected not supported by the output $name." +
        s" Using the default mode ${SaveModeEnum.Append}"
      )
  }

  private[sparta] def getSparkSaveMode(saveModeEnum: SaveModeEnum.Value): SaveMode =
    saveModeEnum match {
      case SaveModeEnum.Append => SaveMode.Append
      case SaveModeEnum.ErrorIfExists => SaveMode.ErrorIfExists
      case SaveModeEnum.Overwrite => SaveMode.Overwrite
      case SaveModeEnum.Ignore => SaveMode.Ignore
      case SaveModeEnum.Upsert => SaveMode.Append
      case SaveModeEnum.Delete => SaveMode.Append
      case _ =>
        log.warn(s"Save Mode $saveModeEnum not supported, using default save mode ${SaveModeEnum.Append}")
        SaveMode.Append
    }

  private[sparta] def getPrimaryKeyOptions(options: Map[String, String]): Option[String] =
    options.get(PrimaryKey).notBlank

  private[sparta] def getPartitionByKeyOptions(options: Map[String, String]): Option[String] =
    options.get(PartitionByKey).notBlank

  private[sparta] def getUniqueConstraintNameOptions(options: Map[String, String]): Option[String] =
    options.get(UniqueConstraintName).notBlank

  private[sparta] def getUniqueConstraintFieldsOptions(options: Map[String, String]): Option[String] =
    options.get(UniqueConstraintFields).notBlank

  private[sparta] def getUpdateFieldsOptions(options: Map[String, String]): Option[String] =
    options.get(UpdateFields).notBlank

  private[sparta] def getTableNameFromOptions(options: Map[String, String]): String =
    options.getOrElse(TableNameKey, {
      log.error("Table name not defined")
      throw new NoSuchElementException("tableName not found in options")
    })

  private[sparta] def applyPartitionBy(options: Map[String, String],
                                       dataFrame: DataFrameWriter[Row],
                                       schemaFields: Array[StructField]): DataFrameWriter[Row] = {

    options.get(PartitionByKey).notBlank.fold(dataFrame)(partitions => {
      val fieldsInDataFrame = schemaFields.map(field => field.name)
      val partitionFields = partitions.split(",")
      if (partitionFields.forall(field => fieldsInDataFrame.contains(field)))
        dataFrame.partitionBy(partitionFields: _*)
      else {
        log.warn(s"Impossible to execute partition by fields: $partitionFields because the dataFrame does not " +
          s"contain all fields. The dataFrame only contains: ${fieldsInDataFrame.mkString(",")}")
        dataFrame
      }
    })
  }
}

object OutputStep {

  private[sparta] val StepType = "output"

  private[sparta] val PrimaryKey = "primaryKey"
  private[sparta] val TableNameKey = "tableName"
  private[sparta] val PartitionByKey = "partitionBy"
  private[sparta] val UniqueConstraintName = "uniqueConstraintName"
  private[sparta] val UniqueConstraintFields = "uniqueConstraintFields"
  private[sparta] val UpdateFields = "updateFields"

  private[sparta] val DefaultCustomKey = "saveOptions"
  private[sparta] val DefaultCustomPropertyKey = "saveOptionsKey"
  private[sparta] val DefaultCustomPropertyValue = "saveOptionsValue"

}