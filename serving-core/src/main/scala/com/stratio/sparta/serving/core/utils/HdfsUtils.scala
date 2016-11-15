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
import com.stratio.sparta.serving.core.constants.AppConstant
import com.typesafe.config.Config
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataOutputStream, FileStatus, FileSystem, Path}
import org.apache.hadoop.security.UserGroupInformation

import scala.util.Try

case class HdfsUtils(dfs: FileSystem, userName: String, ugiOption: Option[UserGroupInformation] = None) {

  def getFiles(path: String): Array[FileStatus] = dfs.listStatus(new Path(path))

  def getFile(filename: String): InputStream = dfs.open(new Path(filename))

  def delete(path: String): Unit = dfs.delete(new Path(path), true)

  def write(path: String, destPath: String, overwrite: Boolean = false): Int = {
    val file = new File(path)

    val out = ugiOption match {
      case Some(ugi) =>
        ugi.doAs(new PrivilegedExceptionAction[FSDataOutputStream]() {
          override def run(): FSDataOutputStream = {
            dfs.create(new Path(s"$destPath${file.getName}"))
          }
        })
      case None =>
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

  def hdfsConfiguration(userName: String, configOpt: Option[Config]): Configuration = {
    val DefaultFSProperty = "fs.defaultFS"
    val HdfsDefaultPort = 9000

    val HdfsDefaultMaster = "127.0.0.1"
    val conf = new Configuration()

    Option(System.getenv(AppConstant.HadoopConfDir)) match {
      case Some(confDir) =>
        log.info(s"The Hadoop configuration is read from directory files in the path $confDir")
      case None =>
        configOpt.foreach { config =>
          val master = Try(config.getString(AppConstant.HdfsMaster)).getOrElse(HdfsDefaultMaster)
          val port = Try(config.getInt(AppConstant.HdfsPort)).getOrElse(HdfsDefaultPort)
          val hdfsPath = s"hdfs://$master:$port/user/$userName/sparta"

          conf.set(DefaultFSProperty, hdfsPath)

          log.info(s"The Hadoop configuration is assigned with $DefaultFSProperty with value: $hdfsPath")
        }
    }
    conf
  }

  def apply(conf: Configuration,
            userName: String,
            principalNameOption: Option[String],
            keytabPathOption: Option[String]): HdfsUtils = {
    Option(System.getenv(AppConstant.HadoopConfDir)).foreach(
      hadoopConfDir => {
        val hdfsCoreSitePath = new Path(s"$hadoopConfDir/core-site.xml")
        val hdfsHDFSSitePath = new Path(s"$hadoopConfDir/hdfs-site.xml")
        val yarnSitePath = new Path(s"$hadoopConfDir/yarn-site.xml")

        conf.addResource(hdfsCoreSitePath)
        conf.addResource(hdfsHDFSSitePath)
        conf.addResource(yarnSitePath)
      }
    )

    val ugi =
      if (principalNameOption.isDefined && keytabPathOption.isDefined) {
        val principalName = principalNameOption.getOrElse(
          throw new IllegalStateException(s"${AppConstant.PrincipalName} can not be null"))
        val keytabPath = keytabPathOption.getOrElse(
          throw new IllegalStateException(s"${AppConstant.KeytabPath} can not be null"))

        UserGroupInformation.setConfiguration(conf)
        Option(UserGroupInformation.loginUserFromKeytabAndReturnUGI(principalName, keytabPath))
      } else None

    new HdfsUtils(FileSystem.get(conf), userName, ugi)
  }

  def apply(config: Option[Config]): HdfsUtils = {
    val userName = Try(config.get.getString(AppConstant.HadoopUserName)).getOrElse(AppConstant.DefaultHdfsUser)
    val principalName: Option[String] =
      Try(config.get.getString(AppConstant.PrincipalName)).toOption.flatMap(x => if (x == "") None else Some(x))
    val keytabPath: Option[String] =
      Try(config.get.getString(AppConstant.KeytabPath)).toOption.flatMap(x => if (x == "") None else Some(x))
    apply(hdfsConfiguration(userName, config), userName, principalName, keytabPath)
  }
}
