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

package com.stratio.sparkta.driver.util

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

import com.stratio.sparkta.driver.models.AggregationPoliciesModel
import com.stratio.sparkta.sdk.JsoneyStringSerializer

/**
 * Utils for policies.
 * @author anistal,sgomezg
 */
object PolicyUtils {

  /**
   * Method to parse AggregationPoliciesModel from JSON string
   * @param json The policy as JSON string
   * @return AggregationPoliciesModel
   */
  def parseJson(json: String): AggregationPoliciesModel = {
    implicit val formats = DefaultFormats + new JsoneyStringSerializer()
    parse(json).extract[AggregationPoliciesModel]
  }
}
