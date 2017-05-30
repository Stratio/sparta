/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            val format = DateTimeFormat.forPattern("yyyy-MM-dd-hh:mm:ss")
            format.print(DateTime.now)
          }
        }
        AppInfo(pomParsed, profileIdParsed, timestampParsed, devContact, supportContact, license)
      case Failure(e) =>
        log.error("Cannot get version info", e)
        throw e
    }
  }
}
