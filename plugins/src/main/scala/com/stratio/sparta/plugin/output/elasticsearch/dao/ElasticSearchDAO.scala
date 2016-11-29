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
package com.stratio.sparta.plugin.output.elasticsearch.dao

import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.{TypeOp, _}

trait ElasticSearchDAO {

  final val DefaultIndexType = "sparta"
  final val DefaultNode = "localhost"
  final val DefaultTcpPort = "9300"
  final val DefaultHttpPort = "9200"
  final val NodeName = "node"
  final val NodesName = "nodes"
  final val TcpPortName = "tcpPort"
  final val HttpPortName = "httpPort"
  final val DefaultCluster = "elasticsearch"
  // this regex pretend validate all localhost loopback values including ipv6
  final val LocalhostPattern = "^localhost$|^127(?:\\.[0-9]+){0,2}\\.[0-9]+$|^(?:0*\\:)*?:?0*1$".r.pattern

  val dateTypeMap = Map("timestamp" -> TypeOp.Timestamp, "date" -> TypeOp.Date, "datetime" -> TypeOp.DateTime,
    "long" -> TypeOp.Long, "string" -> TypeOp.String)

  def tcpNodes: Seq[(String, Int)]

  def httpNodes: Seq[(String, Int)]

  def clusterName: String

  def idField: Option[String] = None

  def defaultIndexMapping: Option[String] = None

  def mappingType: String

  def getSparkConfig(timeName: Option[String]): Map[String, String] =
    idField.fold(Map.empty[String, String]) {field => Map("es.mapping.id" -> field)} ++
    Map("es.nodes" -> httpNodes.head._1, "es.port" -> httpNodes(0)._2.toString, "es.index.auto.create" -> "no") ++ {
    timeName match {
      case Some(timeNameValue) if !timeNameValue.isEmpty =>  Map("es.mapping.timestamp" -> timeNameValue)
      case _ => Map()
    }
  }

  def getDateTimeType(dateType: Option[String]): TypeOp = {
    dateType match {
      case None => TypeOp.Long
      case Some(date) => dateTypeMap.getOrElse(date.toLowerCase, TypeOp.String)
    }
  }
}
