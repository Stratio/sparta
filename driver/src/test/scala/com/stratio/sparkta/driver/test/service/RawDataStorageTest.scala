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

package com.stratio.sparkta.driver.test.service

import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.sdk.{Input, Event}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class RawDataStorageTest extends FlatSpec with ShouldMatchers {
  val service = new RawDataStorageService(null, "")
  "RawDataStorage" should "return a event" in {
    val service = new RawDataStorageService(null, "")
    val inputEvent = new Event(Map(Input.RawDataKey -> "hola"))

    val result = service.extractRawFromEvent(inputEvent)

    result should be("hola")
  }
  it should "return a raw event" in {
    val service = new RawDataStorageService(null, "")
    val inputEvent = new Event(Map(Input.RawDataKey -> "hola"), Some("rawData"))
    val result = service.extractRawFromEvent(inputEvent)
    result should be("rawData")
  }
  it should "return a raw " in {
    val service = new RawDataStorageService(null, "")
    val inputEvent = new Event(Map("other key" -> "hola","other one" -> "adios"))
    val result = service.extractRawFromEvent(inputEvent)
    result should be("hola###adios")
  }


}
