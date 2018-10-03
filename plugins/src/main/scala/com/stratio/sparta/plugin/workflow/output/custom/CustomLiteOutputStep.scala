/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.custom

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.lite.common.LiteCustomOutput
import org.apache.spark.sql.crossdata.XDSession

class CustomLiteOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends CustomLiteOutputCommon[LiteCustomOutput](name, xDSession, properties)