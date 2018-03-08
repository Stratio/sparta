/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import java.util.Date
import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions._

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql._

class FileCustomOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val path = propertiesWithCustom.get("path").getOrElse("/tmp/file-custom-step")
  lazy val createDifferentFiles = Try(propertiesWithCustom.getString("createDifferentFiles", "true").toBoolean)
    .getOrElse(true)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val finalPath = {
      if (createDifferentFiles)
        s"${path.toString}/${new Date().getTime}"
      else path.toString
    }

    dataFrame.write.json(finalPath)
  }
}
