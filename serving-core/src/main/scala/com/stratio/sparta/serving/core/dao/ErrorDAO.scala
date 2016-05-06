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
package com.stratio.sparta.serving.core.dao

import com.typesafe.config.Config

import com.stratio.common.utils.components.config.impl.TypesafeConfigComponent
import com.stratio.common.utils.components.dao.GenericDAOComponent
import com.stratio.common.utils.components.logger.impl.Slf4jLoggerComponent
import com.stratio.sparta.serving.core.constants.AppConstant

class ErrorDAO(conf: Config) extends GenericDAOComponent[String] with TypesafeConfigComponent with
  Slf4jLoggerComponent {

  override val config = new TypesafeConfig(Option(conf))
  override val dao: DAO = new GenericDAO(Option(AppConstant.ErrorsZkPath))
}

object ErrorDAO {

  private var instance: Option[ErrorDAO] = None

  def getInstance: ErrorDAO = {
    require(instance.isDefined, "The instance was not created. You need to specify ZookeeperConfig")
    instance.get
  }

  def apply(): ErrorDAO = getInstance

  def apply(conf: Config): ErrorDAO = {
    instance = Some(new ErrorDAO(conf))
    instance.get
  }
}
