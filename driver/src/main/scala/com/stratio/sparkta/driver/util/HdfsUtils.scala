/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.util

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import java.security.PrivilegedExceptionAction

import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.hadoop.security.UserGroupInformation

class HdfsUtils(ugi: UserGroupInformation, dfs: FileSystem) {

  def getFiles(path: String): Array[FileStatus] = {
    ugi.doAs(new PrivilegedExceptionAction[Array[FileStatus]]() {
      def run: Array[FileStatus] = {
        dfs.listStatus(new Path(path))
      }
    })
  }

  def getFile(filename: String): InputStream = {
    ugi.doAs(new PrivilegedExceptionAction[InputStream]() {
      def run: FSDataInputStream = {
        val path = new Path(filename)
        dfs.open(path)
      }
    })
  }

  def write(path: String, destPath: String, overwrite: Boolean = false): Unit = {
    ugi.doAs(new PrivilegedExceptionAction[Unit]() {
      def run: Unit = {
        val file = new File(path)
        val out = dfs.create(new Path(s"$destPath${file.getName}"))
        val in = new BufferedInputStream(new FileInputStream(file))

        IOUtils.copy(in, out)

        IOUtils.closeQuietly(in)
        IOUtils.closeQuietly(out)
      }
    })
  }

}

object HdfsUtils {

  def hdfsConfiguration(hadoopConfDir: Option[String]): FileSystem = {
    val conf = new Configuration()
    if (hadoopConfDir.isDefined) {
      val confDir = if (hadoopConfDir.get.endsWith("/")) hadoopConfDir.get else hadoopConfDir.get + "/"
      val hdfsCoreSitePath = new FileInputStream(new File(confDir + "core-site.xml"))
      val hdfsHDFSSitePath = new FileInputStream(new File(confDir + "hdfs-site.xml"))

      conf.addResource(hdfsCoreSitePath)
      conf.addResource(hdfsHDFSSitePath)
    }
    FileSystem.get(conf)
  }

  def ugi(userName: String): UserGroupInformation = {
    System.setProperty("HADOOP_USER_NAME", userName)
    UserGroupInformation.createProxyUser(userName, UserGroupInformation.getLoginUser())
  }

}
