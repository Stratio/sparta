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

package com.stratio.sparta.plugin.parser.ingestion

import java.io.{Serializable => JSerializable}

import scala.collection.JavaConversions._
import scala.util._
import akka.event.slf4j.SLF4JLogging
import org.apache.avro.specific.SpecificDatumReader
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}
import com.stratio.decision.commons.avro.InsertMessage
import com.stratio.decision.commons.constants.ColumnType
import com.stratio.decision.commons.messages.ColumnNameTypeValue
import com.stratio.sparta.plugin.parser.ingestion.serializer.JavaToAvroSerializer
import com.stratio.sparta.sdk.{Parser, TypeOp}

class IngestionParser(order: Integer,
                      inputField: String,
                      outputFields: Seq[String],
                      schema: StructType,
                      properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) with SLF4JLogging {

  val fieldNames = outputFieldsSchema.map(field => field.name)

  override def parse(row: Row, removeRaw: Boolean): Row = {
    val input = row.get(schema.fieldIndex(inputField))

    val parsedValues = IngestionParser.parseRawData(input, fieldNames, outputFieldsSchema)

    val previousParserValues = if (removeRaw) row.toSeq.drop(1) else row.toSeq
    Row.fromSeq(previousParserValues ++ parsedValues)
  }
}

object IngestionParser extends SLF4JLogging {

  val datumReader = new SpecificDatumReader[InsertMessage](InsertMessage.getClassSchema)
  val javaToAvro = new JavaToAvroSerializer(datumReader)

  def parseRawData(rawData: Any, fieldNames: Seq[String], schemas: Array[StructField]): Seq[Any] = {
    val stratioStreamingMessage = javaToAvro.deserialize(rawData.asInstanceOf[Array[Byte]])
    val columnsStratioStreamingMessage = stratioStreamingMessage.getColumns.toList
    val columnsNamesStratioStreamingMessage = columnsStratioStreamingMessage.map(_.getColumn)

    fieldNames.foreach { fieldName =>
      if (!columnsNamesStratioStreamingMessage.contains(fieldName)) {
        val error = s"Error parsing data because the output field $fieldName is not included in the input data"
        log.warn(error)
        throw new IllegalStateException(error)
      }
    }

    schemas.map(schema => {
      val columnFound = columnsStratioStreamingMessage.find(column => column.getColumn == schema.name).getOrElse {
        val error = s"Error parsing data with field ${schema.name}"
        log.warn(error)
        throw new IllegalStateException(error)
      }
      TypeOp.transformValueByTypeOp(schema.dataType, columnFound.getValue.asInstanceOf[Any])
    })
  }
}