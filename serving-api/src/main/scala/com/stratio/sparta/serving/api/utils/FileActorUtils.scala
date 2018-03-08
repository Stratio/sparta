/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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

import scala.util.{Failure, Properties, Success, Try}

trait FileActorUtils extends SLF4JLogging {

  //The dir where the files will be saved
  val targetDir: String

  //The dir where the files will be saved
  val temporalDir: String

  //Api path to return the URL to obtain the file
  val apiPath: String

  def deleteFiles(): Try[Unit] =
    Try {
      val directory = new File(targetDir)
      if (directory.exists && directory.isDirectory)
        directory.listFiles.filter(_.isFile).toList.foreach { file =>
            file.delete()
        }
    }

  def deleteFile(fileName: String): Try[Unit] =
    Try {
      val plugin = new File(s"$targetDir/$fileName")
      if (plugin.exists && !plugin.isDirectory)
        plugin.delete()
    }

  def browseDirectory(): Try[Seq[SpartaFile]] =
    Try {
      val directory = new File(targetDir)
      if (directory.exists && directory.isDirectory) {
        directory.listFiles.filter(_.isFile).toList.map { file =>
            SpartaFile(file.getName, s"$url/${file.getName}", file.getAbsolutePath)
        }
      } else Seq.empty[SpartaFile]
    }

  def browseFile(filePath: String): Try[SpartaFile] =
    Try {
      val file = new File(filePath)
      if (file.exists && file.isFile) {
        SpartaFile(file.getName, s"$url/${file.getName}", file.getAbsolutePath)
      } else throw new Exception(s"The file $filePath is corrupted")
    }

  def uploadFiles(files: Seq[BodyPart], useTemporalDirectory: Boolean = false): Try[Seq[SpartaFile]] =
    Try {
      files.flatMap { file =>
        val fileNameOption = file.filename.orElse(file.name.orElse {
          log.warn(s"It is necessary a name to upload the file")
          None
        })
        fileNameOption.flatMap { fileName =>
            val localMachineDir = {
              if(useTemporalDirectory) s"$temporalDir/$fileName"
              else s"$targetDir/$fileName"
            }

            Try(saveFile(file.entity.data.toByteArray, localMachineDir)) match {
              case Success(newFile) =>
                Option(SpartaFile(fileName, s"$url/$fileName", localMachineDir))
              case Failure(e) =>
                log.error(s"Error saving file in path $localMachineDir", e)
                None
            }
        }
      }
    }

  def url: String = {
    val marathonLB_host = Properties.envOrElse("MARATHON_APP_LABEL_HAPROXY_0_VHOST", "")
    val marathonLB_path = Properties.envOrElse("MARATHON_APP_LABEL_HAPROXY_0_PATH", "")

    if (marathonLB_host.nonEmpty && marathonLB_path.nonEmpty)
      s"https://$marathonLB_host$marathonLB_path/$apiPath"
    else {
      val protocol = {
        if(Try(Properties.envOrElse("SECURITY_TLS_ENABLE", "false").toBoolean).getOrElse(false)) "https://"
        else "http://"
      }
      val host = Try(InetAddress.getLocalHost.getHostName).getOrElse(SpartaConfig.apiConfig.get.getString("host"))
      val port = SpartaConfig.apiConfig.get.getInt("port")

      s"$protocol$host:$port/${HttpConstant.SpartaRootPath}/$apiPath"
    }
  }

  private def saveFile(array: Array[Byte], fileName: String): File = {
    log.debug(s"Saving file to: $fileName")
    new File(fileName).getParentFile.mkdirs
    val bos = new BufferedOutputStream(new FileOutputStream(fileName))
    try {
      bos.write(array)
    } finally {
      bos.close()
    }
    new File(fileName)
  }
}
