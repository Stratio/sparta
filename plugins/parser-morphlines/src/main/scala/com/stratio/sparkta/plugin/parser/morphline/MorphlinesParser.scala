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

package com.stratio.sparkta.plugin.parser.morphline

import java.io.{ByteArrayInputStream, Serializable => JSerializable}
import java.util.concurrent.ConcurrentHashMap

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Parser}
import com.typesafe.config.ConfigFactory
import org.kitesdk.morphline.api.{Command, MorphlineContext, Record}
import org.kitesdk.morphline.base.Compiler

import scala.collection.JavaConverters._

class MorphlinesParser(name: String,
                       order: Integer,
                       inputField: String,
                       outputFields: Seq[String],
                       properties: Map[String, JSerializable])
  extends Parser(name, order, inputField, outputFields, properties) {

  private val config: String = properties.getString("morphline")

  override def parse(data: Event): Event = {
    val record = new Record()
    data.keyMap.foreach(e => {
      if (inputField.equals(e._1)) {
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
    new Event(checkFields(MorphlinesParser(name, config).process(record).keyMap) ++ data.keyMap)
  }
}

case class MorphlineImpl(config: String) {

  private val morphlineContext: MorphlineContext = new MorphlineContext.Builder().build()

  private val collector: ThreadLocal[MorphlineEventCollector] = new ThreadLocal[MorphlineEventCollector]() {
    override def initialValue(): MorphlineEventCollector = new MorphlineEventCollector
  }

  private val morphline: ThreadLocal[Command] = new ThreadLocal[Command]() {
    override def initialValue(): Command = new Compiler()
      .compile(
        ConfigFactory.parseString(config),
        morphlineContext,
        collector.get())
  }

  def process(inputRecord: Record): Event = {
    val coll = collector.get()
    coll.reset()
    morphline.get().process(inputRecord)
    coll.records.headOption match {
      case None => new Event(Map())
      case Some(record) => toEvent(record)
    }
  }

  private def toEvent(record: Record): Event = {
    val map = record.getFields.asMap().asScala.map(m => {
      //Getting only the first element
      (m._1, m._2.asScala.headOption match {
        case Some(e) => e.asInstanceOf[JSerializable]
      })
    }).toMap
    new Event(map)
  }
}

object MorphlinesParser {

  private val instances: ConcurrentHashMap[String, MorphlineImpl] = new ConcurrentHashMap[String, MorphlineImpl]()

  def apply(name: String,config: String): MorphlineImpl = {
    Option(instances.get(config)) match {
      case Some(m) => m
      case None =>
        val morphlineImpl = new MorphlineImpl(config)
        instances.put(config, morphlineImpl)
        morphlineImpl
    }
  }
}
