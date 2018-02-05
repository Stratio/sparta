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
import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import scala.util.Try

class WordCountOperator(
                         name: String,
                         val whenRowErrorDo: WhenRowError,
                         val whenFieldErrorDo: WhenFieldError,
                         inputField: Option[String] = None
                       ) extends Operator(name, whenRowErrorDo, whenFieldErrorDo, inputField) with Associative {

  override val defaultOutputType: DataType = MapType(StringType, LongType)

  override def processMap(inputFieldsValues: Row): Option[Any] =
    inputField.flatMap { field =>
      returnFieldFromTry(s"Error in process map with operator: $name, inputRow: $inputFieldsValues and field: $field") {
        Try {
          inputFieldsValues.get(inputFieldsValues.schema.fieldIndex(field)).toString.split("[\\p{Punct}\\s]+").toSeq
        }
      }
    }

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] =
    returnFromTryWithNullCheck("Error in WordCountOperator when reducing values") {
      Try {
        values.flatten.flatMap { value =>
          value match {
            case value: Seq[_] => value.map(_.toString)
            case value: String => Seq(value)
            case _ => throw new Exception(s"Unsupported value: $value")
          }
        }.toSeq
      }
    }

  def associativity(values: Iterable[(String, Option[Any])]): Option[Any] = {
    returnFromTryWithNullCheck("Error in WordCountOperator when associating values") {
      Try {
        val oldValues = extractValues(values, Option(Operator.OldValuesKey))
          .flatMap(_.asInstanceOf[Map[String, Long]]).toSeq
        val newValuesList = extractValues(values, Option(Operator.NewValuesKey)).toSeq
          .flatMap { value =>
            value match {
              case value: Seq[_] => value.map(_.toString)
              case value: String => Seq(value)
              case _ => throw new Exception(s"Unsupported value: $value")
            }
          }
        val newValues = applyCount(newValuesList).toSeq

        applyCountMerge(oldValues ++ newValues)
      }
    }
  }

  private[operators] def applyCount(values: Seq[String]): Map[String, Long] =
    values.groupBy((word: String) => word).map { case (key, value) => (key, value.length.toLong) }

  private[operators] def applyCountMerge(values: Seq[(String, Long)]): Map[String, Long] =
    values.groupBy { case (word, _) => word }.map { case (key, counts) =>
      (key, counts.map { case (_, value) => value }.sum)
    }
}
