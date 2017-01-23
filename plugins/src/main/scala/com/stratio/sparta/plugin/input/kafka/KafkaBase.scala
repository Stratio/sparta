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

package com.stratio.sparta.plugin.input.kafka

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.input.kafka.models.TopicsModel
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats}

import scala.util.Try

trait KafkaBase {

  val DefaultHost = "localhost"
  val DefaultBrokerPort = "9092"
  val DefaultZkPort = "2181"
  val DefaultZookeeperPath = ""

  val properties: Map[String, JSerializable]

  /** HOSTS and PORT extractions **/

  def getHostPort(key: String,
                  defaultHost: String,
                  defaultPort: String): Map[String, String] = {
    if (properties.contains(key)) {
      Map(key -> Try(properties.getHostsPorts(key).hostsPorts
        .map(hostHortModel => s"${hostHortModel.host}:${hostHortModel.port}")
        .mkString(",")).getOrElse(s"$defaultHost:$defaultPort")
      )
    } else Map(key.toString -> s"$defaultHost:$defaultPort")
  }

  def getHostPortZk(key: String,
                    defaultHost: String,
                    defaultPort: String): Map[String, String] = {
    val zookeeperPath = properties.getString("zookeeper.path", DefaultZookeeperPath)

    getHostPort(key, defaultHost, defaultPort).mapValues(hostPort => {
      val fullConnectionPath = if (zookeeperPath.isEmpty) hostPort else s"$hostPort/$zookeeperPath"
      fullConnectionPath.replaceAll("//", "/")
    })
  }

  /** GROUP ID extractions **/

  def getGroupId(key: String): Map[String, String] =
    Map(key -> properties.getString(key, s"sparta-${System.currentTimeMillis}"))

  /** TOPICS extractions **/

  def extractTopics: Set[String] =
    if (properties.contains("topics"))
      getTopicsPartitions.topics.map(topicPartitionModel => topicPartitionModel.topic).toSet
    else throw new IllegalStateException(s"Invalid configuration, topics must be declared in direct approach")

  private def getTopicsPartitions: TopicsModel = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val topicsModel = read[TopicsModel](
      s"""{"topics": ${properties.get("topics").fold("[]") { values => values.toString }}}""""
    )

    if (topicsModel.topics.isEmpty)
      throw new IllegalStateException(s"topics is mandatory")
    else topicsModel
  }
}
