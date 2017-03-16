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

import java.lang.reflect.Constructor

import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.config.SpartaConfig._

import scala.util.{Failure, Success, Try}


object SecurityManagerHelper {

  lazy val securityManager =
    if (!isSecurityManagerEnabled){
      log.warn("Authorization is not enabled, configure a security manager if needed")
      None
    } else {
        SpartaConfig.mainConfig.map { case (config) =>
            Try(config.getString("manager.class")) match {
              case Success(value) =>
                val securityManagerClass = Class.forName(value)
                val constr: Constructor[_] = securityManagerClass.getConstructor()
                val secManager = constr.newInstance().asInstanceOf[SpartaSecurityManager]
                secManager.start()
                secManager
              case Failure(e) =>
                val msg = "Is mandatory specify one manager class when the security is enable"
                log.error(msg)
                throw new IllegalStateException(msg)
            }
        }
    }

    def isSecurityManagerEnabled : Boolean = SpartaConfig.mainConfig match {
      case Some(config) =>
        Try(config.getBoolean("manager.enabled")) match {
          case Success(value) =>
            value
          case Failure(e) =>
            log.error("Incorrect value in security manager option, setting enable by default", e)
            true
        }
      case None =>
        log.warn("Sparta main config is not initialized, setting security manager enabled by default")
        true
    }

}
