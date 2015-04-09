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

    def validateRollupsInDimensions: (Boolean, String) = {

      val dimensionNames = aggregationPoliciesDto.dimensions.map(_.name)

      val rollupNames = aggregationPoliciesDto.rollups.flatMap(_.dimensionAndBucketTypes).map(_.dimensionName)

      val isRollupInDimensions = dimensionNames.containsSlice(rollupNames)
      val isRollupInDimensionsMsg =
        if (!isRollupInDimensions)
          "All rollups should be declared in dimensions block\n"
        else
          ""
      (isRollupInDimensions, isRollupInDimensionsMsg)
    }

    val (isRollupInDimensions: Boolean, isRollupInDimensionsMsg: String) = validateRollupsInDimensions

    val isValid = isRollupInDimensions
    val errorMsg = isRollupInDimensionsMsg
    (isValid, errorMsg)
  }
}
