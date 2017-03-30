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
import java.util.regex.Pattern

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.policy.files.JarFile
import spray.http.BodyPart

import scala.util.{Failure, Success, Try}

trait FileActorUtils extends SLF4JLogging {

  //The dir where the jars will be saved
  val targetDir: String
  val apiPath: String

  //Regexp for jar name validation
  lazy val jarFileName: Predicate[String] = Pattern.compile(""".*\.jar""").asPredicate()

  def deleteFiles(): Try[_] =
    Try {
      val pluginsDirectory = new File(targetDir)
      if (pluginsDirectory.exists && pluginsDirectory.isDirectory)
        pluginsDirectory.listFiles.filter(_.isFile).toList.foreach { file =>
          if (jarFileName.test(file.getName)) file.delete()
        }
    }

  def deleteFile(fileName: String): Try[_] =
    Try {
      val plugin = new File(s"$targetDir/$fileName")
      if (plugin.exists && !plugin.isDirectory)
        plugin.delete()
    }

  def browseDirectory(): Try[Seq[JarFile]] =
    Try {
      val pluginsDirectory = new File(targetDir)
      val formatter = new DecimalFormat("###.#")
      if (pluginsDirectory.exists && pluginsDirectory.isDirectory) {
        pluginsDirectory.listFiles.filter(_.isFile).toList.flatMap { file =>
          if (jarFileName.test(file.getName))
            Option(JarFile(file.getName, s"$url/${file.getName}", file.getAbsolutePath, sizeToMbFormat(file.length())))
          else None
        }
      } else Seq.empty[JarFile]
    }

  def uploadFiles(files: Seq[BodyPart]): Try[Seq[JarFile]] =
    Try {
      files.flatMap { file =>
        val fileNameOption = file.filename.orElse(file.name.orElse {
          log.warn(s"Is necessary one file name to upload plugins files")
          None
        })
        fileNameOption.flatMap { fileName =>
          if (jarFileName.test(fileName)) {
            val localMachineDir = s"$targetDir/$fileName"

            Try(saveFile(file.entity.data.toByteArray, localMachineDir)) match {
              case Success(newFile) =>
                Option(JarFile(fileName, s"$url/$fileName", localMachineDir, sizeToMbFormat(newFile.length())))
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

  private def sizeToMbFormat(size: Long) : String = {
    val formatter = new DecimalFormat("####.##")
    s"${formatter.format(size.toDouble / (1024 * 1024))} MB"
  }


  private def saveFile(array: Array[Byte], fileName: String): File = {
    log.info(s"Saving file to: $fileName")
    val bos = new BufferedOutputStream(new FileOutputStream(fileName))
    bos.write(array)
    bos.close()
    new File(fileName)
  }

  private def url: String = {
    val host = Try(InetAddress.getLocalHost.getHostName).getOrElse(SpartaConfig.apiConfig.get.getString("host"))
    val port = SpartaConfig.apiConfig.get.getInt("port")

    s"$host:$port/$apiPath"
  }
}
