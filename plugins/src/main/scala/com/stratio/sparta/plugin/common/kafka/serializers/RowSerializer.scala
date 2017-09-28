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

package com.stratio.sparta.plugin.common.kafka.serializers

import java.util

import com.stratio.sparta.sdk.workflow.enumerators.OutputFormatEnum
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}
import org.apache.spark.sql.Row
import org.apache.spark.sql.json.RowJsonHelper

import scala.collection.JavaConversions._


class RowSerializer extends Serializer[Row] {

  private val stringSerializer = new StringSerializer
  private var outputFormat = OutputFormatEnum.ROW
  private var delimiter = ","

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    val format = {
      if(isKey)
        configs.getOrElse("key.serializer.outputFormat", "ROW")
      else configs.getOrElse("value.serializer.outputFormat", "ROW")
    }.asInstanceOf[String]

    delimiter = {
      if(isKey)
        configs.getOrElse("key.serializer.delimiter", ",")
      else configs.getOrElse("value.serializer.delimiter", ",")
    }.asInstanceOf[String]

    outputFormat = OutputFormatEnum.withName(format)

    stringSerializer.configure(configs, isKey)
  }

  override def serialize(topic: String, data: Row): Array[Byte] = {
    val valueToSerialize = {
      if(outputFormat == OutputFormatEnum.ROW)
        data.mkString(delimiter)
      else RowJsonHelper.toJSON(data)
    }

    stringSerializer.serialize(topic, valueToSerialize)

  }

  override def close() : Unit =
    stringSerializer.close()

}