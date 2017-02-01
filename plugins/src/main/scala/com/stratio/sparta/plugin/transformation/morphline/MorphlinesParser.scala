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

package com.stratio.sparta.plugin.transformation.morphline

import java.io.{ByteArrayInputStream, Serializable => JSerializable}
import java.util.concurrent.ConcurrentHashMap

import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}
import org.kitesdk.morphline.api.Record

import scala.collection.JavaConverters._
import scala.util.Try

class MorphlinesParser(order: Integer,
                       inputField: Option[String],
                       outputFields: Seq[String],
                       schema: StructType,
                       properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  assert(inputField.isDefined, "It's necessary to define one inputField in the Morphline Transformation")

  private val config: String = properties.getString("morphline")

  override def parse(row: Row): Seq[Row] = {
    val inputValue = Option(row.get(inputFieldIndex))
    val newData = Try {
      inputValue match {
        case Some(s: String) =>
          if (s.isEmpty) returnWhenError(new IllegalStateException(s"Impossible to parse because value is empty"))
          else parseWithMorphline(new ByteArrayInputStream(s.getBytes("UTF-8")))
        case Some(b: Array[Byte]) =>
          if (b.length == 0)  returnWhenError(new IllegalStateException(s"Impossible to parse because value is empty"))
          else parseWithMorphline(new ByteArrayInputStream(b))
        case _ =>
          returnWhenError(new IllegalStateException(s"Impossible to parse because value is empty"))
      }
    }
    returnData(newData, removeInputFieldMorphline(row))
  }

   private def removeIndex(row: Row, inputFieldIndex: Int): Row =
    if (row.size < inputFieldIndex) row
    else Row.fromSeq(row.toSeq.take(inputFieldIndex) ++ row.toSeq.drop(inputFieldIndex + 1))


   private def removeInputFieldMorphline(row: Row): Row =
    if (inputFieldRemoved && inputField.isDefined) removeIndex(row, inputFieldIndex)
    else row

  private def parseWithMorphline(value: ByteArrayInputStream): Row = {
    val record = new Record()
    record.put(inputField.get, value)
    MorphlinesParser(order, config, outputFieldsSchema).process(record)
  }
}

object MorphlinesParser {

  private val instances = new ConcurrentHashMap[String, KiteMorphlineImpl].asScala

  def apply(order: Integer, config: String, outputFieldsSchema: Array[StructField]): KiteMorphlineImpl = {
    instances.get(config) match {
      case Some(kiteMorphlineImpl) =>
        kiteMorphlineImpl
      case None =>
        val kiteMorphlineImpl = KiteMorphlineImpl(ConfigFactory.parseString(config), outputFieldsSchema)
        instances.putIfAbsent(config, kiteMorphlineImpl)
        kiteMorphlineImpl
    }
  }
}
