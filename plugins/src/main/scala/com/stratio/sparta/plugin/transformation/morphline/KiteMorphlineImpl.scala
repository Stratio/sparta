/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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
package com.stratio.sparta.plugin.transformation.morphline

import com.typesafe.config.Config
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructField
import org.kitesdk.morphline.api.{Command, MorphlineContext, Record}
import org.kitesdk.morphline.base.Compiler

case class KiteMorphlineImpl(config: Config, outputFieldsSchema: Array[StructField]) {

  private val morphlineContext: MorphlineContext = new MorphlineContext.Builder().build()

  private val collector: ThreadLocal[EventCollector] = new ThreadLocal[EventCollector]() {
    override def initialValue(): EventCollector = new EventCollector(outputFieldsSchema)
  }

  private val morphline: ThreadLocal[Command] = new ThreadLocal[Command]() {
    override def initialValue(): Command = new Compiler().compile(config, morphlineContext, collector.get())
  }

  def process(inputRecord: Record): Row = {
    val coll = collector.get()
    coll.reset()
    morphline.get().process(inputRecord)
    coll.row
  }
}