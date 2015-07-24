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

package com.stratio.sparkta.plugin.field.arrayText

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging

import com.stratio.sparkta.sdk._

class ArrayTextField(props: Map[String, JSerializable]) extends DimensionType with JSerializable with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val operationProps: Map[String, JSerializable] = props

  override val properties: Map[String, JSerializable] = props

  override val defaultTypeOperation = TypeOp.String

  override def precisionValue(keyName: String, value: JSerializable): (Precision, JSerializable) = {
    value.asInstanceOf[Seq[String]].zipWithIndex.find{ case (item, index) => item == keyName} match {
      case Some(find) => {
        val precision = getPrecision(find._1 + find._2, getTypeOperation)
        (precision, TypeOp.transformValueByTypeOp(precision.typeOp, find._2.asInstanceOf[JSerializable]))
      }
      case None => (DimensionType.getIdentity(None, TypeOp.String), 0)
    }
  }

  override def precision(keyName: String): Precision =
    DimensionType.getIdentity (getTypeOperation, defaultTypeOperation)
}
