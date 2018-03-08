/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.helper

import akka.event.slf4j.SLF4JLogging
import com.databricks.spark.avro.SchemaConverters
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy._
import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import com.stratio.sparta.plugin.enumerations.SchemaInputMode._
import org.apache.avro.Schema
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.catalyst.parser.LegacyTypeStringParser
import org.apache.spark.sql.json.RowJsonHelper.extractSchemaFromJson
import org.apache.spark.sql.types.{DataType, StructField, StructType}

import scala.util.{Failure, Try}

object SchemaHelper extends SLF4JLogging {

  def getJsonSparkSchema(
                          useRowSchema: Boolean,
                          schemaInputMode: SchemaInputMode.Value,
                          schemaProvided: Option[String],
                          jsonOptions: Map[String, String] = Map.empty[String, String]
                        ): Option[StructType] =
    schemaProvided flatMap { schemaStr =>
      if (useRowSchema) None
      else {
        {
          schemaInputMode match {
            case EXAMPLE => Try(extractSchemaFromJson(schemaStr, jsonOptions))
            case SPARKFORMAT => getSparkSchemaFromString(schemaStr)
            case _ => throw new Exception("Invalid input mode in json schema extractor")
          }
        } recoverWith {
          case e =>
            log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event." +
              s" ${e.getLocalizedMessage}")
            Failure(e)
        } toOption
      }
    }

  def getAvroSparkSchema(useRowSchema: Boolean, schemaProvided: Option[String]): Option[StructType] =
    schemaProvided flatMap { schemaStr =>
      if (useRowSchema) None
      else Try(getSparkSchemaFromAvroSchema(getAvroSchemaFromString(schemaStr))).recoverWith { case e =>
        log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event" +
          s" ${e.getLocalizedMessage}")
        Failure(e)
      }.toOption
    }

  def getAvroSchema(useRowSchema: Boolean, schemaProvided: Option[String]): Option[Schema] =
    schemaProvided flatMap { schemaStr =>
      if (useRowSchema) None
      else Try(getAvroSchemaFromString(schemaStr)).recoverWith { case e =>
        log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event" +
          s" ${e.getLocalizedMessage}")
        Failure(e)
      }.toOption
    }

  def getAvroSchemaFromString(schemaStr: String): Schema = {
    val parser = new Schema.Parser()

    parser.parse(schemaStr)
  }

  def getSparkSchemaFromAvroSchema(avroSchema: Schema): StructType =
    SchemaConverters.toSqlType(avroSchema).dataType match {
      case t: StructType => t
      case _ => throw new Exception(
        s"Avro schema cannot be converted to a Spark SQL StructType: ${avroSchema.toString(true)}")
    }

  def getSparkSchemaFromString(schemaStr: String): Try[StructType] =
    Try { // Try to deserialize the schema assuming it is in JSON format
      DataType.fromJson(schemaStr)
    } orElse Try { // If it wasn't a JSON, try assuming it is an string serialization of `StructType`
      LegacyTypeStringParser.parse(schemaStr)
    } flatMap { schema =>
      Try(schema.asInstanceOf[StructType])
    }

  def getNewOutputSchema(inputSchema: StructType, preservationPolicy: FieldsPreservationPolicy.Value,
                         providedSchema: Seq[StructField], inputField: String): StructType = {
    preservationPolicy match {
      case APPEND =>
        StructType(inputSchema.fields ++ providedSchema)
      case REPLACE =>
        val inputFieldIdx = inputSchema.indexWhere(_.name == inputField)
        assert(inputFieldIdx > -1, s"$inputField should be a field in the input row")
        val (leftInputFields, rightInputFields) = inputSchema.fields.splitAt(inputFieldIdx)
        val outputFields = leftInputFields ++ providedSchema ++ rightInputFields.tail

        StructType(outputFields)
      case _ =>
        StructType(providedSchema)
    }
  }

  def updateRow(source: Row, extracted: Row, inputFieldIdx: Int ,
                preservationPolicy: FieldsPreservationPolicy.Value): Row =
    preservationPolicy match {
      case APPEND =>
        val values = (source.toSeq ++ extracted.toSeq).toArray
        val schema = StructType(source.schema ++ extracted.schema)
        new GenericRowWithSchema(values, schema)

      case REPLACE =>
        val (leftInputFields, rightInputFields) = source.schema.fields.splitAt(inputFieldIdx)
        val (leftValues, rightValues) = source.toSeq.toArray.splitAt(inputFieldIdx)

        val outputFields = leftInputFields ++ extracted.schema.fields ++ rightInputFields.tail
        val outputValues = leftValues ++ extracted.toSeq.toArray[Any] ++ rightValues.tail

        new GenericRowWithSchema(outputValues, StructType(outputFields))

      case _ =>
        extracted
    }
}
