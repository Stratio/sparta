/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparkta.sdk

import scala.util.parsing.json.JSON
import scala.util.{Failure, Success, Try}

class ValidatingPropertyMap[K, V](val m: Map[K, V]) {

  def getMandatory(key: K): V =
    m.get(key) match {
      case Some(value) => value
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getOption(key: K): Option[V] = m.get(key)

  def getString(key: K): String =
    m.get(key) match {
      case Some(value: String) => value
      case Some(value) => value.toString
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getConnectionConfs(key: K, firstJsonItem: String, secondJsonItem: String): String = {
    val conObj = getConnectionChain(key)
    conObj.map(c => c.get(firstJsonItem).get + ":" + c.get(secondJsonItem).get).mkString(",")
  }

  def getZkConnectionConfs(key: K, defaultHost: String, defaultPort: String): (String, String) = {
    val conObj = getConnectionChain(key)
    val value = conObj.map(c => {
      val host = c.get("host") match {
        case Some(value) => value.toString
        case None => defaultHost
      }
      val port = c.get("port") match {
        case Some(value) => value.toString
        case None => defaultPort
      }
      s"$host:$port"
    }).mkString(",")
    (key.toString, value)
  }

  def getHostPortConfs(key: K, defaultHost: String, defaultPort: String): Seq[(String, Int)] = {

    val conObj = getConnectionChain(key)
    conObj.map(c =>
      (c.get("node") match{
        case Some(value) => value.toString
        case None => defaultHost
      },
        c.get("defaultPort") match {
          case Some(value) => value.toString.toInt
          case None => defaultPort.toInt
        }))
  }

  def getTopicPartition(key: K, defaultPartition: Int): Seq[(String, Int)] ={

    val conObj = getConnectionChain(key)
    conObj.map(c =>
      (c.get("topic") match{
        case Some(value) => value.toString
        case None => throw new IllegalStateException(s"$key is mandatory")
      },
        c.get("partition") match {
          case Some(value) => value.toString.toInt
          case None => defaultPartition
        }))
  }

  def getConnectionChain(key: K): Seq[Map[String,String]] = {
    m.get(key) match {
      case Some(value) => JSON.parseFull(value.asInstanceOf[JsoneyString].string)
        .get.asInstanceOf[Seq[Map[String, String]]]
      case None => throw new IllegalStateException(s"$key is mandatory")
    }
  }

  def getString(key: K, default: String): String = {
    m.get(key) match {
      case Some(value: String) => value
      case Some(value) => value.toString
      case None => default
    }
  }

  def getString(key: K, default: Option[String]): Option[String] = {
    m.get(key) match {
      case Some(value: String) => if (value != "") Some(value) else default
      case Some(value) => if (value.toString != "") Some(value.toString) else default
      case None => default
    }
  }

  def getBoolean(key: K): Boolean =
    m.get(key).getOrElse(throw new Exception(s"$key is mandatory")) match {
      case value: String => value.toBoolean
      case value: Int => value.asInstanceOf[Boolean]
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

  def getInt(key: K, default: Int): Int = {
    m.get(key) match {
      case Some(value: Int) => getInt(key)
      case Some(value: JsoneyString) => getInt(key)
      case None => default
    }
  }

  def getLong(key: K): Long =
    m.get(key) match {
      case Some(value: String) =>
        Try(value.toLong) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case Some(value: Long) => value
      case Some(value: Int) => value.toLong
      case Some(value) =>
        Try(value.toString.toLong) match {
          case Success(v) => v
          case Failure(ex) => throw new IllegalStateException(s"Bad value for $key", ex)
        }
      case None =>
        throw new IllegalStateException(s"$key is mandatory")
    }

  def getMap(prefix: String): Option[Map[String, V]] = {
    val subMap = m.filter(entry => entry._1.asInstanceOf[String].startsWith(prefix))
    if (subMap.isEmpty) {
      None
    } else {
      Some(subMap.map(entry => (entry._1.asInstanceOf[String].substring(prefix.length + 1), entry._2)))
    }
  }

  def hasKey(key: K): Boolean = !m.get(key).isEmpty
}

object ValidatingPropertyMap {

  implicit def map2ValidatingPropertyMap[K, V](m: Map[K, V]): ValidatingPropertyMap[K, V] =
    new ValidatingPropertyMap[K, V](m)

}