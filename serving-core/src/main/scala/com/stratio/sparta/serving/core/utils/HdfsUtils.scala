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
import com.stratio.sparta.sdk.utils.AggregationTimeUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileStatus, FileSystem, Path}
import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

case class HdfsUtils(dfs: FileSystem, userName: String, ugiOption: Option[UserGroupInformation] = None)
  extends SLF4JLogging {

  def reLogin(): Unit = ugiOption.foreach(ugi => ugi.checkTGTAndReloginFromKeytab())

  def runFunction(function: ⇒ Unit): Unit = {
    ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[Unit]() {
          override def run(): Unit = {
            log.debug(s"Executing function with HDFS security")
            function
          }
        })
      case None =>
        log.debug(s"Executing function without HDFS security")
        function
    }
  }

  def getFiles(path: String): Array[FileStatus] = {
    ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[Array[FileStatus]]() {
          override def run(): Array[FileStatus] = {
            log.debug(s"Listing Hdfs statuses with security: $path")
            dfs.listStatus(new Path(path))
          }
        })
      case None =>
        log.debug(s"Listing Hdfs statuses without security: $path")
        dfs.listStatus(new Path(path))
    }
  }

  def getFile(filename: String): InputStream = {
    ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[FSDataInputStream]() {
          override def run(): FSDataInputStream = {
            log.debug(s"Getting Hdfs with security: $filename")
            dfs.open(new Path(filename))
          }
        })
      case None =>
        log.debug(s"Getting Hdfs without security: $filename")
        dfs.open(new Path(filename))
    }
  }

  def delete(path: String): Unit = {
    ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[Boolean]() {
          override def run(): Boolean = {
            log.debug(s"Deleting Hdfs with security: $path")
            dfs.delete(new Path(path), true)
          }
        })
      case None =>
        log.debug(s"Deleting Hdfs without security: $path")
        dfs.delete(new Path(path), true)
    }
  }

  def write(path: String, destPath: String, overwrite: Boolean = false): Int = {
    val file = new File(path)
    val out = ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[FSDataOutputStream]() {
          override def run(): FSDataOutputStream = {
            log.debug(s"Creating Hdfs file with security in path: $path and destination path: $destPath")
            dfs.create(new Path(s"$destPath${file.getName}"))
          }
        })
      case None =>
        log.debug(s"Creating Hdfs file without security in path: $path and destination path: $destPath")
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

      log.info(s"Initializing keyTab reload task with time: $reloadKeyTabTime")

      AppConstant.SchedulerSystem.scheduler.schedule(0 seconds,
        AggregationTimeUtils.parseValueToMilliSeconds(reloadKeyTabTime) milli)(hdfsUtils.reLogin())
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
        log.info(s"The Hadoop configuration was read from directory files located at: $confDir")
      case None =>
        hdfsConfig.foreach { config =>
          val master = Try(config.getString(AppConstant.HdfsMaster)).getOrElse(HdfsDefaultMaster)
          val port = Try(config.getInt(AppConstant.HdfsPort)).getOrElse(HdfsDefaultPort)
          val hdfsPath = s"hdfs://$master:$port/user/$userName/sparta"

          conf.set(DefaultFSProperty, hdfsPath)

          log.info(s"The Hadoop configuration was assigned with $DefaultFSProperty and located at: $hdfsPath")
        }
    }
    conf
  }

  def getUserName: String = Option(System.getenv(AppConstant.SystemHadoopUserName))
    .getOrElse {
      val hdfsConfig = SpartaConfig.getHdfsConfig
      val userName = Try(hdfsConfig.get.getString(AppConstant.HadoopUserName)).toOption
        .flatMap(x => if (x == "") None else Some(x)).getOrElse(AppConstant.DefaultHdfsUser)

      log.info(s"Connecting to HDFS with user name: $userName")
      userName
    }

  def getPrincipalName: Option[String] =
    Option(System.getenv(AppConstant.SystemPrincipalName)).orElse {
      val hdfsConfig = SpartaConfig.getHdfsConfig
      val principalName = Try(hdfsConfig.get.getString(AppConstant.PrincipalName)).toOption
            .flatMap(x => if (x == "") None else Some(x))
      log.info(s"Kerberos with principal name: $principalName")
      principalName
    }

  def getKeyTabPath: Option[String] =
    Option(System.getenv(AppConstant.SystemKeyTabPath)).orElse {
      val hdfsConfig = SpartaConfig.getHdfsConfig
      val keyTabPath = Try(hdfsConfig.get.getString(AppConstant.KeytabPath)).toOption.flatMap(x =>
        if (x == "") None else Some(x))

      log.info(s"Kerberos with keyTabPath: $keyTabPath")
      keyTabPath
    }

  private def getUGI(principalName: String, keyTabPath: String, conf: Configuration): UserGroupInformation = {
    log.info("Setting configuration for Hadoop Kerberized connection")
    UserGroupInformation.setConfiguration(conf)
    log.info(s"Login user with principal name: $principalName and keyTab: $keyTabPath")
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
        Try {
          val hdfsCoreSitePath = s"$hadoopConfDir/${AppConstant.CoreSite}"
          log.info(s"Adding resource ${AppConstant.CoreSite} from path: $hdfsCoreSitePath")
          val hdfsCoreSite = new Path(hdfsCoreSitePath)
          conf.addResource(hdfsCoreSite)
        } match {
          case Success(_) => log.info(s"Resource ${AppConstant.CoreSite} added correctly")
          case Failure(e) =>
            log.error(s"Resource ${AppConstant.CoreSite} not added. Error: ${e.getLocalizedMessage}")
            throw e
        }

        Try {
          val hdfsHDFSSitePath = s"$hadoopConfDir/${AppConstant.HDFSSite}"
          log.info(s"Adding resource ${AppConstant.HDFSSite} from path: $hdfsHDFSSitePath")
          val hdfsHDFSSite = new Path(hdfsHDFSSitePath)
          conf.addResource(hdfsHDFSSite)
        } match {
          case Success(_) => log.info(s"Resource ${AppConstant.HDFSSite} added correctly")
          case Failure(e) =>
            log.error(s"Resource ${AppConstant.HDFSSite} not added. Error: ${e.getLocalizedMessage}")
            throw e
        }
      }
    )

    val ugi = (principalNameOption, keytabPathOption) match {
      case (Some(principalName), Some(keyTabPath)) =>
        log.info("Obtaining UGI from principal, keyTab and configuration files")
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