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
package com.stratio.sparta.driver.util

import java.io.File
import scala.util.Either
import scala.util.Left
import scala.util.Right

import akka.event.slf4j.SLF4JLogging
import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.stratio.sparta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparta.serving.core.models.SpartaSerializer

/**
 * Utils for policies.
 *
 * @author sgomezg
 */
object PolicyUtils extends SpartaSerializer with SLF4JLogging {

  /**
   * Method to parse AggregationPoliciesModel from JSON string
   *
   * @param json The policy as JSON string
   * @return AggregationPoliciesModel
   */
  def parseJson(json: String): AggregationPoliciesModel = parse(json).extract[AggregationPoliciesModel]

  def jarsFromPolicy(apConfig: AggregationPoliciesModel): Seq[File] = {
    apConfig.userPluginsJars.distinct.map(filePath => new File(filePath))
  }

}
