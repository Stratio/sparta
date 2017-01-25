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
package com.stratio.sparta

import java.io.{Serializable => JSerializable}
import java.util.Date

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.pipeline.output.{Output, OutputFormatEnum, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import org.apache.spark.Logging
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.DataFrame


class FileOutput(keyName: String,
                 version: Option[Int],
                 properties: Map[String, JSerializable],
                 schemas: Seq[SpartaSchema])
                  extends Output(keyName, version, properties, schemas) with Logging {

  val path = properties.get("path").getOrElse(throw new IllegalArgumentException("Property path is mandatory"))
  val createDifferentFiles = properties.get("createDifferentFiles").getOrElse("true")

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val finalPath = if (createDifferentFiles.asInstanceOf[String].toBoolean){
      path.toString + new Date().getTime
    } else {
      path.toString
    }
    dataFrame.write.json(finalPath)
  }
}
