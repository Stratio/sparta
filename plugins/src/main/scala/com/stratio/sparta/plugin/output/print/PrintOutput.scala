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
package com.stratio.sparta.plugin.output.print

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk._
import org.apache.spark.Logging
import org.apache.spark.sql._

/**
 * This output prints all AggregateOperations or DataFrames information on screen. Very useful to debug.
 *
 * @param keyName
 * @param properties
 * @param schemas
 */
class PrintOutput(keyName: String,
                  version: Option[Int],
                  properties: Map[String, JSerializable],
                  schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas) with Logging {

  override def upsert(dataFrame: DataFrame, options: Map[String, String]): Unit = {
    if (log.isDebugEnabled) {
      log.debug(s"> Table name       : ${Output.getTableNameFromOptions(options)}")
      log.debug(s"> Time dimension       : ${Output.getTimeFromOptions(options)}")
      log.debug(s"> Version policy   : $version")
      log.debug(s"> Data frame count : " + dataFrame.count())
      log.debug(s"> DataFrame schema")
      dataFrame.printSchema()
    }

    dataFrame.foreach(row => log.info(row.mkString(",")))
  }
}
