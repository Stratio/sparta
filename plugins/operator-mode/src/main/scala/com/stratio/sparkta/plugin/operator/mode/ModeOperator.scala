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

package com.stratio.sparkta.plugin.operator.mode

import java.io.{Serializable => JSerializable}
import com.stratio.sparkta.sdk.TypeOp
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._

import scala.util.Try

class ModeOperator(name: String, properties: Map[String, JSerializable]) extends Operator(name, properties) {

  override val defaultTypeOperation = TypeOp.ArrayString

  private val inputField = if(properties.contains("inputField")) Some(properties.getString("inputField")) else None

  override val writeOperation = WriteOp.Mode

  override def processMap(inputFields: Map[String, JSerializable]): Option[Any] = {
    if (inputField.isDefined && inputFields.contains(inputField.get)) {
      applyFilters(inputFields).flatMap(filteredFields => Some(filteredFields.get(inputField.get).get))
    } else None
  }


  override def processReduce(values: Iterable[Option[Any]]): Option[Any] = {

    val tupla = values.groupBy(x => x).mapValues(_.size)
    if (!tupla.isEmpty) {
      val max = tupla.map(tuple => tuple._2).max

      Try(Some(transformValueByTypeOp(returnType, tupla.filter(_._2 == max).flatMap(tuple => (tuple._1)))))
        .getOrElse(ModeOperator.SOME_EMPTY)
    } else ModeOperator.SOME_EMPTY
  }

}

private object ModeOperator {
  val SOME_EMPTY = Some(List())
}
