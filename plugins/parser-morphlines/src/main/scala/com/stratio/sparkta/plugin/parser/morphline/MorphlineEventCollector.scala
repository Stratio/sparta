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

import com.google.common.base.Preconditions
import org.kitesdk.morphline.api.{Command, Record}

import scala.collection.mutable


class MorphlineEventCollector extends Command {

  val records = new mutable.MutableList[Record]

  override def notify(p1: Record): Unit = {}

  def reset() = {
    records.clear()
  }

  override def getParent: Command = null

  override def process(p1: Record): Boolean = {
    Preconditions.checkNotNull(p1)
    records += p1
    true
  }
}
