/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.serving.core.models

import java.io._

import com.fasterxml.jackson.databind._
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.collection.JavaConversions._

case class AggregationPoliciesModel(id: Option[String] = None,
                                    version: Option[Int] = None,
                                    storageLevel: Option[String] = AggregationPoliciesModel.storageDefaultValue,
                                    name: String,
                                    description: String = "default description",
                                    sparkStreamingWindow: Long = AggregationPoliciesModel.sparkStreamingWindow,
                                    checkpointPath: String,
                                    rawData: RawDataModel,
                                    transformations: Seq[TransformationsModel],
                                    cubes: Seq[CubeModel],
                                    input: Option[PolicyElementModel] = None,
                                    outputs: Seq[PolicyElementModel],
                                    fragments: Seq[FragmentElementModel])

case object AggregationPoliciesModel {

  val sparkStreamingWindow = 2000
  val storageDefaultValue = Some("MEMORY_AND_DISK_SER_2")

  def checkpointPath(policy: AggregationPoliciesModel): String =
    s"${policy.checkpointPath}/${policy.name}"
}

case class PolicyWithStatus(status: PolicyStatusEnum.Value,
                            policy: AggregationPoliciesModel)

case class PolicyResult(policyId: String, policyName: String)

object AggregationPoliciesValidator extends SparktaSerializer {

  final val MessageCubeName = "All cubes must have a non empty name\n"
  final val MessageDurationGranularity = "The duration must be less than checkpoint interval\n"

  def validateDto(aggregationPoliciesDto: AggregationPoliciesModel): (Boolean, String) = {
    val (isValidAgainstSchema: Boolean, isValidAgainstSchemaMsg: String) = validateAgainstSchema(aggregationPoliciesDto)

    val isValidDurationGranularity = aggregationPoliciesDto.cubes
      .forall(cube => aggregationPoliciesDto.sparkStreamingWindow < cube.checkpointConfig.interval)

    val isValidDurationGranularityMsg = if (!isValidDurationGranularity) MessageDurationGranularity else ""

    val isValid = isValidAgainstSchema && isValidDurationGranularity
    val errorMsg = isValidAgainstSchemaMsg ++ isValidDurationGranularityMsg
    (isValid, errorMsg)
  }

  def validateAgainstSchema(aggregationPoliciesDto: AggregationPoliciesModel): (Boolean, String) = {

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
      msg = getMessageErrorFromValidation(report)
    } catch {
      case ise: InvalidSchemaException => isValid = false; msg = ise.getLocalizedMessage
    }

    (isValid, msg)
  }

  private def getMessageErrorFromValidation(report: ProcessingReport) : String = {
    report.iterator().map(message => {
      s"No usable value for ${message.asJson().findValue("instance").findValue("pointer").asText()
        .replaceAll("/[0-9]/", "-").stripPrefix("/").capitalize}. ${message.getMessage.capitalize}."
    }).mkString("\n")
  }
}
