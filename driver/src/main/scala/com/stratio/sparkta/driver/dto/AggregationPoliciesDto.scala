/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.dto

import java.io._

import com.fasterxml.jackson.databind._
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.stratio.sparkta.sdk.JsoneyStringSerializer

case class AggregationPoliciesDto(name: String = "default",
                                  sparkStreamingWindow: Long = AggregationPoliciesDto.sparkStreamingWindow,
                                  rawData: RawDataDto,
                                  parsers: Seq[PolicyElementDto],
                                  fields: Seq[FieldDto],
                                  cubes: Seq[CubeDto],
                                  inputs: Seq[PolicyElementDto],
                                  outputs: Seq[PolicyElementDto],
                                  fragments: Seq[FragmentElementDto],
                                  checkpointing: CheckpointDto)

case object AggregationPoliciesDto {

  val sparkStreamingWindow = 2000
}

object AggregationPoliciesValidator {

  final val MessageCubeName = "All cubes must have a non empty name\n"
  final val MessageDurationGranularity = "The duration must be less than checkpoint interval\n"

  def validateDto(aggregationPoliciesDto: AggregationPoliciesDto): (Boolean, String) = {
    val (isValidAgainstSchema: Boolean, isValidAgainstSchemaMsg: String) = validateAgainstSchema(aggregationPoliciesDto)
    val (isValidCube: Boolean, isCubeInDimensionsMsg: String) = validateCubes(aggregationPoliciesDto)
    val isValidDurationGranularity =
      aggregationPoliciesDto.sparkStreamingWindow < aggregationPoliciesDto.checkpointing.interval
    val isValidDurationGranularityMsg = if(!isValidDurationGranularity) MessageDurationGranularity else ""

    val isValid = isValidCube && isValidAgainstSchema && isValidDurationGranularity
    val errorMsg = isCubeInDimensionsMsg ++ isValidAgainstSchemaMsg ++ isValidDurationGranularityMsg
    (isValid, errorMsg)
  }

  def validateAgainstSchema(aggregationPoliciesDto: AggregationPoliciesDto): (Boolean, String) = {

    implicit val formats = DefaultFormats + new JsoneyStringSerializer()

    var isValid: Boolean = false
    var msg: String = ""

    val policyJsonAST = Extraction.decompose(aggregationPoliciesDto)

    val mapper: ObjectMapper = new ObjectMapper()
    val policy: JsonNode = mapper.readTree(pretty(policyJsonAST))

    val fileReader: BufferedReader =
      new BufferedReader(new InputStreamReader(AggregationPoliciesValidator
        .getClass.getClassLoader.getResourceAsStream("policy_schema.json")))
    val jsonSchema: JsonNode = mapper.readTree(fileReader)
    val schema: JsonSchema = JsonSchemaFactory.byDefault.getJsonSchema(jsonSchema)

    try {
      val report: ProcessingReport = schema.validate(policy)
      isValid = report.isSuccess
      msg = report.toString
    } catch {
      case ise: InvalidSchemaException => isValid = false; msg = ise.getLocalizedMessage
    }

    (isValid, msg)
  }

  def validateCubes(aggregationPoliciesDto: AggregationPoliciesDto): (Boolean, String) = {

    val hasCubesNames = aggregationPoliciesDto.cubes.filter(c => c.name == null || c.name.isEmpty).isEmpty
    val dimensionNames = aggregationPoliciesDto.fields.map(_.name)
    val cubeDimension = aggregationPoliciesDto.cubes
      .flatMap(x => Option(x))
      .flatMap(x => Option(x.dimensions))
      .flatten
      .map(_.dimension)

    if(!hasCubesNames) (false, MessageCubeName)
    else checkCubeParameter(cubeDimension, dimensionNames, "fields")

  }

  private def checkCubeParameter(cubeNames: Seq[String], parameterNames: Seq[String], label: String):
  (Boolean, String) = {
    val parameterNotIn = cubeNames.filter(!parameterNames.contains(_))
    val isParameterIn = parameterNotIn.isEmpty
    val isCubeInMsg =
      if (!isParameterIn) s"All references to $label in cubes should be declared in $label block" else ""

    (isParameterIn, isCubeInMsg)
  }
}
