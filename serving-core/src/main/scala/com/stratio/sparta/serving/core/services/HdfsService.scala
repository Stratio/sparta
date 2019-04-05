/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import java.io._
import java.security.PrivilegedExceptionAction

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.AggregationTimeHelper
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import com.stratio.sparta.serving.core.factory.SparkContextFactory.maybeWithHdfsUgiService
import com.typesafe.config.Config
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.hadoop.security.UserGroupInformation

import scala.concurrent.duration._
import scala.util.{Failure, Properties, Success, Try}


case class HdfsService(dfs: FileSystem, ugiOption: Option[UserGroupInformation]) extends SLF4JLogging {

  lazy private val hdfsConfig: Option[Config] = SpartaConfig.getHdfsConfig()

  def reLogin(): Unit = {
    ugiOption.foreach { ugi =>
      ugi.checkTGTAndReloginFromKeytab()

      maybeWithHdfsUgiService {
        SparkContextFactory.getXDSession().foreach(xdSession => xdSession.sql("REFRESH DATABASES"))
        SparkContextFactory.getXDSession().foreach(xdSession => xdSession.sql("REFRESH TABLES"))
      }
    }
  }

  def runFunction(function: ⇒ Unit): Unit =
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

  def getFiles(path: String): Array[FileStatus] =
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

  def getFile(filename: String): InputStream =
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

  def delete(path: String): Unit =
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

  def write(path: String, destPath: String, overwrite: Boolean = false): Int = {
    val file = new File(path)
    val out = ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[FSDataOutputStream]() {
          override def run(): FSDataOutputStream = {
            log.debug(s"Creating Hdfs file with security from path: $path and destination: $destPath")
            dfs.create(new Path(s"$destPath${file.getName}"), overwrite)
          }
        })
      case None =>
        log.debug(s"Creating Hdfs file without security from path: $path and destination: $destPath")
        dfs.create(new Path(s"$destPath${file.getName}"), overwrite)
    }

    val in = new BufferedInputStream(new FileInputStream(file))

    try {
      IOUtils.copy(in, out)
    } finally {
      IOUtils.closeQuietly(out)
      IOUtils.closeQuietly(in)
    }
  }

  def writeBinary(inputData: Array[Byte], destPath: String, overwrite: Boolean = false, failOnException: Boolean = true): Int = {
    val emptySize = 0

    val out = ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[FSDataOutputStream]() {
          override def run(): FSDataOutputStream = {
            log.debug(s"Creating Hdfs file with security whose destination path is: $destPath")
            dfs.create(new Path(s"$destPath"), overwrite)
          }
        })
      case None =>
        log.debug(s"Creating Hdfs file without security whose destination path is: $destPath")
        dfs.create(new Path(s"$destPath"), overwrite)
    }

    val in = new ByteArrayInputStream(inputData)

    try {
      IOUtils.copy(in, out)
    }
    catch {
      case e: Exception =>
        if (failOnException) throw e
        else {
          log.error(s"Cannot write to file $destPath . Error: ${e.getLocalizedMessage}")
          emptySize
        }
    }
    finally {
      IOUtils.closeQuietly(out)
      IOUtils.closeQuietly(in)
    }
  }



  def runReloaderKeyTab(): Unit = {
    val reloadKeyTab = Try(hdfsConfig.get.getBoolean(ReloadKeyTab)).getOrElse(DefaultReloadKeyTab)

    if (reloadKeyTab) {
      import scala.concurrent.ExecutionContext.Implicits.global
      val reloadTime = Try(hdfsConfig.get.getString(ReloadKeyTabTime)).toOption.notBlank
        .getOrElse(DefaultReloadKeyTabTime)

      log.info(s"Initializing keyTab reload task with time: $reloadTime")

      SchedulerSystem.scheduler.schedule(0 seconds,
        AggregationTimeHelper.parseValueToMilliSeconds(reloadTime) milli)(reLogin())
    }
  }
}

