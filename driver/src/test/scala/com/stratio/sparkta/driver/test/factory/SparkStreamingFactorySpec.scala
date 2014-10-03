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
  var defaultGeneralConfiguration = new GeneralConfiguration("local[2]", "")

  var socketConfigurationTest = ("socket", Map("hostname" -> "localhost", "port" -> "9999", "storageLevel" -> "MEMORY_ONLY"))
  var flumeConfigurationTest = ("flume", Map("hostname" -> "localhost", "port" -> "9999"))
  var kafkaConfigurationTest = ("kafka", Map("zkQuorum" -> "localhost:2181", "groupId" -> "test_group_id", "topics" -> "topic_test_1,topic_test_2", "partitions" -> "2", "storageLevel" -> "MEMORY_ONLY"))

  behavior of "StreamingContextFactory"

  it should "throw DriverException if wrong property is added" in {
    a[DriverException] should be thrownBy {
      StreamingContextFactory.getStreamingContext(defaultAggregationPolicies.copy(receiverConfiguration = Map("wrong" -> "bad")), defaultGeneralConfiguration)
    }  }

  it should "be ok if socket configuration is ok" in {
    StreamingContextFactory.getStreamingContext(defaultAggregationPolicies.copy(receiver = socketConfigurationTest._1, receiverConfiguration = socketConfigurationTest._2), defaultGeneralConfiguration)
  }

  it should "be ok if flume configuration is ok" in {
    StreamingContextFactory.getStreamingContext(defaultAggregationPolicies.copy(receiver = flumeConfigurationTest._1, receiverConfiguration = flumeConfigurationTest._2), defaultGeneralConfiguration)
  }

  it should "be ok if kafka configuration is ok" in {
    StreamingContextFactory.getStreamingContext(defaultAggregationPolicies.copy(receiver = kafkaConfigurationTest._1, receiverConfiguration = kafkaConfigurationTest._2), defaultGeneralConfiguration)
  }
}
