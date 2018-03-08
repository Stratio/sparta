/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import akka.event.slf4j.SLF4JLogging
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import com.stratio.sparta.serving.core.constants.AppConstant.version
import com.stratio.sparta.serving.core.models.info.AppInfo

import scala.io.Source
import scala.util._

object InfoHelper extends SLF4JLogging {

  val devContact = "sparta@stratio.com"
  val supportContact = "support@stratio.com"
  val description = " Real Time Analytics and Data Pipelines based on Spark Streaming"
  val license = Try {
    Source.fromInputStream(InfoHelper.getClass.getClassLoader.getResourceAsStream("LICENSE.txt")).mkString
  }.getOrElse("")

  def getAppInfo: AppInfo = {
    Try(Source.fromInputStream(InfoHelper.getClass.getClassLoader.getResourceAsStream("version.txt")).getLines) match {
      case Success(lines) =>
        val pomVersion = lines.next()
        val profileId = lines.next()
        val timestamp = lines.next()
        val pomParsed = if (pomVersion != "${project.version}") pomVersion else version
        val profileIdParsed = if (profileId != "${profile.id}") profileId else ""
        val timestampParsed = {
          if (timestamp != "${timestamp}") timestamp
          else {
            val format = DateTimeFormat.forPattern("yyyy-MM-dd-hh:mm")
            format.print(DateTime.now)
          }
        }

        AppInfo(pomParsed, profileIdParsed, timestampParsed, devContact, supportContact, description, license)
      case Failure(e) =>
        log.error("Cannot get version info", e)
        throw e
    }
  }
}
