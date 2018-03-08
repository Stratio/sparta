/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package org.apache.spark.streaming.datasource.models


case class StatusOffset(started: Boolean, offsetConditions: OffsetConditions) {

  override def toString: String =
    s"[Started: $started" +
      s"OffsetConditions: $offsetConditions]"
}

object StatusOffset {

  def apply(offsetConditions: OffsetConditions): StatusOffset = new StatusOffset(started = false, offsetConditions)
}
