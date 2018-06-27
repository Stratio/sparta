/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.lite.xd.streaming

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.lite.streaming.models.ResultStreamingData
import com.stratio.sparta.sdk.lite.validation.Validator
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext

abstract class LiteCustomXDStreamingInput(
                                         @transient xdSession: XDSession,
                                         @transient streamingContext: StreamingContext,
                                         properties: Map[String, String]
                                       ) extends Validator with SLF4JLogging {

  def init(): ResultStreamingData

}
