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

package com.stratio.benchmark.generator.actors

import java.util.UUID

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.benchmark.generator.runners.GeneratorRunner
import com.stratio.kafka.benchmark.generator.kafka.KafkaProducer
import com.stratio.models.benchmark.generator.models.{ConfigModel, RawModel}
import kafka.producer.Producer
import org.json4s.native.Serialization._
import org.json4s.{DefaultFormats, Formats}

import scala.annotation.tailrec
import scala.io.Source

class DataGeneratorActor(producer: Producer[String,String]) extends Actor with SLF4JLogging {

  val DefaultFailureTimeout = 2000L
  val NumberOfClients = 1000
//  var producer: Producer[String, String] = _
//  var topic: String = _

  implicit val formats: Formats = DefaultFormats

  val geolocations = generateGeolocations()
  val clientIdCreditCard: Map[Int, String] = generateClientIdCreditCard((1 to NumberOfClients).toSeq, Map())
  val clientIdGeo: Map[Int, (Double, Double)] = generateClientIdGeo(clientIdCreditCard, geolocations)




  override def receive: Receive = {
    case DataGeneratorActor.CreateInstance(count , eventNum) =>
      while(true) {
        createInstance(count, eventNum)
      //  Thread.sleep(500l)
      }
      //sender ! Finish
      //context.stop(self)
  }





  @tailrec
  private def generateRaw(clientIdGeo: Map[Int, (Double, Double)],
                          clientIdCreditCard: Map[Int, String],
                          config: ConfigModel,
                          count: Int,
                          eventNum: Int)(implicit formats: Formats): Unit = {

    //scalastyle:off
    val id = UUID.randomUUID().toString
    val timestamp = RawModel.generateTimestamp()
    val clientId = if(config.generateAlert == 0 || config.generateAlert == 1) 10 else RawModel.generateRandomInt(1,
      NumberOfClients)
    val latitude = clientIdGeo.get(clientId).get._1
    val longitude = clientIdGeo.get(clientId).get._2
    val paymentMethod = RawModel.generatePaymentMethod()
    val creditCard = if(config.generateAlert == 1) RawModel.generateCreditCard("") else clientIdCreditCard.get(clientId).get
    val shoppingCenter = RawModel.generateShoppingCenter()
    val employee = RawModel.generateRandomInt(1, 300)
    //scalastyle: on


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


    KafkaProducer.send(producer, GeneratorRunner.topic, write(rawModel) )

    if(eventNum == count){
     // context.stop(self)
    } else {
      generateRaw(clientIdGeo, clientIdCreditCard, config, count + 1, eventNum)
    }
  }

  def createInstance(count: Int, eventNum: Int): Unit = {
    generateRaw(clientIdGeo, clientIdCreditCard, ConfigModel(), count, eventNum)
  }

  private def generateGeolocations() : Seq[String] = {
    Source.fromInputStream(
      this.getClass.getClassLoader.getResourceAsStream("geolocations.csv")).getLines().toSeq
  }

  private def generateClientIdCreditCard(idClients: Seq[Int],
                                         clientIdCreditCard: Map[Int, String]): Map[Int, String] = {
    if(idClients.size == 0) {
      clientIdCreditCard
    } else {
      val newIdClients = idClients.init
      val newClientIdCreditCard = clientIdCreditCard + (idClients.last -> RawModel.generateCreditCard(""))
      generateClientIdCreditCard(newIdClients, newClientIdCreditCard)
    }
  }

  private def generateClientIdGeo(clientIdCreditCard: Map[Int, String], geolocations: Seq[String])
  :Map[Int, (Double, Double)] = {
    clientIdCreditCard.map(x => {
      val index = RawModel.generateRandomInt(0, geolocations.size - 1)
      x._1 -> ((geolocations(index)).split(":")(0).toDouble, (geolocations(index)).split(":")(1).toDouble)
    })
  }

}

object DataGeneratorActor {

  case class CreateInstance(count: Int, eventNum: Int)
  case class Finish()
}
