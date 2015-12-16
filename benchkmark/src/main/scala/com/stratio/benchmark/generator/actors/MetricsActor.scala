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

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging

import scala.io.Source


class MetricsActor(path: String) extends Actor with SLF4JLogging {

  override def receive: Receive = {
    case FileReader(path) => sender ! parseCSVtoMap(path)
  }


  def parseCSVtoMap(path: String): Map[String, String] = {
    val processingTime: Map[String, String] =
      Source.fromFile(path)
        .getLines()
        .toList
        .tail
        .map(getTuple)
        .toMap
    processingTime
  }

  def processingTime(startTime: Map[String, String], endTime: Map[String, String]): Unit ={
  }

  def getTuple(s: String): (String, String) = {
    val stringSplit = s.split(",")
    if(stringSplit.size == 2)
      (stringSplit(0), stringSplit(1))
    else
      throw new Exception("Invalid format")
  }

}

case class FileReader(path: String)

