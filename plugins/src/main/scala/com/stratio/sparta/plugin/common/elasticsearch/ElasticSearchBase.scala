/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.elasticsearch

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

import scala.util.Try

trait ElasticSearchBase extends SLF4JLogging {

  val properties: Map[String, JSerializable]

  val DefaultIndexType = "sparta"
  val DefaultNode = "localhost"
  val DefaultHttpPort = "9200"
  val NodeName = "node"
  val NodesName = "nodes"
  val HttpPortName = "httpPort"
  val ElasticSearchClass = "org.elasticsearch.spark.sql"

  def getHostPortConf(
                       key: String,
                       defaultHost: String,
                       defaultPort: String,
                       nodeName: String,
                       portName: String
                     ): Seq[(String, Int)] = {
    val values = Try(properties.getMapFromArrayOfValues(key)).getOrElse(Seq.empty[Map[String, String]])

    values.map { c =>
      (c.getOrElse(nodeName, defaultHost), c.getOrElse(portName, defaultPort).toInt)
    }
  }

}
