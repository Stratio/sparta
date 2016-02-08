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
import com.stratio.sparkta.serving.core.helpers.OperationsHelper
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import scala.collection.JavaConversions._
import com.stratio.sparkta.serving.core.helpers

case class CommonPoliciesModel(id: Option[String] = None,
                               version: Option[Int] = None,
                               storageLevel: Option[String] = CommonPoliciesModel.storageDefaultValue,
                               name: String,
                               description: String = "default description",
                               sparkStreamingWindow: Long = CommonPoliciesModel.sparkStreamingWindow,
                               checkpointPath: String,
                               rawData: RawDataModel,
                               transformations: Seq[TransformationsModel],
                               cubes: Seq[CommonCubeModel],
                               input: Option[PolicyElementModel] = None,
                               outputs: Seq[PolicyElementModel],
                               fragments: Seq[FragmentElementModel])

case object CommonPoliciesModel {

  val sparkStreamingWindow = 2000
  val storageDefaultValue = Some("MEMORY_AND_DISK_SER_2")
  def checkpointPath(policy: CommonPoliciesModel): String =
    s"${policy.checkpointPath}/${policy.name}"
}

case class PolicyWithStatus(status: PolicyStatusEnum.Value,
                            policy: CommonPoliciesModel)

case class PolicyResult(policyId: String, policyName: String)

object CommonPoliciesValidator extends SparktaSerializer {

  final val MessageCubeName = "All cubes must have a non empty name\n"
  final val MessageCheckpointInterval = "The Checkpoint interval has to be at least 5 times greater" +
    " than the Spark Streaming Window and also they have to be multiples\n"
  final val msgCheckpointInterval = write(new ErrorModel(ErrorModel.CodePolicyIsWrong, MessageCheckpointInterval))
  final val MessageComputeLast = "ComputeLast value has to be greater than the precision in order to prevent" +
    " data loss\n"
  final val msgComputeLast = write(new ErrorModel(ErrorModel.CodePolicyIsWrong, MessageComputeLast))

  /**
    * This function validates that the checkpoint interval and the computeLast are correct.
    * @param commonPoliciesDto
    * @return
    */
  def validateDto(commonPoliciesDto: CommonPoliciesModel): (Boolean, String) = {
    val (isValidAgainstSchema: Boolean, isValidAgainstSchemaMsg: String) = validateAgainstSchema(commonPoliciesDto)

    val isValidDurationGranularity = commonPoliciesDto
      .cubes
      .forall(cube =>
        OperationsHelper.checkValidCheckpointInterval(
          commonPoliciesDto.sparkStreamingWindow,cube.checkpointConfig.checkpointEvery.get))
    val isValidComputeLast = commonPoliciesDto
      .cubes
      .forall(cube =>
        OperationsHelper.checkValidComputeLastValue(
          cube.checkpointConfig.computeLast.get, cube.checkpointConfig.precision))

    val isValidComputeLastMsg = if(!isValidComputeLast) msgComputeLast else ""
    val isValidDurationGranularityMsg = if (!isValidDurationGranularity) msgCheckpointInterval else ""
    val isValid = isValidAgainstSchema && isValidDurationGranularity && isValidComputeLast
    val errorMsg = isValidAgainstSchemaMsg ++ isValidDurationGranularityMsg ++ isValidComputeLastMsg
    (isValid, errorMsg)
  }

  /**
    * This function validates that the policy is correct according with the given schema.
    * @param commonPoliciesDto
    * @return
    */
  def validateAgainstSchema(commonPoliciesDto: CommonPoliciesModel): (Boolean, String) = {

    var isValid: Boolean = false
    var msg: String = ""

    val policyJsonAST = Extraction.decompose(commonPoliciesDto)

    val mapper: ObjectMapper = new ObjectMapper()
    val policy: JsonNode = mapper.readTree(pretty(policyJsonAST))

    val fileReader: BufferedReader =
      new BufferedReader(new InputStreamReader(CommonPoliciesValidator
        .getClass.getClassLoader.getResourceAsStream("common_policy_schema.json")))
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

  private def getMessageErrorFromValidation(report: ProcessingReport): String = {
    report.iterator().map(message => {
      s"No usable value for ${
        message.asJson().findValue("instance").findValue("pointer").asText()
          .replaceAll("/[0-9]/", "-").stripPrefix("/").capitalize
      }. ${message.getMessage.capitalize}."
    }).mkString("\n")
  }
}