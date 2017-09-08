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

import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructField
import org.kitesdk.morphline.api.{Command, Record}

class EventCollector(outputFieldsSchema: Array[StructField]) extends Command {

  @volatile var row: Row = Row.empty

  override def notify(p1: Record): Unit = {}

  def reset(): Unit = row = Row.empty

  //scalastyle:off
  override def getParent: Command = null

  //scalastyle:om

  override def process(recordProcess: Record): Boolean = {
      Option(recordProcess) match {
        case Some(record) =>
          row = Row.fromSeq(outputFieldsSchema.map(field =>
            Option(record.getFirstValue(field.name)) match {
              case Some(value) =>
                TypeOp.castingToSchemaType(field.dataType, value.asInstanceOf[Any])
              case None =>
                throw new IllegalStateException(s"Impossible to parse field: ${field.name}.")
            }
          ).toList)
          true
        case None =>
          row = Row.empty
          false
      }
  }
}
