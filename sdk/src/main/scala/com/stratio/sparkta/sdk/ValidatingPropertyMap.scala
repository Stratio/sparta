/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.sdk

class ValidatingPropertyMap[K, V](val m: Map[K, V]) {

  def getMandatory(key: K): V =
    m.get(key) match {
      case Some(value) => value
      case None =>
        throw new Exception(s"$key is mandatory")
    }

  def getString(key: K): String =
    m.get(key) match {
      case Some(value) => value.asInstanceOf[String]
      case None =>
        throw new Exception(s"$key is mandatory")
    }

  def getBoolean(key: K): Boolean =
    m.get(key) match {
      case Some(value : String) => value.asInstanceOf[Boolean]
      case Some(value : Int) => value.asInstanceOf[Boolean]
      case None =>
        throw new Exception(s"$key is mandatory")
    }

  def getInt(key: K): Int =
    m.get(key) match {
      case Some(value : String) => value.toInt
      case Some(value : Int) => value
      case None =>
        throw new Exception(s"$key is mandatory")
    }

  def getMap(prefix : String): Option[Map[String, V]] = {
    val subMap = m.filter(entry => entry._1.asInstanceOf[String].startsWith(prefix))
    if(subMap.isEmpty){
      None
    } else {
      Some(subMap.map(entry => (entry._1.asInstanceOf[String].substring(prefix.length+1), entry._2)))
    }
  }

  def hasKey(key : K) : Boolean = !m.get(key).isEmpty

}

object ValidatingPropertyMap {

  implicit def map2ValidatingPropertyMap[K, V](m: Map[K, V]): ValidatingPropertyMap[K, V] =
    new ValidatingPropertyMap[K, V](m)

}
