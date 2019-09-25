/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep

case class WriterGraph(
                        saveMode: SaveModeEnum.Value = SaveModeEnum.Append,
                        tableName: Option[String] = None,
                        partitionBy: Option[String] = None,
                        constraintType: Option[String] = None,
                        primaryKey: Option[String] = None,
                        uniqueConstraintName: Option[String] = None,
                        uniqueConstraintFields: Option[String] = None,
                        updateFields: Option[String] = None,
                        errorTableName: Option[String] = None,
                        discardTableName: Option[String] = None
                      ) {

  def toOutputWriterOptions(nodeName: String): OutputWriterOptions = {
    val calculatedTableName = tableName.notBlank.getOrElse(nodeName)

    OutputWriterOptions(
      saveMode = saveMode,
      outputStepName = OutputWriterOptions.OutputStepNameNA,
      stepName = nodeName,
      tableName = calculatedTableName,
      errorTableName = errorTableName.notBlank.getOrElse(nodeName),
      discardTableName = discardTableName.notBlank,
      extraOptions = {
        partitionBy.notBlank.fold(Map.empty[String, String]) { partition =>
          Map(OutputStep.PartitionByKey -> partition)
        } ++
          primaryKey.notBlank.fold(Map.empty[String, String]) { key =>
            Map(OutputStep.PrimaryKey -> key)
          } ++
          uniqueConstraintName.notBlank.fold(Map.empty[String, String]) { key =>
            Map(OutputStep.UniqueConstraintName -> key)
          } ++
          uniqueConstraintFields.notBlank.fold(Map.empty[String, String]) { key =>
            Map(OutputStep.UniqueConstraintFields -> key)
          } ++
          updateFields.notBlank.fold(Map.empty[String, String]) { key =>
            Map(OutputStep.UpdateFields -> key)
          }
      }
    )
  }
}

case class OutputWriter(
                         saveMode: SaveModeEnum.Value = SaveModeEnum.Append,
                         outputStepName: String,
                         tableName: Option[String] = None,
                         discardTableName: Option[String] = None,
                         extraOptions: Map[String, String] = Map.empty[String, String]
                       ) {

  def toOutputWriterOptions(nodeName: String, errorTableName: String): OutputWriterOptions = {
    val calculatedTableName = tableName.notBlank.getOrElse(nodeName)

    OutputWriterOptions(
      saveMode = saveMode,
      outputStepName = outputStepName,
      stepName = nodeName,
      tableName = calculatedTableName,
      errorTableName = errorTableName,
      discardTableName = discardTableName.notBlank,
      extraOptions = extraOptions
    )
  }
}
