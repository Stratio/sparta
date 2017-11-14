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

package org.apache.spark.sql.json

import java.io.CharArrayWriter
import java.util.Comparator

import com.fasterxml.jackson.core.{JsonFactory, JsonParser}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst._
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.catalyst.json.JacksonUtils.nextUntil
import org.apache.spark.sql.catalyst.json.{JSONOptions, JacksonGenerator, JacksonParser}
import org.apache.spark.sql.execution.datasources.json.InferSchema.compatibleType
import org.apache.spark.sql.types._


object RowJsonHelper {

  private val structFieldComparator = new Comparator[StructField] {
    override def compare(o1: StructField, o2: StructField): Int = {
      o1.name.compare(o2.name)
    }
  }
  @volatile private var jsonParserOptions : Option[JSONOptions] = None
  private lazy val factory = new JsonFactory()

  def toJSON(row: Row): String = {
    val rowSchema = row.schema
    val writer = new CharArrayWriter()
    val gen = new JacksonGenerator(rowSchema, writer)
    val internalRow = CatalystTypeConverters.convertToCatalyst(row).asInstanceOf[InternalRow]
    gen.write(internalRow)
    gen.flush()
    val json = writer.toString
    writer.reset()
    gen.close()
    json
  }

  def toRow(json: String, extraOptions: Map[String, String], schema: StructType): Row = {
    val configOptions = jsonOptions(extraOptions)
    val columnNameOfCorruptRecord = configOptions.columnNameOfCorruptRecord.getOrElse("_corrupt_record")
    val parser = new JacksonParser(schema, columnNameOfCorruptRecord, configOptions)
    val row = parser.parse(json).map { internalRow =>
      CatalystTypeConverters.convertToScala(internalRow, schema).asInstanceOf[GenericRowWithSchema]
    }.head

    if(row.values.forall(value => Option(value).isEmpty))
      throw new Exception(s"Error converting json to scala types with schema. json: $json\tschema: $schema")

    row
  }

  def extractSchemaFromJson(json: String, extraOptions: Map[String, String]) : StructType = {
    val parser = factory.createParser(json)
    parser.nextToken()
    try {
      inferField(parser, extraOptions).asInstanceOf[StructType]
    } finally parser.close()
  }

  //scalastyle:off
  private def inferField(parser: JsonParser, extraOptions: Map[String, String]): DataType = {
    import com.fasterxml.jackson.core.JsonToken._
    val configOptions = jsonOptions(extraOptions)
    parser.getCurrentToken match {
      case null | VALUE_NULL => NullType
      case FIELD_NAME =>
        parser.nextToken()
        inferField(parser, extraOptions)
      case VALUE_STRING if parser.getTextLength < 1 => NullType
      case VALUE_STRING => StringType
      case START_OBJECT =>
        val builder = Array.newBuilder[StructField]
        while (nextUntil(parser, END_OBJECT)) {
          builder += StructField(parser.getCurrentName, inferField(parser, extraOptions), nullable = true)
        }
        val fields: Array[StructField] = builder.result()
        java.util.Arrays.sort(fields, structFieldComparator)
        StructType(fields)
      case START_ARRAY =>
        var elementType: DataType = NullType
        while (nextUntil(parser, END_ARRAY)) {
          elementType = compatibleType(elementType, inferField(parser, extraOptions))
        }
        ArrayType(elementType)
      case (VALUE_NUMBER_INT | VALUE_NUMBER_FLOAT) if configOptions.primitivesAsString => StringType
      case (VALUE_TRUE | VALUE_FALSE) if configOptions.primitivesAsString => StringType
      case VALUE_NUMBER_INT | VALUE_NUMBER_FLOAT =>
        import JsonParser.NumberType._
        parser.getNumberType match {
          case INT | LONG => LongType
          case BIG_INTEGER | BIG_DECIMAL =>
            val v = parser.getDecimalValue
            if (Math.max(v.precision(), v.scale()) <= DecimalType.MAX_PRECISION) {
              DecimalType(Math.max(v.precision(), v.scale()), v.scale())
            } else {
              DoubleType
            }
          case FLOAT | DOUBLE if configOptions.prefersDecimal =>
            val v = parser.getDecimalValue
            if (Math.max(v.precision(), v.scale()) <= DecimalType.MAX_PRECISION) {
              DecimalType(Math.max(v.precision(), v.scale()), v.scale())
            } else {
              DoubleType
            }
          case FLOAT | DOUBLE => DoubleType
        }
      case VALUE_TRUE | VALUE_FALSE => BooleanType
    }
  }

  private def jsonOptions(configOptions: Map[String, String]): JSONOptions =
    jsonParserOptions.getOrElse{
      val newOptions = new JSONOptions(configOptions)
      jsonParserOptions = Option(newOptions)
      newOptions.setJacksonOptions(factory)
      newOptions
  }
}