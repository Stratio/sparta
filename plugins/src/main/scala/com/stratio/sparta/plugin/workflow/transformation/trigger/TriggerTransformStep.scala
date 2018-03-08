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

package com.stratio.sparta.plugin.workflow.transformation.trigger

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputOptions, TransformStep, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import scala.util.{Failure, Success, Try}

import org.apache.spark.sql.json.RowJsonHelper
import org.apache.spark.sql.types.StructType
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.models.PropertiesTriggerInputsModel

//scalastyle:off
abstract class TriggerTransformStep[Underlying[Row]](
                                                      name: String,
                                                      outputOptions: OutputOptions,
                                                      transformationStepsManagement: TransformationStepManagement,
                                                      ssc: Option[StreamingContext],
                                                      xDSession: XDSession,
                                                      properties: Map[String, JSerializable]
                                                    )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val sql = properties.getString("sql").trim

  lazy val inputsModel: PropertiesTriggerInputsModel = {
    {
      implicit val json4sJacksonFormats: Formats =
        DefaultFormats + new JsoneyStringSerializer()
      read[PropertiesTriggerInputsModel](
        s"""{"inputSchemas": ${
          properties.getString("inputSchemas", None).notBlank.fold("[]") { values =>
            values.toString
          }
        }}"""
      )
    }
  }

  /**
    * Validate inputSchema names with names of input steps, also validate the input schemas
    *
    * @param inputData
    */
  def validateSchemas(inputData: Map[String, DistributedMonad[Underlying]]) = {
    if (inputsModel.inputSchemas.nonEmpty) {
      require(inputData.size == inputsModel.inputSchemas.size,
        s"$name  The inputs size must be equal than provided input trigger schemas")
      //If any of them fails
      require(!inputsModel.inputSchemas.exists(input => parserInputSchema(input.schema).isFailure),
        s"$name input schemas contains errors")
      require(inputData.keys.forall { stepName =>
        inputsModel.inputSchemas.map(_.stepName).contains(stepName)
      }, s"$name input schemas are not the same as the input step names")
    }
  }

  def parserInputSchema(schema: String): Try[StructType] =
    Try {
      SchemaHelper.getSparkSchemaFromString(schema) match {
        case Success(structType) =>
          structType
        case Failure(f) =>
          log.warn(s"$name Error parsing input schema $schema with SparkSchemaFromString. ${f.getLocalizedMessage}")
          Try(RowJsonHelper.extractSchemaFromJson(schema, Map())) match {
            case Success(structType) =>
              structType
            case Failure(e) =>
              log.warn(s"$name Error parsing input schema $schema with SchemaFromJson. ${e.getLocalizedMessage}")
              throw new Exception(s"$name Error parsing input schema")
          }
      }
    }

  def validateSql: Boolean =
    Try(xDSession.sessionState.sqlParser.parsePlan(sql)) match {
      case Success(_) =>
        true
      case Failure(e) =>
        log.warn(s"$name invalid sql. ${e.getLocalizedMessage}")
        false
    }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (sql.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input sql query can not be empty"
      )

    if (sql.nonEmpty && !validateSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input sql query is invalid"
      )

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: the input schema from step ${input.stepName} is not valid")
      }

      inputsModel.inputSchemas.filterNot(is => isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: the input table name ${is.stepName} is not valid")
      }
    }

    validation
  }

  def isCorrectTableName(tableName: String): Boolean =
    tableName.nonEmpty && tableName != "" &&
      tableName.toLowerCase != "select" &&
      tableName.toLowerCase != "project" &&
      !tableName.contains("-") && !tableName.contains("*") && !tableName.contains("/")
}

