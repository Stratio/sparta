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


package com.stratio.sparta.serving.core.helpers

import java.io.{File, Serializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.stratio.sparta.serving.core.utils.ReflectionUtils

object PolicyHelper extends SLF4JLogging {

  lazy val ReflectionUtils = new ReflectionUtils

  def jarsFromPolicy(apConfig: PolicyModel): Seq[File] =
    apConfig.userPluginsJars.filter(!_.jarPath.isEmpty).map(_.jarPath).distinct.map(filePath => new File(filePath))

  def getSparkConfigFromPolicy(policy: PolicyModel): Map[String, String] =
    policy.sparkConf.flatMap { sparkProperty =>
      if (sparkProperty.sparkConfKey.isEmpty || sparkProperty.sparkConfValue.isEmpty)
        None
      else Option((sparkProperty.sparkConfKey, sparkProperty.sparkConfValue))
    }.toMap

  def getSparkConfigs(policy: PolicyModel, methodName: String, suffix: String): Map[String, String] = {
    log.info("Initializing reflection")
    policy.outputs.flatMap(o => {
      val classType = o.configuration.getOrElse(AppConstant.CustomTypeKey, o.`type`).toString
      val clazzToInstance = ReflectionUtils.getClasspathMap.getOrElse(classType + suffix, o.`type` + suffix)
      val clazz = Class.forName(clazzToInstance)
      clazz.getMethods.find(p => p.getName == methodName) match {
        case Some(method) =>
          method.setAccessible(true)
          method.invoke(clazz, o.configuration.asInstanceOf[Map[String, Serializable]])
            .asInstanceOf[Seq[(String, String)]]
        case None =>
          Seq()
      }
    }).toMap
  }

}
