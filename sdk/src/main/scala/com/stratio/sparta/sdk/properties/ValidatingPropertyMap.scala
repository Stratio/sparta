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

package com.stratio.sparta.sdk.properties

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.models.{HostsPortsModel, PropertiesQueriesModel}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

class ValidatingPropertyMap[K, V](val m: Map[K, V]) extends SLF4JLogging {

  def getString(key: K): String =
    m.get(key) match {
      case Some(value: String) => value
      case Some(value) => value.toString
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getHostsPorts(key: K): HostsPortsModel = {
    implicit val json4sJacksonFormats: Formats =
      DefaultFormats +
        new JsoneyStringSerializer()

    read[HostsPortsModel](
      s"""{"hostsPorts": ${m.get(key).fold("[]") { values => values.toString }}}""""
    )
  }

  def getPropertiesQueries(key: K): PropertiesQueriesModel = {
    implicit val json4sJacksonFormats: Formats =
      DefaultFormats +
        new JsoneyStringSerializer()

    read[PropertiesQueriesModel](
      s"""{"queries": ${m.get(key).fold("[]") { values => values.toString }}}""""
    )
  }

  def getMapFromJsoneyString(key: K): Seq[Map[String, String]] = {
    m.get(key) match {
      case Some(value) =>

        val parsed = parse(s"""{"children": ${value.toString} }"""")

        val result: List[Map[String, String]] =
          for {
            JArray(element) <- parsed \ "children"
            JObject(list) <- element
          } yield {
            (for {
              JField(key, JString(value)) <- list
            } yield (key, value)).toMap
          }
        if (result.isEmpty)
          throw new IllegalStateException(s"$key is mandatory")
        else result
      case None => throw new IllegalStateException(s"$key is mandatory")
    }
  }

  def getString(key: K, default: String): String = {
    m.get(key) match {
      case Some(value: String) => if (value.isEmpty) default else value
      case Some(null) => default
      case Some(value) => if (value.toString.isEmpty) default else value.toString
      case None => default
    }
  }

  def getString(key: K, default: Option[String]): Option[String] = {
    m.get(key) match {
      case Some(value: String) => if (value.isEmpty) default else Some(value)
      case Some(null) => default
      case Some(value) => if (value.toString.isEmpty) default else Some(value.toString)
      case None => default
    }
  }

  def getBoolean(key: K): Boolean =
    m.getOrElse(key, throw new Exception(s"$key is mandatory")) match {
      case value: String => value.toBoolean
      case value: Int =>
        value.asInstanceOf[Int] match {
          case 1 => true
          case 0 => false
          case _ => throw new IllegalStateException(s"$value is not parsable as boolean")
        }
      case value: Boolean => value
    }

  def getInt(key: K): Int =
    m.get(key) match {
      case Some(value: String) =>
        Try(value.toInt) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case Some(value: Int) => value
      case Some(value: Long) => value.toInt
      case Some(value) =>
        Try(value.toString.toInt) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getOptionsList(key: K,
                     propertyKey: String,
                     propertyValue: String): Map[String, String] =
    Try(getMapFromJsoneyString(key)).getOrElse(Seq.empty[Map[String, String]])
      .map(c =>
        (c.get(propertyKey) match {
          case Some(value) => value.toString
          case None => throw new IllegalStateException(s"The field $propertyKey is mandatory")
        },
          c.get(propertyValue) match {
            case Some(value) => value.toString
            case None => throw new IllegalStateException(s"The field $propertyValue is mandatory")
          })).toMap

  def hasKey(key: K): Boolean = m.get(key).isDefined
}

object ValidatingPropertyMap {

  implicit def map2ValidatingPropertyMap[K, V](m: Map[K, V]): ValidatingPropertyMap[K, V] =
    new ValidatingPropertyMap[K, V](m)
}