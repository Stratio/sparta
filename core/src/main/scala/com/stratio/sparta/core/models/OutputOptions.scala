/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models

import com.stratio.sparta.core.enumerators.SaveModeEnum


case class OutputOptions(
                          saveMode: SaveModeEnum.Value = SaveModeEnum.Append,
                          stepName: String,
                          tableName: String,
                          partitionBy: Option[String] = None,
                          constraintType : Option[String] = None,
                          primaryKey: Option[String] = None,
                          uniqueConstraintName: Option[String] = None,
                          uniqueConstraintFields: Option[String] = None,
                          updateFields: Option[String] = None,
                          errorTableName: Option[String] = None,
                          discardTableName: Option[String] = None
                        )
