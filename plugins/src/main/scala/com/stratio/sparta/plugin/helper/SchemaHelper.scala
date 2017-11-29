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


package com.stratio.sparta.plugin.helper

import akka.event.slf4j.SLF4JLogging
import com.databricks.spark.avro.SchemaConverters
import com.stratio.sparta.plugin.enumerations.SchemaInputMode
import com.stratio.sparta.plugin.enumerations.SchemaInputMode._
import org.apache.avro.Schema
import org.apache.spark.sql.catalyst.parser.LegacyTypeStringParser
import org.apache.spark.sql.json.RowJsonHelper.extractSchemaFromJson
import org.apache.spark.sql.types.{DataType, StructType}

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
            log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event", e)
            Failure(e)
        } toOption
      }
    }

  def getAvroSparkSchema(useRowSchema: Boolean, schemaProvided: Option[String]): Option[StructType] =
    schemaProvided flatMap { schemaStr =>
      if (useRowSchema) None
      else Try(getSparkSchemaFromAvroSchema(getAvroSchemaFromString(schemaStr))).recoverWith { case e =>
        log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event", e)
        Failure(e)
      }.toOption
    }

  def getAvroSchema(useRowSchema: Boolean, schemaProvided: Option[String]): Option[Schema] =
    schemaProvided flatMap { schemaStr =>
      if (useRowSchema) None
      else Try(getAvroSchemaFromString(schemaStr)).recoverWith { case e =>
        log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each event", e)
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

  private def getSparkSchemaFromString(schemaStr: String): Try[StructType] =
    Try { // Try to deserialize the schema assuming it is in JSON format
      DataType.fromJson(schemaStr)
    } orElse Try { // If it wasn't a JSON, try assuming it is an string serialization of `StructType`
      LegacyTypeStringParser.parse(schemaStr)
    } flatMap { schema =>
      Try(schema.asInstanceOf[StructType])
    }
}
