/**
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
package com.stratio.sparta.driver.util

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

import com.stratio.sparta.serving.core.models.AggregationPoliciesModel

import scala.util.Try

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.Config
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path

case class HdfsUtils(dfs: FileSystem, userName: String) {

  def getPolicyCheckpointPath(policy: AggregationPoliciesModel): String =
    s"${dfs.getHomeDirectory}/${AggregationPoliciesModel.checkpointPath(policy)}"

  def getFiles(path: String): Array[FileStatus] = dfs.listStatus(new Path(path))

  def getFile(filename: String): InputStream = dfs.open(new Path(filename))

  def delete(path: String): Unit = dfs.delete(new Path(path), true)

  def write(path: String, destPath: String, overwrite: Boolean = false): Int = {
    val file = new File(path)
    val out = dfs.create(new Path(s"$destPath${file.getName}"))
    val in = new BufferedInputStream(new FileInputStream(file))
    val bytesCopied = Try(IOUtils.copy(in, out))
    IOUtils.closeQuietly(in)
    IOUtils.closeQuietly(out)
    bytesCopied.get
  }
}

object HdfsUtils extends SLF4JLogging {

  private final val DefaultFSProperty = "fs.defaultFS"

  private final val HdfsDefaultPort = 8020

  def hdfsConfiguration(configOpt: Option[Config]): Configuration =
    configOpt.map { config =>
      val master = config.getString("hdfsMaster")
      val port = Try(config.getInt("hdfsPort")).getOrElse(HdfsDefaultPort)
      val conf = new Configuration()
      conf.set(DefaultFSProperty, s"hdfs://$master:$port/user/stratio")
      conf
    }.getOrElse(
      throw new Exception("Not found hdfs config")
    )

  def apply(user: String, conf: Configuration): HdfsUtils = {
    log.debug(s"Configuring HDFS with master: ${conf.get(DefaultFSProperty)} and user: $user")
    val defaultUri = FileSystem.getDefaultUri(conf)
    new HdfsUtils(FileSystem.get(defaultUri, conf, user), user)
  }

  def apply(config: Option[Config]): HdfsUtils = {
    val user = config.map(_.getString("hadoopUserName")).getOrElse("stratio")
    apply(user, hdfsConfiguration(config))
  }
}
