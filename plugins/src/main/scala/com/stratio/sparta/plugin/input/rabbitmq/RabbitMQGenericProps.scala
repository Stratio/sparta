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
package com.stratio.sparta.plugin.input.rabbitmq

import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.streaming.rabbitmq.ConfigParameters

import scala.util.Try

object RabbitMQGenericProps {
  val RabbitmqProperties = "rabbitmqProperties"
  val RabbitmqPropertyKey = "rabbitmqPropertyKey"
  val RabbitmqPropertyValue = "rabbitmqPropertyValue"
}

trait RabbitMQGenericProps {
  this: Input =>

  import RabbitMQGenericProps._

  def propsWithStorageLevel(sparkStorageLevel: String): Map[String, String] = {
    val rabbitMQProperties = getRabbitMQProperties
    Map(ConfigParameters.StorageLevelKey -> sparkStorageLevel) ++
      rabbitMQProperties.mapValues(value => value.toString) ++
      properties.mapValues(value => value.toString)
  }

  def getRabbitMQProperties: Map[String, String] =
    Try(properties.getMapFromJsoneyString(RabbitmqProperties))
      .getOrElse(Seq.empty[Map[String, String]])
      .map(c =>
        (c.get(RabbitmqPropertyKey) match {
          case Some(value) => value.toString
          case None => throw new IllegalArgumentException(s"The field $RabbitmqPropertyKey is mandatory")
        },
          c.get(RabbitmqPropertyValue) match {
            case Some(value) => value.toString
            case None => throw new IllegalArgumentException(s"The field $RabbitmqPropertyValue is mandatory")
          })).toMap
}
