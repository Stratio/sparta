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
package com.stratio.sparta.sdk.pipeline.aggregation.cube

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.schema.{TypeConversions, TypeOp}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp.TypeOp

case class Precision(id: String, typeOp: TypeOp, properties: Map[String, JSerializable]) {

  def this(id: String, typeOp: TypeOp) {
    this(id, typeOp, Map())
  }
}

trait DimensionType extends TypeConversions {

  /**
   * When writing to the cube at some address, the address will have one coordinate for each
   * dimension in the cube, for example (time: 348524388, location: portland). For each
   * dimension, for each precision type within that dimension, the dimensionType must transform the
   * input data into the precision that should be used to store the data.
   *
   * @param value Used to generate the different precisions
   * @return Map with all generated precisions and a sequence with all values
   */
  def precisionValue(keyName: String, value: Any): (Precision, Any)

  /**
   * All precisions supported into this dimensionType
   *
   * @return Sequence of Precisions
   */
  def precision(keyName : String): Precision

  def properties: Map[String, JSerializable] = Map()

  override def defaultTypeOperation: TypeOp = TypeOp.String

}

object DimensionType {

  final val IdentityName = "identity"
  final val IdentityFieldName = "identityField"
  final val TimestampName = "timestamp"
  final val DefaultDimensionClass = "Default"

  def getIdentity(typeOperation: Option[TypeOp], default: TypeOp): Precision =
    new Precision(IdentityName, typeOperation.getOrElse(default))

  def getIdentityField(typeOperation: Option[TypeOp], default: TypeOp): Precision =
    new Precision(IdentityFieldName, typeOperation.getOrElse(default))

  def getTimestamp(typeOperation: Option[TypeOp], default: TypeOp): Precision =
    new Precision(TimestampName, typeOperation.getOrElse(default))

}
