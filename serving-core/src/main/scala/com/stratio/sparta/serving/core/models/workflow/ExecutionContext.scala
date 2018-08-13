/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.serving.core.models.env.EnvironmentVariable

case class ExecutionContext(
                             withEnvironment: Boolean,
                             extraParams: Seq[EnvironmentVariable] = Seq.empty[EnvironmentVariable],
                             paramsLists: Seq[String] = Seq.empty[String]
                           ) {

  def toVariablesMap: Map[String, String] = extraParams.flatMap(paramVariable =>
    if(paramVariable.value.nonEmpty)
      Option(paramVariable.name -> paramVariable.value)
    else None
  ).toMap
}
