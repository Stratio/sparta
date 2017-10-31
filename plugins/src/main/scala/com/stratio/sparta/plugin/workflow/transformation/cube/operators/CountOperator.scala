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
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.{Associative, Operator}
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DataType, LongType}

import scala.util.Try

class CountOperator(
                     name: String,
                     val whenErrorDo: WhenError,
                     inputField: Option[String] = None
                   ) extends Operator(name, whenErrorDo, inputField) with Associative {

  override val defaultOutputType: DataType = LongType

  override def processMap(inputFieldsValues: Row): Option[Any] =
    Option(1L)

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] =
    returnFromTryWithNullCheck("Error in CountOperator when reducing values") {
      Try(values.flatten.map(value => value.asInstanceOf[Long]).sum)
    }

  def associativity(values: Iterable[(String, Option[Any])]): Option[Any] =
    returnFromTryWithNullCheck("Error in CountOperator when associating values") {
      Try(extractValues(values, None).map(value => value.asInstanceOf[Long]).sum)
    }
}
