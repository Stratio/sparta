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
import com.stratio.sparta.plugin.enumerations.SchemaInputMode
import com.stratio.sparta.plugin.enumerations.SchemaInputMode._
import org.apache.spark.sql.catalyst.parser.LegacyTypeStringParser
import org.apache.spark.sql.json.RowJsonHelper.extractSchemaFromJson
import org.apache.spark.sql.types.{DataType, StructType}

import scala.util.{Failure, Try}

object JsonHelper extends SLF4JLogging {

  def getJsonSchema(
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
            case EXAMPLE =>
              Try(extractSchemaFromJson(schemaStr, jsonOptions))
            case SPARKFORMAT =>
              Try { // Try to deserialize the schema assuming it is in JSON format
                DataType.fromJson(schemaStr)
              } orElse Try { // If it wasn't a JSON, try assuming it is an string serialization of `StructType`
                LegacyTypeStringParser.parse(schemaStr)
              } flatMap { schema =>
                Try(schema.asInstanceOf[StructType])
              }
            case _ => throw new Exception("Invalid input mode in json schema extractor")
          }
        } recoverWith {
          case e =>
            log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each json event", e)
            Failure(e)
        } toOption
      }
    }
}
