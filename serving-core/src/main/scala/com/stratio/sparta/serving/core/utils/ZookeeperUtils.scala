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

import com.stratio.sparta.serving.core.config.SpartaConfig
import org.apache.zookeeper.common._

import scala.util.{Failure, Properties, Success, Try}

trait ZookeeperUtils {

  def retrievePathFromEnvOrConf: Option[String] = Properties.envOrNone("SPARTA_ZOOKEEPER_PATH") match {
    case Some(path) if path.nonEmpty => Option(path)
    case Some(_) => retrieveFromConf
    case None => retrieveFromConf
  }

  def retrieveFromConf: Option[String] = Try(SpartaConfig.getZookeeperConfig.get.getString("storagePath")) match {
    case Success(confPath) if confPath.nonEmpty => Option(confPath)
    case Success(_) => None
    case Failure(_) => None
  }


  def checkIfValidPath(zkPath: String) : Boolean =
    Try(PathUtils.validatePath(zkPath)) match {
      case Success(_) =>
        if (zkPath.startsWith("/stratio/sparta")) true
        else false
      case Failure(_) => false
    }
}
