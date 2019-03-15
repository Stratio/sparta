/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.sdk.lite.common.models

/**
  * Sparta Output options
 *
  * @param saveMode save mode configured in the previous transformation step
  * @param tableName table name configured in the previous transformation step
  * @param primaryKey primary key configured in the previous transformation step
  * @param partitionBy partitionBy fields configured in the previous transformation step
  * @param customProperties customProperties map as key value
  */
case class OutputOptions(
                          saveMode: SaveMode,
                          tableName: Option[String],
                          primaryKey: Option[String],
                          partitionBy: Seq[String],
                          customProperties: Map[String, String]
                        )

//must be synchronized with com.stratio.sparta.core.workflow.step.OutputStep
object OutputOptions {

  private[sdk] val PrimaryKey = "primaryKey"
  private[sdk] val TableNameKey = "tableName"
  private[sdk] val PartitionByKey = "partitionBy"
  private[sdk] val UniqueConstraintName = "uniqueConstraintName"
  private[sdk] val UniqueConstraintFields = "uniqueConstraintFields"
  private[sdk] val UpdateFields = "updateFields"

  def toMap: OutputOptions => Map[String, String] = outputOpts => {
    outputOpts.tableName.map(tName => Map(TableNameKey -> tName)).getOrElse(Map.empty) ++
      outputOpts.customProperties ++
      outputOpts.primaryKey.map(pKey => Map(PrimaryKey -> pKey)).getOrElse(Map.empty) ++
      outputOpts.partitionBy.headOption.map(_ => Map(PartitionByKey -> outputOpts.partitionBy.mkString(","))).getOrElse(Map.empty)
  }

}