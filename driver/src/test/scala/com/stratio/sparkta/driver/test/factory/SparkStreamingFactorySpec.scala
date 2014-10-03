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
package com.stratio.sparkta.driver.test.factory

import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory.StreamingContextFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by ajnavarro on 2/10/14.
 */
class SparkStreamingFactorySpec extends FlatSpec with Matchers {
  var defaultAggregationPolicies = new AggregationPoliciesDto("test1", "socket", Map("hostname" -> "localhost", "port" -> "9999", "storageLevel" -> "MEMORY_ONLY"), null, null)
  var defailtGeneralConfiguration = new GeneralConfiguration("local[2]", "")

  behavior of "StreamingContextFactory"

  it should "throw DriverException if wrong property is added" in {
    a[DriverException] should be thrownBy {
      StreamingContextFactory.getStreamingContext(defaultAggregationPolicies.copy(receiverConfiguration = Map("wrong" -> "bad")), defailtGeneralConfiguration)
    }
  }

  it should "be ok if all configuration is ok" in {
    StreamingContextFactory.getStreamingContext(defaultAggregationPolicies, defailtGeneralConfiguration)
  }
}
