/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.enumerators.SaveModeEnum

case class WriterGraph(
                        saveMode: SaveModeEnum.Value = SaveModeEnum.Append,
                        tableName: Option[JsoneyString] = None,
                        partitionBy: Option[JsoneyString] = None,
                        constraintType: Option[JsoneyString] = None,
                        primaryKey: Option[JsoneyString] = None,
                        uniqueConstraintName: Option[JsoneyString] = None,
                        uniqueConstraintFields: Option[JsoneyString] = None,
                        updateFields: Option[JsoneyString] = None,
                        errorTableName: Option[JsoneyString] = None,
                        discardTableName: Option[JsoneyString] = None
                      )
