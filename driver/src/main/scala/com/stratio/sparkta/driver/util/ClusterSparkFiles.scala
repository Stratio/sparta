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
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel

case class ClusterSparkFiles(policy: AggregationPoliciesModel) extends SLF4JLogging {

  private val jarsPlugins = JarsHelper.findJarsByPath(
    new File(SparktaConfig.sparktaHome, AppConstant.JarPluginsFolder),
    Some(AppConstant.pluginExtension), None, None, None, false)
  private val activeJars = PolicyUtils.activeJars(policy, jarsPlugins)

  def getPluginsFiles: Map[String, String] = {
    validate()
    PolicyUtils.activeJarFiles(activeJars.right.get, jarsPlugins)
      .filter(file => !file.getName.contains("driver")).map(file => file.getName -> file.getAbsolutePath).toMap
  }

  def getClasspathFiles: Map[String, String] = {
    JarsHelper.findJarsByPath(new File(SparktaConfig.sparktaHome, AppConstant.ClasspathJarFolder),
      Some(".jar"), None, Some("driver"), Some(Seq("plugins", "spark", "driver", "web", "serving-api")), false)
      .distinct.map(file => file.getName -> file.getAbsolutePath).toMap
  }

  def getDriverFile: String = {
    val driverJar =
      JarsHelper.findDriverByPath(new File(SparktaConfig.sparktaHome, AppConstant.ClusterExecutionJarFolder)).head
    driverJar.getAbsolutePath
  }

  def validate(): Unit = {
    if (activeJars.isLeft) {
      activeJars.left.get.foreach(log.error)
      require(activeJars.isRight, s"The policy have jars witch cannot be found in classpath:")
    }
    require(activeJars.right.get.nonEmpty, s"The policy don't have jars in the plugins")
  }
}
