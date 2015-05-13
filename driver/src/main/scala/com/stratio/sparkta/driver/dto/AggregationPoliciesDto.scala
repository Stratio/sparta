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
                                  saveRawData: String = "true",
                                  rawDataParquetPath: String = "default",
                                  checkpointDir: String = "checkpoint",
                                  timeBucket: String = "",
                                  checkpointGranularity: String = "minute",
                                  checkpointInterval: Int = AggregationPoliciesDto.CheckPointInterval,
                                  checkpointTimeAvailability: Int = AggregationPoliciesDto.checkpointTimeAvailability,
                                  duration: Long = AggregationPoliciesDto.StreamingWindowDurationInMillis,
                                  dimensions: Seq[DimensionDto],
                                  rollups: Seq[RollupDto],
                                  operators: Seq[PolicyElementDto],
                                  inputs: Seq[PolicyElementDto],
                                  parsers: Seq[PolicyElementDto],
                                  outputs: Seq[PolicyElementDto])

case object AggregationPoliciesDto {
  val StreamingWindowDurationInMillis = 2000
  val CheckPointInterval = 20000
  val checkpointTimeAvailability = 60000
}

object AggregationPoliciesValidator {

  def validateDto(aggregationPoliciesDto: AggregationPoliciesDto): (Boolean, String) = {
    val (isValidAgainstSchema: Boolean, isValidAgainstSchemaMsg: String) = validateAgainstSchema(aggregationPoliciesDto)
    val (isValidRollup: Boolean, isRollupInDimensionsMsg: String) =
      validateRollups(aggregationPoliciesDto)

    val isValid = isValidRollup && isValidAgainstSchema
    val errorMsg = isRollupInDimensionsMsg ++ isValidAgainstSchemaMsg
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

  def validateRollups(aggregationPoliciesDto: AggregationPoliciesDto): (Boolean, String) = {

    val dimensionNames = aggregationPoliciesDto.dimensions.map(_.name)

    val rollupNames = aggregationPoliciesDto.rollups
      .flatMap(x => Option(x))
      .flatMap(x => Option(x.dimensionAndBucketTypes))
      .flatten
      .map(_.dimensionName)

    val rollupNotInDimensions = rollupNames.filter(!dimensionNames.contains(_))
    val isRollupInDimensions = rollupNotInDimensions.isEmpty
    val isRollupInDimensionsMsg =
      if (!isRollupInDimensions) "All rollups should be declared in dimensions block\n" else ""
    (isRollupInDimensions, isRollupInDimensionsMsg)
  }
}
