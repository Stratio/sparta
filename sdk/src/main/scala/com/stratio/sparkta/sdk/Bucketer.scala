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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp.TypeOp

case class BucketType(id: String, typeOp: TypeOp, properties: Map[String, JSerializable]) {

  def this(id: String, typeOp: TypeOp) {
    this(id, typeOp, Map())
  }
}

trait Bucketer {

  /**
   * When writing to the cube at some address, the address will have one coordinate for each
   * dimension in the cube, for example (time: 348524388, location: portland). For each
   * dimension, for each bucket type within that dimension, the bucketer must transform the
   * input data into the bucket that should be used to store the data.
   *
   * @param value Used to generate the different bucketTypes
   * @return Map with all generated bucketTypes and a sequence with all values
   */
  def bucket(value: JSerializable): Map[BucketType, JSerializable]

  final val TypeOperationName = "typeOp"

  /**
   * All buckets supported into this bucketer
   *
   * @return Sequence of BucketTypes
   */
  def bucketTypes: Map[String, BucketType]

  def properties: Map[String, JSerializable] = Map()

  def defaultTypeOperation: TypeOp = TypeOp.String

  def getTypeOperation: Option[TypeOp] = getResultType(TypeOperationName)

  def getTypeOperation(typeOperation: String): Option[TypeOp] =
    if (!typeOperation.isEmpty && properties.contains(typeOperation)) getResultType(typeOperation)
    else getResultType(TypeOperationName)

  def getPrecision(precision: String, typeOperation: Option[TypeOp]): BucketType =
    new BucketType(precision, typeOperation.getOrElse(defaultTypeOperation))

  def getResultType(typeOperation: String): Option[TypeOp] =
    if (!typeOperation.isEmpty) {
      properties.get(typeOperation) match {
        case Some(operation) => Some(getTypeOperationByName(operation.asInstanceOf[String]))
        case None => None
      }
    } else None

  //scalastyle:off
  def getTypeOperationByName(nameOperation: String): TypeOp =
    nameOperation.toLowerCase match {
      case name if name == "bigdecimal" => TypeOp.BigDecimal
      case name if name == "long" => TypeOp.Long
      case name if name == "int" => TypeOp.Int
      case name if name == "string" => TypeOp.String
      case name if name == "double" => TypeOp.Double
      case name if name == "boolean" => TypeOp.Boolean
      case name if name == "binary" => TypeOp.Binary
      case name if name == "date" => TypeOp.Date
      case name if name == "datetime" => TypeOp.DateTime
      case name if name == "timestamp" => TypeOp.Timestamp
      case name if name == "arraydouble" => TypeOp.ArrayDouble
      case name if name == "arraystring" => TypeOp.ArrayString
      case _ => defaultTypeOperation
    }

  //scalastyle:on
}

object Bucketer {

  final val IdentityName = "identity"
  final val IdentityFieldName = "identityField"
  final val TimestampName = "timestamp"

  def getIdentity(typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(IdentityName, typeOperation.getOrElse(default))

  def getIdentityField(typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(IdentityFieldName, typeOperation.getOrElse(default))

  def getTimestamp(typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(TimestampName, typeOperation.getOrElse(default))
}
