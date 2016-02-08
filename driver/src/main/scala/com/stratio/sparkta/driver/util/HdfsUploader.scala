/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

import java.io.File

import akka.event.slf4j.SLF4JLogging

import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.helpers.JarsHelper
import com.stratio.sparkta.serving.core.models.CommonPoliciesModel

case class HdfsUploader(policy: CommonPoliciesModel, hdfs: HdfsUtils) extends SLF4JLogging {

  private val hdfsConfig = SparktaConfig.getHdfsConfig.get
  private val jarsPlugins = JarsHelper.findJarsByPath(
    new File(SparktaConfig.sparktaHome, AppConstant.JarPluginsFolder),
    Some(AppConstant.pluginExtension), None, None, None, false)
  private val activeJars = PolicyUtils.activeJars(policy, jarsPlugins)

  def uploadPlugins(pluginsJarsPath: String): Unit = {
    validate
    val pluginsJarsFiles = PolicyUtils.activeJarFiles(activeJars.right.get, jarsPlugins)
    pluginsJarsFiles.foreach(file => hdfs.write(file.getAbsolutePath, pluginsJarsPath, true))
    log.info("Jars plugins uploaded to HDFS")
  }

  def uploadClasspath(classpathJarsPath: String): Unit = {
    JarsHelper.findJarsByPath(new File(SparktaConfig.sparktaHome, AppConstant.ClasspathJarFolder),
      Some(".jar"), None, Some("driver"), Some(Seq("plugins", "spark", "driver", "web", "serving-api")), false)
      .foreach(file => hdfs.write(file.getAbsolutePath, classpathJarsPath, true))
    log.info("Classpath uploaded to HDFS")
  }

  def uploadDriver(driverJarPath: String): String = {
    val driveFolder = new File(SparktaConfig.sparktaHome, AppConstant.ClusterExecutionJarFolder)
    val driverJar = JarsHelper.findDriverByPath(driveFolder).head
    hdfs.write(driverJar.getAbsolutePath, driverJarPath, true)
    log.info("Jar driver uploaded to HDFS")
    s"hdfs://${hdfsConfig.getString(AppConstant.HdfsMaster)}$driverJarPath${driverJar.getName}"
  }

  def validate: Unit = {
    if (activeJars.isLeft) {
      activeJars.left.get.foreach(log.error)
      require(activeJars.isRight, s"The policy have jars witch cannot be found in classpath:")
    }
    require(activeJars.right.get.nonEmpty, s"The policy don't have jars in the plugins")
  }
}
