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
import org.apache.hadoop.fs.permission.FsPermission
import org.apache.hadoop.security.UserGroupInformation

import scala.util.Try

class HdfsUtils(userName: String, hadoopConfDir: Option[String]) {

  System.setProperty("HADOOP_USER_NAME", userName)
  private val ugi: UserGroupInformation =
    UserGroupInformation.createProxyUser(userName, UserGroupInformation.getLoginUser())
  private val conf = new Configuration()
  if (hadoopConfDir.isDefined) {
    val confDir = if (hadoopConfDir.get.endsWith("/")) hadoopConfDir.get else hadoopConfDir.get + "/"
    val hdfsCoreSitePath = new FileInputStream(new File(confDir + "core-site.xml"))
    val hdfsHDFSSitePath = new FileInputStream(new File(confDir + "hdfs-site.xml"))

    conf.addResource(hdfsCoreSitePath)
    conf.addResource(hdfsHDFSSitePath)
  }
  private val dfs = FileSystem.get(conf)

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

  def createFolder(folderPath: String): Unit = {
    val path = new Path(folderPath)
    if (!dfs.exists(path)) {
      dfs.mkdirs(path)
    }
  }

  def homeDir(user: String = userName): String = {
    if (dfs.isDirectory(new Path("/user/" + user))) "/user/" + user else "/"
  }

  def setPermission(path: Path, mode: Short): Unit = {
    ugi.doAs(new PrivilegedExceptionAction[Unit]() {
      def run: Unit = {
        dfs.setPermission(path, new FsPermission(mode))
      }
    })
  }

  //scalastyle:off
  def setOwner(path: Path, username: Option[String], group: Option[String]): Unit = {
    ugi.doAs(new PrivilegedExceptionAction[Unit]() {
      def run: Unit = {
        dfs.setOwner(path, username.getOrElse(null), group.getOrElse(null))
      }
    })
  }
  //scalastyle:on

  def open(path: Path): FSDataInputStream = {
    ugi.doAs(new PrivilegedExceptionAction[FSDataInputStream]() {
      def run: FSDataInputStream = {
        dfs.open(path)
      }
    })
  }

  def fileStatus(path: Path): FileStatus = {
    ugi.doAs(new PrivilegedExceptionAction[FileStatus]() {
      def run: FileStatus = {
        dfs.getFileStatus(path)
      }
    })
  }

  def mkdir(path: Path): Boolean = {
    ugi.doAs(new PrivilegedExceptionAction[Boolean]() {
      def run: Boolean = {
        dfs.mkdirs(path)
      }
    })
  }

  def create(path: Path, overwrite: Boolean = false): FSDataOutputStream = {
    ugi.doAs(new PrivilegedExceptionAction[FSDataOutputStream]() {
      def run: FSDataOutputStream = {
        dfs.create(path, overwrite)
      }
    })
  }

  def rename(src: Path, dst: Path): Boolean = {
    ugi.doAs(new PrivilegedExceptionAction[Boolean]() {
      def run: Boolean = {
        dfs.rename(src, dst)
      }
    })
  }

  def write(path: String, destPath: String, overwrite: Boolean = false): Unit = {
    ugi.doAs(new PrivilegedExceptionAction[Unit]() {
      def run: Unit = {
        val file = new File(path)
        val out = dfs.create(new Path(s"$destPath${file.getName}"))
        val in = new BufferedInputStream(new FileInputStream(file))

        Try(IOUtils.copy(in, out))

        IOUtils.closeQuietly(in)
        IOUtils.closeQuietly(out)
      }
    })
  }

  def delete(path: Path, recursive: Boolean = false): Boolean = {
    ugi.doAs(new PrivilegedExceptionAction[Boolean]() {
      def run: Boolean = {
        dfs.delete(path, recursive)
      }
    })
  }

  def deletePath(path: String): Unit = getFiles(path).foreach(file => delete(file.getPath))
}
