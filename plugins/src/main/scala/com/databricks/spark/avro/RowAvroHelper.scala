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

package com.databricks.spark.avro

import java.nio.ByteBuffer
import java.sql.Timestamp

import org.apache.avro.generic.GenericData.Record
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import scala.collection.immutable.Map
import scala.collection.JavaConverters.mapAsJavaMapConverter


object RowAvroHelper {

  def getAvroConverter(avroSchema: Schema, expectedSchema: StructType): AnyRef => AnyRef = {
    SchemaConverters.createConverterToSQL(avroSchema, expectedSchema)
  }

  //scalastyle:off
  def createConverterToAvro(
                             dataType: DataType,
                             structName: String,
                             recordNamespace: String): (Any) => Any = {
    dataType match {
      case BinaryType => (item: Any) => item match {
        case null => null
        case bytes: Array[Byte] => ByteBuffer.wrap(bytes)
      }
      case ByteType | ShortType | IntegerType | LongType |
           FloatType | DoubleType | StringType | BooleanType => identity
      case _: DecimalType => (item: Any) => Option(item).map(_.toString).orNull
      case TimestampType => (item: Any) =>
        Option(item) collect { case t: Timestamp => t.getTime } orNull
      case ArrayType(elementType, _) =>
        val elementConverter = createConverterToAvro(elementType, structName, recordNamespace)
        (item: Any) => Option(item) collect { case sourceArray: Seq[Any] =>
          sourceArray.map(elementConverter).toArray
        } orNull
      case MapType(StringType, valueType, _) =>
        val valueConverter = createConverterToAvro(valueType, structName, recordNamespace)
        (item: Any) => {
          Option(item) collect { case item: Map[String @ unchecked, _] =>
            item.mapValues(valueConverter).asJava
          } orNull
        }
      case structType: StructType =>
        val builder = SchemaBuilder.record(structName).namespace(recordNamespace)
        val schema: Schema = SchemaConverters.convertStructToAvro(
          structType, builder, recordNamespace)
        val fieldConverters = structType.fields.map(field =>
          createConverterToAvro(field.dataType, field.name, recordNamespace))
        (item: Any) => {
          Option(item) collect { case row: Row =>
            val record = new Record(schema)
            val convertersIterator = fieldConverters.iterator
            val fieldNamesIterator = dataType.asInstanceOf[StructType].fieldNames.iterator
            val rowIterator = row.toSeq.foreach { cell =>
              val converter = convertersIterator.next()
              record.put(fieldNamesIterator.next(), converter(cell))
            }
            record
          } orNull
        }
    }
  }

  //scalastyle:on

}