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

package com.stratio.sparta.serving.core.utils

import java.io._
import java.security.PrivilegedExceptionAction

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileStatus, FileSystem, Path}
import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.duration._
import scala.util.Try

case class HdfsUtils(dfs: FileSystem, userName: String, ugiOption: Option[UserGroupInformation] = None)
  extends SLF4JLogging {

  def reLogin(): Unit = ugiOption.foreach(ugi => ugi.reloginFromKeytab())

  def getFiles(path: String): Array[FileStatus] = {
    ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[Array[FileStatus]]() {
          override def run(): Array[FileStatus] = {
            log.debug(s"Listing Hdfs status for path with security: $path")
            dfs.listStatus(new Path(path))
          }
        })
      case None =>
        log.debug(s"Listing Hdfs status for path without security: $path")
        dfs.listStatus(new Path(path))
    }
  }

  def getFile(filename: String): InputStream = {
    ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[FSDataInputStream]() {
          override def run(): FSDataInputStream = {
            log.debug(s"Getting Hdfs file with security: $filename")
            dfs.open(new Path(filename))
          }
        })
      case None =>
        log.debug(s"Getting Hdfs file without security: $filename")
        dfs.open(new Path(filename))
    }
  }

  def delete(path: String): Unit = {
    ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[Boolean]() {
          override def run(): Boolean = {
            log.debug(s"Deleting Hdfs path with security: $path")
            dfs.delete(new Path(path), true)
          }
        })
      case None =>
        log.debug(s"Deleting Hdfs path without security: $path")
        dfs.delete(new Path(path), true)
    }
  }

  def write(path: String, destPath: String, overwrite: Boolean = false): Int = {
    val file = new File(path)
    val out = ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[FSDataOutputStream]() {
          override def run(): FSDataOutputStream = {
            log.debug(s"Creating Hdfs file with security: Path $path and destination path $destPath")
            dfs.create(new Path(s"$destPath${file.getName}"))
          }
        })
      case None =>
        log.debug(s"Creating Hdfs file without security: Path $path and destination path $destPath")
        dfs.create(new Path(s"$destPath${file.getName}"))
    }

    val in = new BufferedInputStream(new FileInputStream(file))
    val bytesCopied = Try(IOUtils.copy(in, out))
    IOUtils.closeQuietly(in)
    IOUtils.closeQuietly(out)
    bytesCopied.get
  }
}

object HdfsUtils extends SLF4JLogging {

  def runReloaderKeyTab(hdfsUtils: HdfsUtils): Unit = {
    val hdfsConfig = SpartaConfig.getHdfsConfig
    val reloadKeyTab = Try(hdfsConfig.get.getBoolean(AppConstant.ReloadKeyTab))
      .getOrElse(AppConstant.DefaultReloadKeyTab)
    if(reloadKeyTab) {
      import scala.concurrent.ExecutionContext.Implicits.global
      val reloadKeyTabTime = Try(hdfsConfig.get.getString(AppConstant.ReloadKeyTabTime)).toOption
        .flatMap(x => if (x == "") None else Some(x)).getOrElse(AppConstant.DefaultReloadKeyTabTime)

      log.info(s"Initializing reload keyTab task with time: $reloadKeyTabTime")

      AppConstant.SchedulerSystem.scheduler.schedule(0 seconds,
        AggregationTime.parseValueToMilliSeconds(reloadKeyTabTime) milli)(hdfsUtils.reLogin())
    }
  }

  def hdfsConfiguration(userName: String): Configuration = {
    log.info("Creating HDFS configuration...")

    val DefaultFSProperty = "fs.defaultFS"
    val HdfsDefaultPort = 9000
    val hdfsConfig = SpartaConfig.getHdfsConfig

    val HdfsDefaultMaster = "127.0.0.1"
    val conf = new Configuration()

    Option(System.getenv(AppConstant.SystemHadoopConfDir)) match {
      case Some(confDir) =>
        log.info(s"The Hadoop configuration is read from directory files in the path $confDir")
      case None =>
        hdfsConfig.foreach { config =>
          val master = Try(config.getString(AppConstant.HdfsMaster)).getOrElse(HdfsDefaultMaster)
          val port = Try(config.getInt(AppConstant.HdfsPort)).getOrElse(HdfsDefaultPort)
          val hdfsPath = s"hdfs://$master:$port/user/$userName/sparta"

          conf.set(DefaultFSProperty, hdfsPath)

          log.info(s"The Hadoop configuration is assigned with $DefaultFSProperty with value: $hdfsPath")
        }
    }
    conf
  }

