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
package com.stratio.sparkta.driver.service

import com.stratio.sparkta.aggregator.domain.{Event, InputEvent}
import com.stratio.sparkta.aggregator.operator.CountOperator
import com.stratio.sparkta.aggregator.output.{AbstractOutput, PrintOutput}
import com.stratio.sparkta.aggregator.parser.KeyValueParser
import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.service.ValidatingPropertyMap._
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.util.Try

/**
 * Created by ajnavarro on 8/10/14.
 */
class StreamingContextService(generalConfiguration: GeneralConfiguration) {

  def createStreamingContext(aggregationPoliciesConfiguration: AggregationPoliciesDto): StreamingContext = {
    val ssc = new StreamingContext(
      configToSparkConf(generalConfiguration, aggregationPoliciesConfiguration.name),
      new Duration(generalConfiguration.duration)
    )
    var receivers: Map[String, DStream[Event]] = Map()
    aggregationPoliciesConfiguration.receivers.foreach(element => {
      val config = element.configuration
      val receiver: DStream[InputEvent] = element.elementType match {
        case "kafka" =>
          KafkaUtils.createStream(ssc = ssc,
            zkQuorum = config.getMandatory("zkQuorum"),
            groupId = config.getMandatory("groupId"),
            topics = config.getMandatory("topics")
              .split(",")
              .map(s => (s.trim, config.getMandatory("partitions").toInt))
              .toMap,
            storageLevel = StorageLevel.fromString(config.getMandatory("storageLevel"))
          ).map(data => new InputEvent(data._1.getBytes, data._2.getBytes))
        case "flume" =>
          FlumeUtils.createPollingStream(
            ssc, config.getMandatory("hostname"),
            config.getMandatory("port").toInt
            //TODO add headers
          ).map(data => new InputEvent(null, data.event.getBody.array()))
        case "socket" =>
          ssc.socketTextStream(
            config.getMandatory("hostname"),
            config.getMandatory("port").toInt,
            StorageLevel.fromString(config.getMandatory("storageLevel"))).map(data => new InputEvent(null, data.getBytes))
        case _ =>
          throw new DriverException("Receiver " + element.elementType + " not supported in receiver " + element.name)
      }

      val parser = config.getMandatory("parser") match {
        case "keyValueParser" => new KeyValueParser
      }

      receivers += (element.name -> parser.map(receiver))
    })

    var outputs: Map[String, AbstractOutput] = Map()
    aggregationPoliciesConfiguration.outputs.foreach(element => {
      //val config = element.configuration
      val output = element.elementType match {
        case "print" => new PrintOutput()
        case _ =>
          throw new DriverException("Output " + element.elementType + " not supported")
      }
      outputs += (element.name -> output)
    })

    aggregationPoliciesConfiguration.operators.foreach(element => {
      val config = element.configuration
      val operator = element.elementType match {
        case "count" =>
          new CountOperator(
            Option(config.getMandatory("window").toLong),
            Option(config.getMandatory("slideWindow").toLong),
            config.getMandatory("key")
          )
        case _ =>
          throw new DriverException("Operator " + element.elementType + " not supported")
      }
      val dstreamProcessed = receivers.get(config.getMandatory("receiverName")) match {
        case Some(receiver) =>
          operator.process(receiver)
        case _ =>
          throw new DriverException("No receiver found with name  " + config.getMandatory("receiverName"))
      }
      outputs.get(config.getMandatory("output")) match {
        case Some(output) =>
          output.save(dstreamProcessed)
        case _ =>
          throw new DriverException("No output found with name  " + config.getMandatory("output"))
      }
    })

    ssc
  }

  private def configToSparkConf(generalConfiguration: GeneralConfiguration, appName: String): SparkConf = {
    val conf = new SparkConf()
    conf.setMaster(generalConfiguration.master)
      .setAppName(appName)

    conf.set("spark.cores.max", generalConfiguration.cpus.toString)

    // Should be a -Xmx style string eg "512m", "1G"
    conf.set("spark.executor.memory", generalConfiguration.memory)

    Try(generalConfiguration.sparkHome).foreach { home => conf.setSparkHome(generalConfiguration.sparkHome)}

    // Set the Jetty port to 0 to find a random port
    conf.set("spark.ui.port", "0")

    conf
  }
}
