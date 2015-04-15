/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.dto

import java.io.{FileReader, BufferedReader}

import com.fasterxml.jackson.databind._
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 * Created by ajnavarro on 2/10/14.
 */
case class AggregationPoliciesDto(name: String = "default",
                                  duration: Long = AggregationPoliciesDto.StreamingWindowDurationInMillis,
                                  dimensions: Seq[DimensionDto],
                                  rollups: Seq[RollupDto],
                                  operators: Seq[PolicyElementDto],
                                  inputs: Seq[PolicyElementDto],
                                  parsers: Seq[PolicyElementDto],
                                  outputs: Seq[PolicyElementDto])

case object AggregationPoliciesDto {
  val StreamingWindowDurationInMillis = 2000
}

object AggregationPoliciesValidator {



  def validateDto(aggregationPoliciesDto : AggregationPoliciesDto): (Boolean, String) = {
    val (isValidAgainstSchema: Boolean, isValidAgainstSchemaMsg: String) = validateAgainstSchema(aggregationPoliciesDto)
    val (isRollupInDimensions: Boolean, isRollupInDimensionsMsg: String) =
      validateRollupsInDimensions(aggregationPoliciesDto)

    val isValid = isRollupInDimensions && isValidAgainstSchema
    val errorMsg = isRollupInDimensionsMsg ++ isValidAgainstSchemaMsg
    (isValid, errorMsg)
  }

  def validateAgainstSchema(aggregationPoliciesDto : AggregationPoliciesDto): (Boolean, String) = {

    implicit val formats = DefaultFormats + new JsoneyStringSerializer()

    var isValid: Boolean = false
    var msg: String = ""

    val policyJsonAST = Extraction.decompose(aggregationPoliciesDto)

    val mapper: ObjectMapper = new ObjectMapper()
    val policy: JsonNode = mapper.readTree(pretty(policyJsonAST))

    val fileReader: BufferedReader =
      new BufferedReader(
        new FileReader(AggregationPoliciesValidator
          .getClass.getClassLoader.getResource("policy_schema.json").getPath))
    val jsonSchema: JsonNode = mapper.readTree(fileReader)
    val schema: JsonSchema = JsonSchemaFactory.byDefault.getJsonSchema(jsonSchema)

    try{
      val report: ProcessingReport = schema.validate(policy)
      isValid = report.isSuccess
      msg = report.toString
    } catch {
      case ise: InvalidSchemaException => isValid = false; msg = ise.getLocalizedMessage
    }

    (isValid, msg)
  }

  def validateRollupsInDimensions(aggregationPoliciesDto : AggregationPoliciesDto): (Boolean, String) = {

    val dimensionNames = aggregationPoliciesDto.dimensions.map(_.name)

    val rollupNames = aggregationPoliciesDto.rollups
      .flatMap(x=> Option(x)) //filter(_!=null) //Seq[Seq[Option[_]]]
      .flatMap(x => Option(x.dimensionAndBucketTypes)) //Seq[Seq[_]]
      .flatten //Seq[_]
      .map(_.dimensionName)

    val rollupNotInDimensions = rollupNames.filter(!dimensionNames.contains(_))
    val isRollupInDimensions = rollupNotInDimensions.isEmpty
    val isRollupInDimensionsMsg =
      if (!isRollupInDimensions)
        "All rollups should be declared in dimensions block\n"
      else
        ""
    (isRollupInDimensions, isRollupInDimensionsMsg)
  }


}
