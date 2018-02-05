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

import breeze.stats._
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.Operator
import com.stratio.sparta.sdk.utils.CastingUtils
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DataType, DoubleType}

import scala.util.Try

class VarianceOperator(
                        name: String,
                        val whenRowErrorDo: WhenRowError,
                        val whenFieldErrorDo: WhenFieldError,
                        inputField: Option[String] = None
                      ) extends Operator(name, whenRowErrorDo, whenFieldErrorDo, inputField) {

  assert(inputField.isDefined)

  override val defaultOutputType: DataType = DoubleType

  override def processMap(inputRow: Row): Option[Any] = processMapFromInputField(inputRow)

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] =
    returnFromTryWithNullCheck("Error in VarianceOperator when reducing values") {
      Try {
        val valuesFlattened = values.flatten
        if (valuesFlattened.nonEmpty)
          variance(valuesFlattened.map(value =>
            CastingUtils.castingToSchemaType(defaultOutputType, value).asInstanceOf[Double]))
        else 0d
      }
    }
}
