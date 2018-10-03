/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.lite.xd.common

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.lite.common.SDKCustomOutput
import com.stratio.sparta.sdk.lite.common.models.OutputOptions
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

abstract class LiteCustomXDOutput(
                                   @transient xdSession: XDSession,
                                   properties: Map[String, String]
                                 ) extends SDKCustomOutput with SLF4JLogging {

  @deprecated("this method will become internal. Use overloaded version of 'save' with OutputOptions instead. " +
    "It will be invoked if the overloaded version is not overridden")
  def save(data: DataFrame, saveMode: String, saveOptions: Map[String, String]): Unit

  def save(dataFrame: DataFrame, outputOptions: OutputOptions): Unit =
    save(dataFrame, outputOptions.saveMode.toString, properties ++ outputOptions.customProperties)

}
