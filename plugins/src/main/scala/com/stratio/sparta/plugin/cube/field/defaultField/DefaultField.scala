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
package com.stratio.sparta.plugin.default

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{DimensionType, Precision}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.pipeline.schema.TypeOp.TypeOp

case class DefaultField(props: Map[String, JSerializable], override val defaultTypeOperation : TypeOp)
  extends DimensionType with JSerializable with SLF4JLogging {

  def this(defaultTypeOperation : TypeOp) {
    this(Map(), defaultTypeOperation)
  }

  def this(props: Map[String, JSerializable]) {
    this(props,  TypeOp.String)
  }

  def this() {
    this(Map(), TypeOp.String)
  }

  override val operationProps: Map[String, JSerializable] = props

  override val properties: Map[String, JSerializable] = props

  override def precisionValue(keyName: String, value: Any): (Precision, Any) = {
    val precision = DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
    (precision, TypeOp.castingToSchemaType(precision.typeOp, value))
  }

  override def precision(keyName: String): Precision =
    DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
}
