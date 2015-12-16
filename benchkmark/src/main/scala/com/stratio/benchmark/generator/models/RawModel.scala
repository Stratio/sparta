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

package com.stratio.models.benchmark.generator.models

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.annotation.tailrec
import scala.io.Source
import scala.util.Random

case class RawModel (order_id: String,
                     timestamp: String,
                     client_id: Integer,
                     latitude: Double,
                     longitude: Double,
                     payment_method: String,
                     credit_card: String,
                     shopping_center: String,
                     employee: Integer) {}

object RawModel {

  val Range_client_id = (1, 30000)
  val Range_payment_method = Source.fromInputStream(
    this.getClass.getClassLoader.getResourceAsStream("payment-methods.txt")).getLines().toSeq
  val Range_shopping_center = Source.fromInputStream(
    this.getClass.getClassLoader.getResourceAsStream("shopping-centers.txt")).getLines().toSeq
  val Range_employee = (1, 300)
  val Range_quantity = (1, 30)
  val Range_timestap = (0, 60)
  val Range_creditCard = (0, 9)
  val R = Random
  val DigitsCreditCard = 16

  val Range_family_product: Map[String, Map[String,Float]] = Source.fromInputStream(
    this.getClass.getClassLoader.getResourceAsStream("family-products.csv")).getLines().map(x => {
      val splitted = x.split(",")
      (splitted(0), Map(splitted(1) -> splitted(2).toFloat))
    }).toMap

  def generateShoppingCenter(): String = {
    Range_shopping_center(generateRandomInt(0, Range_shopping_center.length - 1))
  }

  def generatePaymentMethod(): String = {
    Range_payment_method(generateRandomInt(0, Range_payment_method.length - 1))
  }

  def generateTimestamp(): String = {
    val datetime = new DateTime().minusDays(generateRandomInt(Range_timestap._1, Range_timestap._2))
    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss ZZ").print(datetime)
  }

  def generateRandomInt(min: Int, max: Int): Int = {
    R.nextInt((max -min) + 1) + min
  }

  @tailrec
  def generateCreditCard(current: String): String = {
    if(current.length != DigitsCreditCard)
      generateCreditCard(current + generateRandomInt(Range_creditCard._1, Range_creditCard._2))
    else current
  }
}