object HdfsService extends SLF4JLogging {

  lazy val hdfsConfig = SpartaConfig.getHdfsConfig()
  lazy val configuration  = hdfsConfiguration(hdfsConfig)

  def getPrincipalName(hdfsConfig: Option[Config]): Option[String] =
    Option(System.getenv(SystemPrincipalName)).orElse(Try(hdfsConfig.get.getString(PrincipalName)).toOption.notBlank)

  def getKeyTabPath(hdfsConfig: Option[Config]): Option[String] =
    Option(System.getenv(SystemKeyTabPath)).orElse(Try(hdfsConfig.get.getString(KeytabPath)).toOption.notBlank)


  def apply(): HdfsService = {
    log.debug("Creating HDFS connection ...")

    Option(System.getenv(SystemHadoopConfDir)).foreach { hadoopConfDir =>
      Try {
        val hdfsCoreSitePath = s"$hadoopConfDir/$CoreSite"

        log.debug(s"Adding resource $CoreSite from path: $hdfsCoreSitePath")

        configuration.addResource(new Path(hdfsCoreSitePath))
      } match {
        case Success(_) =>
          log.debug(s"Resource $CoreSite added correctly")
        case Failure(e) =>
          log.error(s"Resource $CoreSite not added", e)
          throw e
      }

      Try {
        val hdfsHDFSSitePath = s"$hadoopConfDir/$HDFSSite"

        log.debug(s"Adding resource $HDFSSite from path: $hdfsHDFSSitePath")

        configuration.addResource(new Path(hdfsHDFSSitePath))
      } match {
        case Success(_) =>
          log.debug(s"Resource $HDFSSite added correctly")
        case Failure(e) =>
          log.error(s"Resource $HDFSSite not added", e)
          throw e
      }
    }

    val ugi = (getPrincipalName(hdfsConfig), getKeyTabPath(hdfsConfig)) match {
      case (Some(principalName), Some(keyTabPath)) =>
        log.debug("Obtaining UGI from principal, keyTab and configuration files")
        Option(getUGI(principalName, keyTabPath, configuration))
      case _ => None
    }

    val hdfsService = new HdfsService(FileSystem.get(configuration), ugi)

    ugi.foreach(_ => hdfsService.runReloaderKeyTab())

    hdfsService
  }

  private def hdfsConfiguration(hdfsConfig: Option[Config]): Configuration = {
    log.debug("Creating HDFS configuration...")

    val conf = new Configuration()

    Option(System.getenv(SystemHadoopConfDir)) match {
      case Some(confDir) =>
        log.debug(s"The HDFS configuration have been created for read files with conf located at: $confDir")
      case None =>
        hdfsConfig.foreach { config =>
          if (config.hasPath(HdfsMaster) && config.hasPath(HdfsPort)) {
            val master = config.getString(HdfsMaster)
            val port = config.getInt(HdfsPort)
            val hdfsPath = s"hdfs://$master:$port"

            Properties.envOrNone(SystemHadoopUserName)
              .orElse(Try(config.getString(HadoopUserName)).toOption.notBlank)
              .foreach { user =>
                System.setProperty(SystemHadoopUserName, user)
              }

            conf.set(DefaultFSProperty, hdfsPath)

            log.debug(s"The HDFS configuration have been assigned with $DefaultFSProperty and located at: $hdfsPath")
          } else log.debug(s"The HDFS configuration have been created for read files in the local filesystem")
        }
    }
    conf
  }

  private def getUGI(principalName: String, keyTabPath: String, conf: Configuration): UserGroupInformation = {
    log.debug("Setting configuration for Hadoop with secured connection")

    UserGroupInformation.setConfiguration(conf)

    log.debug(s"Login user with principal name: $principalName and keyTab: $keyTabPath")

    val ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principalName, keyTabPath)

    UserGroupInformation.setLoginUser(ugi)

    ugi
  }
}