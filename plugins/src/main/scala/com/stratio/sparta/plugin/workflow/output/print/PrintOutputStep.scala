/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.print

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class PrintOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val printData = Try(properties.getBoolean("printData")).getOrElse(false)
  lazy val printSchema = Try(properties.getBoolean("printSchema")).getOrElse(false)
  lazy val printMetadata = Try(properties.getBoolean("printMetadata")).getOrElse(true)
  lazy val logLevel = properties.getString("logLevel", "warn")

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val metadataInfo = if (printMetadata)
      Option(s"Table name : ${getTableNameFromOptions(options)}\tElements: ${dataFrame.count()}")
    else None
    val schemaInfo = if (printSchema) Option(s"DataFrame schema: ${dataFrame.schema.toString()}") else None

    logLevel match {
      case "warn" =>
        metadataInfo.foreach(info => log.warn(info))
        schemaInfo.foreach(schema => log.warn(schema))
        if (printData) dataFrame.foreach(row => log.warn(row.mkString(",")))
      case "error" =>
        metadataInfo.foreach(info => log.error(info))
        schemaInfo.foreach(schema => log.error(schema))
        if (printData) dataFrame.foreach(row => log.error(row.mkString(",")))
      case _ =>
        metadataInfo.foreach(info => log.info(info))
        schemaInfo.foreach(schema => log.info(schema))
        if (printData) dataFrame.foreach(row => log.info(row.mkString(",")))
    }
  }
}
