/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql.json

import java.io.CharArrayWriter
import java.util.{Comparator, TimeZone}

import com.fasterxml.jackson.core.{JsonFactory, JsonParser}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst._
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.catalyst.json.JacksonUtils.nextUntil
import org.apache.spark.sql.catalyst.json.{CreateJacksonParser, JSONOptions, JacksonGenerator, JacksonParser}
import org.apache.spark.sql.execution.datasources.json.JsonInferSchema.compatibleType
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String


object RowJsonHelper {

  private val structFieldComparator = new Comparator[StructField] {
    override def compare(o1: StructField, o2: StructField): Int = {
      o1.name.compare(o2.name)
    }
  }
  @volatile private var jsonParserOptions: Option[JSONOptions] = None
  private lazy val factory = new JsonFactory()

  def toJSON(row: Row, extraOptions: Map[String, String]): String = {
    val configOptions = jsonOptions(extraOptions)
    val rowSchema = row.schema
    val writer = new CharArrayWriter()
    val gen = new JacksonGenerator(rowSchema, writer, configOptions)
    val internalRow = CatalystTypeConverters.convertToCatalyst(row).asInstanceOf[InternalRow]
    gen.write(internalRow)
    gen.flush()
    val json = writer.toString
    writer.reset()
    gen.close()
    json
  }

  def toRow(json: String, extraOptions: Map[String, String], schema: StructType): Row = {
    val parsedOptions = jsonOptions(extraOptions)
    val createParser = CreateJacksonParser.string _
    val parser = new JacksonParser(schema, parsedOptions)
    val row = parser.parse(json, createParser, UTF8String.fromString).map { internalRow =>
      CatalystTypeConverters.convertToScala(internalRow, schema).asInstanceOf[GenericRowWithSchema]
    }.head

    if (row.values.forall(value => Option(value).isEmpty))
      throw new Exception(s"Error converting json to scala types with schema. json: $json\tschema: $schema")

    row
  }

  def extractSchemaFromJson(json: String, extraOptions: Map[String, String]): StructType = {
    val parser = factory.createParser(json)
    parser.nextToken()
    try {
      val configOptions = jsonOptions(extraOptions)
      inferField(parser, configOptions).asInstanceOf[StructType]
    } finally parser.close()
  }

  //scalastyle:off
  private def inferField(parser: JsonParser, configOptions: JSONOptions): DataType = {
    import com.fasterxml.jackson.core.JsonToken._
    parser.getCurrentToken match {
      case null | VALUE_NULL => NullType

      case FIELD_NAME =>
        parser.nextToken()
        inferField(parser, configOptions)

      case VALUE_STRING if parser.getTextLength < 1 =>
        // Zero length strings and nulls have special handling to deal
        // with JSON generators that do not distinguish between the two.
        // To accurately infer types for empty strings that are really
        // meant to represent nulls we assume that the two are isomorphic
        // but will defer treating null fields as strings until all the
        // record fields' types have been combined.
        NullType

      case VALUE_STRING => StringType
      case START_OBJECT =>
        val builder = Array.newBuilder[StructField]
        while (nextUntil(parser, END_OBJECT)) {
          builder += StructField(
            parser.getCurrentName,
            inferField(parser, configOptions),
            nullable = true)
        }
        val fields: Array[StructField] = builder.result()
        // Note: other code relies on this sorting for correctness, so don't remove it!
        java.util.Arrays.sort(fields, structFieldComparator)
        StructType(fields)

      case START_ARRAY =>
        // If this JSON array is empty, we use NullType as a placeholder.
        // If this array is not empty in other JSON objects, we can resolve
        // the type as we pass through all JSON objects.
        var elementType: DataType = NullType
        while (nextUntil(parser, END_ARRAY)) {
          elementType = compatibleType(
            elementType, inferField(parser, configOptions))
        }

        ArrayType(elementType)

      case (VALUE_NUMBER_INT | VALUE_NUMBER_FLOAT) if configOptions.primitivesAsString => StringType

      case (VALUE_TRUE | VALUE_FALSE) if configOptions.primitivesAsString => StringType

      case VALUE_NUMBER_INT | VALUE_NUMBER_FLOAT =>
        import JsonParser.NumberType._
        parser.getNumberType match {
          // For Integer values, use LongType by default.
          case INT | LONG => LongType
          // Since we do not have a data type backed by BigInteger,
          // when we see a Java BigInteger, we use DecimalType.
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
          case FLOAT | DOUBLE =>
            DoubleType
        }

      case VALUE_TRUE | VALUE_FALSE => BooleanType
    }
  }

  private def jsonOptions(configOptions: Map[String, String]): JSONOptions =
    jsonParserOptions.getOrElse {
      val newOptions = new JSONOptions(configOptions, TimeZone.getDefault.getID)
      jsonParserOptions = Option(newOptions)
      newOptions.setJacksonOptions(factory)
      newOptions
    }
}