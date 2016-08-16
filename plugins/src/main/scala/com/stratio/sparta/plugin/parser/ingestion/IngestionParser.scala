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
    Try {
      val stratioStreamingMessage = javaToAvro.deserialize(rawData.asInstanceOf[Array[Byte]])

      stratioStreamingMessage.getColumns.toList
        // Filter to just parse those columns added to the schema
        .filter(column => fieldNames.contains(column.getColumn))
        .map { case column: ColumnNameTypeValue =>
          val schemaFound = schemas.find(schema => schema.name == column.getColumn).getOrElse {
            val error = s"Error parsing data with column ${column.getColumn}"
            log.warn(error)
            throw new RuntimeException(error)
          }
          TypeOp.transformValueByTypeOp(schemaFound.dataType, column.getValue)
        }
    } match {
      case Success(parsedValues) =>
        parsedValues
      case Failure(e) =>
        log.warn(s"Error parsing event: $rawData", e)
        Seq()
    }
  }
}