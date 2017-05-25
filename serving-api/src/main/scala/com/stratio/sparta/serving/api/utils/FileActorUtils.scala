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

package com.stratio.sparta.serving.api.utils

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.net.InetAddress
import java.text.DecimalFormat
import java.util.function.Predicate

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.files.SpartaFile
import spray.http.BodyPart

import scala.util.{Failure, Success, Try}

trait FileActorUtils extends SLF4JLogging {

  //The dir where the files will be saved
  val targetDir: String
  val apiPath: String

  //Regexp for name validation
  val patternFileName: Option[Predicate[String]] = None

  def deleteFiles(): Try[_] =
    Try {
      val directory = new File(targetDir)
      if (directory.exists && directory.isDirectory)
        directory.listFiles.filter(_.isFile).toList.foreach { file =>
          if (patternFileName.isEmpty || (patternFileName.isDefined && patternFileName.get.test(file.getName)))
            file.delete()
        }
    }

  def deleteFile(fileName: String): Try[_] =
    Try {
      val plugin = new File(s"$targetDir/$fileName")
      if (plugin.exists && !plugin.isDirectory)
        plugin.delete()
    }

  def browseDirectory(): Try[Seq[SpartaFile]] =
    Try {
      val directory = new File(targetDir)
      if (directory.exists && directory.isDirectory) {
        directory.listFiles.filter(_.isFile).toList.flatMap { file =>
          if (patternFileName.isEmpty || (patternFileName.isDefined && patternFileName.get.test(file.getName)))
            Option(SpartaFile(file.getName, s"$url/${file.getName}", file.getAbsolutePath,
              sizeToMbFormat(file.length())))
          else None
        }
      } else Seq.empty[SpartaFile]
    }

  def uploadFiles(files: Seq[BodyPart]): Try[Seq[SpartaFile]] =
    Try {
      files.flatMap { file =>
        val fileNameOption = file.filename.orElse(file.name.orElse {
          log.warn(s"Is necessary one file name to upload files")
          None
        })
        fileNameOption.flatMap { fileName =>
          if (patternFileName.isEmpty || (patternFileName.isDefined && patternFileName.get.test(fileName))) {
            val localMachineDir = s"$targetDir/$fileName"

            Try(saveFile(file.entity.data.toByteArray, localMachineDir)) match {
              case Success(newFile) =>
                Option(SpartaFile(fileName, s"$url/$fileName", localMachineDir, sizeToMbFormat(newFile.length())))
              case Failure(e) =>
                log.error(s"Error saving file in path $localMachineDir", e)
                None
            }
          } else {
            log.warn(s"$fileName is Not a valid file name")
            None
          }
        }
      }
    }

  private def sizeToMbFormat(size: Long): String = {
    val formatter = new DecimalFormat("####.##")
    s"${formatter.format(size.toDouble / (1024 * 1024))} MB"
  }

  private def saveFile(array: Array[Byte], fileName: String): File = {
    log.info(s"Saving file to: $fileName")
    new File(fileName).getParentFile.mkdirs
    val bos = new BufferedOutputStream(new FileOutputStream(fileName))
    bos.write(array)
    bos.close()
    new File(fileName)
  }

  private def url: String = {
    val host = Try(InetAddress.getLocalHost.getHostName).getOrElse(SpartaConfig.apiConfig.get.getString("host"))
    val port = SpartaConfig.apiConfig.get.getInt("port")

    s"http://$host:$port/${HttpConstant.SpartaRootPath}/$apiPath"
  }
}
