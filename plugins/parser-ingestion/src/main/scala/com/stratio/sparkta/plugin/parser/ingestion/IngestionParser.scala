/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.plugin.parser.ingestion

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.{Event, Parser}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.annotation.tailrec
import scala.util.parsing.json.JSON

class IngestionParser(name: String,
                      order: Integer,
                      inputField: String,
                      outputFields: Seq[String],
                      properties: Map[String, JSerializable])
  extends Parser(name, order, inputField, outputFields, properties) {

  private val DatetimePattern = "yyyy-MM-dd HH:mm:ss"

  override def parse(data: Event): Event = {
    val ingestionModel = JSON.parseFull(data.keyMap.get(inputField).get.asInstanceOf[String])
      .get.asInstanceOf[Map[String,JSerializable]]
    val columnList = ingestionModel.get("columns").get.asInstanceOf[List[Map[String, String]]]
    val columnPairs = extractColumnPairs(columnList)
    val allParsedPairs = parseWithSchema(columnPairs, Map())._2
    val filteredParsedPairs = allParsedPairs.filter(element => outputFields.contains(element._1))
    new Event(filteredParsedPairs)
  }

  // XXX Private methods.

  private def extractColumnPairs(columnList: List[Map[String, String]]): List[(String, String)] = {
    val columnListKeyValue = for {
      columnElement <- columnList
      value <- columnElement
    } yield Map(value._1 -> value._2)
    extractColumnPairElement(columnListKeyValue, List())._2
  }

  @tailrec
  private def extractColumnPairElement(columnList: List[Map[String, String]],
                                       result: List[(String,String)])
  : (List[Map[String,String]], List[(String,String)]) = {
    if(columnList.isEmpty) {
      (columnList, result)
    } else {
      val currentValue = columnList.last.head._2
      val columnListWithoutValue = columnList.init
      val currentKey = columnListWithoutValue.last.head._2
      val columnListWithoutKeyAndValue = columnListWithoutValue.init
      extractColumnPairElement(columnListWithoutKeyAndValue, result.:::(List((currentKey, currentValue))))
    }
  }

  @tailrec
  private def parseWithSchema(elementList: List[(String, String)],
                              currentMap: Map[String,JSerializable])
  : (List[(String, JSerializable)],Map[String,JSerializable]) = {
    if(elementList.isEmpty) {
      (elementList, currentMap)
    } else {
      val currentElement = elementList.last
      val newElementList = elementList.init
      val newCurrentMap = currentMap ++ parseElementWithSchema(currentElement)
      parseWithSchema(newElementList, newCurrentMap)
    }
  }

  private def parseElementWithSchema(element: (String, JSerializable)): Map[String, JSerializable] = {
    val key = element._1
    val value = element._2.toString

    val dataType: Option[String] = (properties.get(key) match {
      case Some(value) => Some(value.toString.toLowerCase)
      case _ => None
    })

    dataType match {
      case Some("long") =>
        Map(key -> value.toLong)
      case Some("int") | Some("integer") =>
        Map(key -> value.toInt)
      case Some("string") =>
        Map(key -> value)
      case Some("float") =>
        Map(key -> value.toFloat)
      case Some("double") =>
        Map(key -> value.toDouble)
      case None =>
        Map()
      case _ => throw new NoSuchElementException(s"The dataType $dataType does not exists in the schema.")
    }
  }
}