  def getUserName: String = Option(System.getenv(AppConstant.SystemHadoopUserName))
    .getOrElse {
      val hdfsConfig = SpartaConfig.getHdfsConfig

      Try(hdfsConfig.get.getString(AppConstant.HadoopUserName)).toOption
        .flatMap(x => if (x == "") None else Some(x)).getOrElse(AppConstant.DefaultHdfsUser)
    }

  def getPrincipalName: Option[String] =
    Option(System.getenv(AppConstant.SystemPrincipalName)).orElse {
      val hdfsConfig = SpartaConfig.getHdfsConfig
      val principalNameSuffix = Try(hdfsConfig.get.getString(AppConstant.PrincipalNameSuffix)).toOption
        .flatMap(x => if (x == "") None else Some(x))
      val principalNamePrefix = Try(hdfsConfig.get.getString(AppConstant.PrincipalNamePrefix)).toOption
        .flatMap(x => if (x == "") None else Some(x))
      val hostName = Option(System.getenv(AppConstant.SystemHostName))
      (principalNamePrefix, principalNameSuffix, hostName) match {
        case (Some(prefix), Some(suffix), Some(host)) => Some(s"$prefix$host$suffix")
        case _ =>
          Try(hdfsConfig.get.getString(AppConstant.PrincipalName)).toOption
            .flatMap(x => if (x == "") None else Some(x))
      }
    }

  def getKeyTabPath: Option[String] =
    Option(System.getenv(AppConstant.SystemKeyTabPath)).orElse {
      val hdfsConfig = SpartaConfig.getHdfsConfig

      Try(hdfsConfig.get.getString(AppConstant.KeytabPath)).toOption.flatMap(x => if (x == "") None else Some(x))
    }

  private def getUGI(principalName: String, keyTabPath: String, conf: Configuration): UserGroupInformation = {
    log.info("Setting configuration for Hadoop Kerberized connection")
    UserGroupInformation.setConfiguration(conf)
    log.info(s"Login User with principal name: $principalName and keyTab: $keyTabPath")
    val ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principalName, keyTabPath)
    UserGroupInformation.setLoginUser(ugi)

    ugi
  }

  def apply(conf: Configuration,
            userName: String,
            principalNameOption: Option[String],
            keytabPathOption: Option[String]): HdfsUtils = {

    log.info("Creating HDFS connection...")

    Option(System.getenv(AppConstant.SystemHadoopConfDir)).foreach(
      hadoopConfDir => {
        val hdfsCoreSitePath = new Path(s"$hadoopConfDir/core-site.xml")
        val hdfsHDFSSitePath = new Path(s"$hadoopConfDir/hdfs-site.xml")
        //val yarnSitePath = new Path(s"$hadoopConfDir/yarn-site.xml")

        conf.addResource(hdfsCoreSitePath)
        conf.addResource(hdfsHDFSSitePath)
        //conf.addResource(yarnSitePath)
      }
    )

    val ugi = (principalNameOption, keytabPathOption) match {
      case (Some(principalName), Some(keyTabPath)) =>
        Option(getUGI(principalName, keyTabPath, conf))
      case _ => None
    }
    val hdfsUtils = new HdfsUtils(FileSystem.get(conf), userName, ugi)

    if (ugi.isDefined) runReloaderKeyTab(hdfsUtils)

    hdfsUtils
  }

  def apply(): HdfsUtils = {
    val userName = getUserName
    val principalName = getPrincipalName
    val keytabPath = getKeyTabPath

    apply(hdfsConfiguration(userName), userName, principalName, keytabPath)
  }
}