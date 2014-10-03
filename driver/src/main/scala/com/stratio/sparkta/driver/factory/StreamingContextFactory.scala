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
package com.stratio.sparkta.driver.factory

import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory.SparkConfHandler._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.kafka.KafkaUtils
import ValidatingPropertyMap._

/**
 * Builder used to transform a configuration file into a StreamingContext.
 * It also can be used programatically.
 */
object StreamingContextFactory {

  def getStreamingContext(aggregationPoliciesConfiguration: AggregationPoliciesDto,
                          generalConfiguration: GeneralConfiguration): StreamingContext = {

    val ssc = new StreamingContext(
      configToSparkConf(generalConfiguration, aggregationPoliciesConfiguration.name),
      new Duration(generalConfiguration.duration)
    )
    val properties = aggregationPoliciesConfiguration.receiverConfiguration
    val receiver = aggregationPoliciesConfiguration.receiver match {
      case "kafka" =>
        KafkaUtils.createStream(ssc = ssc,
          zkQuorum = properties.getMandatory("zkQuorum"),
          groupId = properties.getMandatory("groupId"),
          topics = properties.getMandatory("topics")
            .split(",")
            .map(s => (s.trim, properties.getMandatory("partitions").toInt))
            .toMap,
          storageLevel = StorageLevel.fromString(properties.getMandatory("storageLevel"))
        )
      case "flume" =>
        FlumeUtils.createPollingStream(
          ssc, properties.getMandatory("hostname"),
          properties.getMandatory("port").toInt
        )
      case "socket" =>
        ssc.socketTextStream(
          properties.getMandatory("hostname"),
          properties.getMandatory("port").toInt,
          StorageLevel.fromString(properties.getMandatory("storageLevel")))
      case _ =>
        throw new DriverException("Receiver " + aggregationPoliciesConfiguration.receiver + " not supported.")
    }
    //TODO add transformations and actions to dstream
    receiver.print()

    ssc
  }


}
