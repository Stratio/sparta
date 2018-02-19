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

import com.google.common.io.BaseEncoding
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.json4s.jackson.Serialization._

trait ArgumentsUtils extends SpartaSerializer {

  def render(config: Config, key: String): String = config.atKey(key).root.render(ConfigRenderOptions.concise)

  def encode(value: String): String = BaseEncoding.base64().encode(value.getBytes)

  def keyConfigEncoded(key: String, config: Config): String = encode(render(config, key))

  def keyOptionConfigEncoded(key: String, opConfig: Option[Config]): String =
    opConfig match {
      case Some(config) => keyConfigEncoded(key, config)
      case None => encode(" ")
    }

  def pluginsEncoded(plugins: Seq[String]): String = encode((Seq(" ") ++ plugins).mkString(","))

  def workflowEncoded(workflow: Workflow): String = encode(write(workflow))

}
