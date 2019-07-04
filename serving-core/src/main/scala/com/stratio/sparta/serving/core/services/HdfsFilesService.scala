/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import java.io.File

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{AppConstant, MarathonConstant}
import com.stratio.sparta.serving.core.constants.AppConstant.SystemHadoopConfDir
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.utils.CheckpointUtils
import org.apache.commons.io.FileUtils
import org.apache.hadoop.fs.FileStatus

import scala.util.{Properties, Try}

case class HdfsFilesService() extends CheckpointUtils {

  lazy private val hdfsConfig = SpartaConfig.getHdfsConfig()
  lazy private val host = Try(hdfsConfig.get.getString(AppConstant.HdfsMaster)).toOption
  lazy private val port = Try(hdfsConfig.get.getInt(AppConstant.HdfsPort)).toOption
  lazy private val hdfsService = HdfsService()
  lazy private val instanceName = {
    val instancePrefix = Properties.envOrElse(
      MarathonConstant.ServerTenantName,
      AppConstant.instanceServiceName.getOrElse("")
    )

    if (instancePrefix.nonEmpty && !instancePrefix.endsWith("/")) s"$instancePrefix/" else instancePrefix
  }
  lazy private val pluginsLocation = {
    val pluginsLocationPrefix = Try(SpartaConfig.getDetailConfig().get.getString(AppConstant.PluginsLocation))
      .getOrElse(AppConstant.DefaultPluginsLocation)

    instanceName + pluginsLocationPrefix
  }
  lazy private val pluginJarPathParsed = s"${pluginsLocation.replace("hdfs://", "")}" +
    s"${if (pluginsLocation.endsWith("/")) "" else "/"}"
  lazy private val driverLocation = Try(SpartaConfig.getDetailConfig().get.getString(AppConstant.DriverPackageLocation))
    .getOrElse(AppConstant.DefaultDriverPackageLocation)

  def browsePlugins: Seq[FileStatus] =
    hdfsService.getFiles(pluginsLocation)

  def deletePlugin(file: String): Unit = {
    val pluginJarPathParsed = s"${pluginsLocation.replace("hdfs://", "")}" +
      s"${if (pluginsLocation.endsWith("/")) "" else "/"}"
    hdfsService.delete(pluginJarPathParsed + file)
  }

  def deletePlugins(): Unit =
    browsePlugins.foreach(plugin => if (plugin.isFile) deletePlugin(plugin.getPath.getName))


  def uploadDriverFile(driverJarPath: String): String = {
    val driverJar = JarsHelper.findDriverByPath(new File(driverLocation)).head

    log.debug(s"Uploading driver jar ($driverJar) to HDFS cluster ...")

    val driverJarPathParsed = s"$instanceName${driverJarPath.replace("hdfs://", "")}" +
      s"${if (driverJarPath.endsWith("/")) "" else "/"}"

    uploadJarFile(driverJar, driverJarPathParsed)
  }

  def downloadPluginFile(fileName: String, temporalDir: String): String = {
    val fileUri = pluginJarPathParsed + fileName
    val file = new File(s"$temporalDir/$fileName")
    log.debug(s"Downloading HDFS file to local file system: ${file.getAbsoluteFile}")
    val inputStream = hdfsService.getFile(fileUri)
    FileUtils.copyInputStreamToFile(inputStream, file)
    file.getPath
  }

  def uploadPluginFile(localPath: String): String = {
    val pluginLocalJar = new File(localPath)

    log.debug(s"Uploading plugin jar (${pluginLocalJar.getName}) to HDFS cluster ...")

    uploadJarFile(pluginLocalJar, pluginJarPathParsed)
  }

  private[core] def uploadJarFile(localFile: File, destinationPathInHdfs: String) = {
    hdfsService.write(localFile.getAbsolutePath, destinationPathInHdfs, overwrite = true)
    val hdfsPath = hdfsService.getFiles(s"$destinationPathInHdfs${localFile.getName}").head.getPath.toUri.getPath
    val uploadedFilePath = {
      if (isHadoopEnvironmentDefined) s"hdfs://"
      else if (host.isDefined && port.isDefined) s"hdfs://${host.get}:${port.get}" else ""
    } + hdfsPath

    log.debug(s"File successfully uploaded to HDFS: $uploadedFilePath")

    uploadedFilePath
  }

  private[core] def isHadoopEnvironmentDefined: Boolean =
    Option(System.getenv(SystemHadoopConfDir)) match {
      case Some(_) => true
      case None => false
    }
}
