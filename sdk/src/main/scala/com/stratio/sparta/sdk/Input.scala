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
package com.stratio.sparta.sdk

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream


abstract class Input(properties: Map[String, JSerializable]) extends Parameterizable(properties) {

  def setUp(ssc: StreamingContext, storageLevel: String): DStream[Row]

  def storageLevel(sparkStorageLevel: String): StorageLevel = {
    StorageLevel.fromString(sparkStorageLevel)
  }
}

object Input {

  final val ClassSuffix = "Input"
  final val RawDataKey = "_attachment_body"
  final val InitSchema = Map(Input.RawDataKey -> StructType(Seq(StructField(Input.RawDataKey, StringType))))
}

