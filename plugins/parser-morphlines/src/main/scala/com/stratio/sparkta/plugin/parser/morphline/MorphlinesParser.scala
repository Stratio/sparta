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
package com.stratio.sparkta.plugin.parser.morphline

import java.io.{ByteArrayInputStream, Serializable}

import com.stratio.sparkta.plugin.parser.morphline.MorphlineEventCollector
import com.stratio.sparkta.plugin.parser.morphline.MorphlinesParser._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Input, Parser}
import com.typesafe.config.ConfigFactory
import org.kitesdk.morphline.api.{Command, MorphlineContext, Record}
import org.kitesdk.morphline.base.Compiler

import scala.collection.JavaConverters._

class MorphlinesParser(properties: Map[String, Serializable]) extends Parser(properties) {

  private val config: String = properties.getString("morphline")

  override def parse(data: Event): Event = {
    collectorInstance.reset
    val record = new Record()
    data.keyMap.foreach(e => {
      if (Input.RAW_DATA_KEY.equals(e._1)) {
        //TODO: This actually needs getting raw bytes from the origin
        val result = e._2 match {
          case s: String => new ByteArrayInputStream(s.getBytes("UTF-8"))
          case b: Array[Byte] => new ByteArrayInputStream(b)
        }
        record.put(e._1, result)
      } else {
        record.put(e._1, e._2)
      }
    })

    morphlineInstance(config).process(record)
    if (collectorInstance.records.isEmpty) {
      new Event(Map())
    } else {
      toEvent(collectorInstance.records.head)
    }
  }
}

object MorphlinesParser {
  private lazy val morphlineContext: MorphlineContext = new MorphlineContext.Builder().build()
  private val collector: ThreadLocal[MorphlineEventCollector] = new ThreadLocal[MorphlineEventCollector]() {
    override def initialValue() : MorphlineEventCollector = new MorphlineEventCollector
  }
  private var morphline: Command = null

  private def morphlineInstance(config: String): Command = {
    if (morphline == null) {
      morphline = new Compiler()
        .compile(
          ConfigFactory.parseString(config),
          contextInstance(),
          collectorInstance())
    }
    morphline
  }

  private def collectorInstance(): MorphlineEventCollector = collector.get()

  private def contextInstance(): MorphlineContext = morphlineContext

  private def toEvent(record: Record): Event = {
    val map = record.getFields.asMap().asScala.map(m => {
      //Getting only the first element
      (m._1, m._2.asScala.headOption match {
        case Some(e) => e.asInstanceOf[Serializable]
        case None => null
      })
    }).toMap
    new Event(map)
  }
}
