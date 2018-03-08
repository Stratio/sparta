/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
