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

import com.stratio.sparkta.sdk.Parser
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.annotation.tailrec
import scala.util.Try
import scala.util.parsing.json.JSON

class IngestionParser(order: Integer,
                      inputField: String,
                      outputFields: Seq[String],
                      schema: StructType,
                      properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  override def parse(data: Row, removeRaw: Boolean): Row = {
    val input = data.get(schema.fieldIndex(inputField))
    JSON.globalNumberParser = { input: String => (input).toLong }
    val rawData = Try(input.asInstanceOf[String]).getOrElse(new String(input.asInstanceOf[Array[Byte]]))
    val ingestionModel = JSON.parseFull(rawData).get.asInstanceOf[Map[String, JSerializable]]
    val columnList = ingestionModel.get("columns").get.asInstanceOf[List[Map[String, String]]]
    val columnPairs = extractColumnPairs(columnList)
    val allParsedPairs = parseWithSchema(columnPairs, Map())._2
    val filteredParsedPairs = allParsedPairs.filter(element => outputFields.contains(element._1))
    val prevData = if(removeRaw) data.toSeq.drop(1) else data.toSeq

    Row.fromSeq(prevData ++ filteredParsedPairs.values.toSeq)
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
                                       result: List[(String, String)])
  : (List[Map[String, String]], List[(String, String)]) = {
    if (columnList.isEmpty) {
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
                              currentMap: Map[String, JSerializable])
  : (List[(String, JSerializable)], Map[String, JSerializable]) = {
    if (elementList.isEmpty) {
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