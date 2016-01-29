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

case class AggregationOldPoliciesModel(id: Option[String] = None,
                               version: Option[Int] = None,
                               storageLevel: Option[String] = AggregationOldPoliciesModel.storageDefaultValue,
                               name: String,
                               description: String = "default description",
                               sparkStreamingWindow: Long = AggregationOldPoliciesModel.sparkStreamingWindow,
                               checkpointPath: String,
                               rawData: RawDataModel,
                               transformations: Seq[TransformationsModel],
                               cubes: Seq[CubeModel],
                               input: Option[PolicyElementModel] = None,
                               outputs: Seq[PolicyElementModel],
                               fragments: Seq[FragmentElementModel])

case object AggregationOldPoliciesModel {

  val sparkStreamingWindow = 2000
  val storageDefaultValue = Some("MEMORY_AND_DISK_SER_2")
}
