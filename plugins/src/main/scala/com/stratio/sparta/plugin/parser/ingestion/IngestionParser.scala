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
import org.apache.spark.sql.types.StructType

import com.stratio.decision.commons.avro.InsertMessage
import com.stratio.decision.commons.constants.ColumnType
import com.stratio.decision.commons.messages.ColumnNameTypeValue
import com.stratio.sparta.plugin.parser.ingestion.serializer.JavaToAvroSerializer
import com.stratio.sparta.sdk.Parser

class IngestionParser(order: Integer,
                      inputField: String,
                      outputFields: Seq[String],
                      schema: StructType,
                      properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) with SLF4JLogging {

  val fieldNames = schema.map(field => field.name)

  override def parse(row: Row, removeRaw: Boolean): Row = {
    val input = row.get(schema.fieldIndex(inputField))

    val parsedValues = IngestionParser.parseRawData(input, fieldNames)

    val previousParserValues = if(removeRaw) row.toSeq.drop(1) else row.toSeq
    Row.fromSeq(previousParserValues ++ parsedValues)
  }

}

object IngestionParser extends SLF4JLogging {

  val datumReader =  new SpecificDatumReader[InsertMessage](InsertMessage.getClassSchema())
  val javaToAvro = new JavaToAvroSerializer(datumReader)

  def parseRawData(rawData: Any, fieldNames: Seq[String]): Seq[Any] = {
    Try {
      val stratioStreamingMessage = javaToAvro.deserialize(rawData.asInstanceOf[Array[Byte]])

      stratioStreamingMessage.getColumns.toList
        // Filter to just parse those columns added to the schema
        .filter(column => fieldNames.contains(column.getColumn))
        .map{case column: ColumnNameTypeValue => getValue(column)}
    } match {
        case Success(parsedValues) => parsedValues
        case Failure(e) => {
          log.warn(s"Error parsing event: ${rawData}", e)
          Seq()
        }
      }
  }


  private def getValue(column: ColumnNameTypeValue): Any = {
    val columnType = Try(ColumnType.valueOf(column.getType.toString)).getOrElse(ColumnType.STRING)

    columnType match {
      case ColumnType.STRING => column.getValue.toString
      case ColumnType.INTEGER => column.getValue.toString.toInt
      case ColumnType.LONG => column.getValue.toString.toLong
      case ColumnType.DOUBLE => column.getValue.toString.toDouble
      case ColumnType.FLOAT => column.getValue.toString.toFloat
      case ColumnType.BOOLEAN => column.getValue.toString.toBoolean
    }
  }
}