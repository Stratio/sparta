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
package com.stratio.sparta.sdk

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.TypeOp._

trait TypeConversions {

  final val TypeOperationName = "typeOp"

  def defaultTypeOperation: TypeOp

  def operationProps: Map[String, JSerializable] = Map()

  def getTypeOperation: Option[TypeOp] = getResultType(TypeOperationName)

  def getTypeOperation(typeOperation: String): Option[TypeOp] =
    if (!typeOperation.isEmpty && operationProps.contains(typeOperation)) getResultType(typeOperation)
    else getResultType(TypeOperationName)

  def getPrecision(precision: String, typeOperation: Option[TypeOp]): Precision =
    new Precision(precision, typeOperation.getOrElse(defaultTypeOperation))

  def getResultType(typeOperation: String): Option[TypeOp] =
    if (!typeOperation.isEmpty) {
      operationProps.get(typeOperation).flatMap(operation =>
        Option(operation).flatMap(op => Some(getTypeOperationByName(op.toString, defaultTypeOperation))))
    } else None
}
