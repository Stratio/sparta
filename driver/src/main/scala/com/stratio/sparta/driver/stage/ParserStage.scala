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
package com.stratio.sparta.driver.stage

import java.io.Serializable

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, TransformationsModel}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

trait ParserStage extends BaseStage {
  this: ErrorPersistor =>

  def parserStage(refUtils: ReflectionUtils,
                  schemas: Map[String, StructType]): Seq[Parser] =
    policy.transformations.map(parser => createParser(parser, refUtils, schemas))

  private def createParser(model: TransformationsModel,
                           refUtils: ReflectionUtils,
                           schemas: Map[String, StructType]): Parser = {
    val errorMessage = s"Something gone wrong creating the parser: ${model.`type`}. Please re-check the policy."
    val okMessage = s"Parser: ${model.`type`} created correctly."
    generalTransformation(PhaseEnum.Parser, okMessage, errorMessage) {
      val outputFieldsNames = model.outputFieldsTransformed.map(_.name)
      val schema = schemas.getOrElse(model.order.toString, throw new Exception("Can not find transformation schema"))
      refUtils.tryToInstantiate[Parser](model.`type` + Parser.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[Integer],
          classOf[Option[String]],
          classOf[Seq[String]],
          classOf[StructType],
          classOf[Map[String, Serializable]])
          .newInstance(model.order, model.inputField, outputFieldsNames, schema, model.configuration)
          .asInstanceOf[Parser])
    }
  }


}

object ParserStage extends SLF4JLogging {

  def parseEvent(row: Row, parser: Parser, removeRaw: Boolean = false): Seq[Row] =
    Try {
      parser.parse(row, removeRaw)
    } match {
      case Success(eventParsed) =>
        eventParsed
      case Failure(exception) =>
        val error = s"Failure[Parser]: ${row.mkString(",")} | Message: ${exception.getLocalizedMessage}" +
          s" | Parser: ${parser.getClass.getSimpleName}"
        log.error(error, exception)
        Seq.empty[Row]
    }

  def executeParsers(row: Row, parsers: Seq[Parser]): Seq[Row] = {
    if (parsers.size == 1) parseEvent(row, parsers.head, removeRaw = true)
    else parseEvent(row, parsers.head).flatMap(eventParsed => executeParsers(eventParsed, parsers.drop(1)))
  }

  def applyParsers(input: DStream[Row], parsers: Seq[Parser]): DStream[Row] = {
    if (parsers.isEmpty) input
    else input.mapPartitions(rows => rows.flatMap(row => executeParsers(row, parsers)), preservePartitioning = true)
  }

}
