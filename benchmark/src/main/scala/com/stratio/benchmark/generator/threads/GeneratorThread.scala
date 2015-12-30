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

package com.stratio.benchmark.generator.threads

import java.util.{Date, UUID}

import akka.event.slf4j.SLF4JLogging
import com.stratio.benchmark.generator.runners.StoppedThreads
import com.stratio.kafka.benchmark.generator.kafka.KafkaProducer
import com.stratio.models.benchmark.generator.models.{RawModel, RawModelCommonData}
import kafka.producer.Producer
import org.json4s.native.Serialization._
import org.json4s.{DefaultFormats, Formats}

class GeneratorThread(producer: Producer[String,String], timeout: Long, stoppedThreads: StoppedThreads, topic: String)
  extends Runnable with SLF4JLogging with RawModelCommonData {

  implicit val formats: Formats = DefaultFormats

  var numberOfEvents = 0

  override def run: Unit = {
    generateRaw(new Date().getTime)
    producer.close()

    stoppedThreads.incrementNumberOfEvents(numberOfEvents)
    stoppedThreads.incrementNumberOfThreads
  }

  private def generateRaw(startTimeInMillis: Long): Unit = {
    while(((startTimeInMillis + timeout) - new Date().getTime) > 0) {
      val id = UUID.randomUUID.toString
      val timestamp = RawModel.generateTimestamp
      val clientId = RawModel.generateRandomInt(RawModel.Range_client_id._1, RawModel.Range_client_id._2)
      val latitude = clientIdGeo.get(clientId).get._1
      val longitude = clientIdGeo.get(clientId).get._2
      val paymentMethod = RawModel.generatePaymentMethod()
      val creditCard = clientIdCreditCard.get(clientId).get
      val shoppingCenter = RawModel.generateShoppingCenter()
      val employee = RawModel.generateRandomInt(RawModel.Range_employee._1, RawModel.Range_employee._2)

      val rawModel = new RawModel(
        id,
        timestamp,
        clientId,
        latitude,
        longitude,
        paymentMethod,
        creditCard,
        shoppingCenter,
        employee)

      KafkaProducer.send(producer, topic, write(rawModel))
      numberOfEvents = numberOfEvents + 1
    }
  }
}