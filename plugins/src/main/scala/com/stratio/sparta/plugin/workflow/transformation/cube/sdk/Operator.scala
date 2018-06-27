/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.sdk

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators.WhenError.WhenError
import com.stratio.sparta.core.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.core.enumerators.WhenRowError.WhenRowError
import com.stratio.sparta.core.workflow.step.ErrorCheckingOption
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.DataType

import scala.util.Try

abstract class Operator(
                         val name: String,
                         whenRowErrorDo: WhenRowError,
                         whenFieldErrorDo: WhenFieldError,
                         inputField: Option[String]
                       ) extends SLF4JLogging with Serializable with ErrorCheckingOption {

  val defaultOutputType : DataType

  def processMap(inputRow: Row): Option[Any]

  def processReduce(values: Iterable[Option[Any]]): Option[Any]

  val isAssociative: Boolean = false // `Associative` trait will override it to `true`

  def processMapFromInputField(inputRow: Row): Option[Any] =
    inputField.flatMap { field =>
      returnFieldFromTry(s"Error in process map with operator: $name, inputRow: $inputRow and field: $field") {
        Try(inputRow.get(inputRow.schema.fieldIndex(field)))
      }
    }

  def extractValues(values: Iterable[(String, Option[Any])], filterKey: Option[String]): Iterable[Any] =
    values.flatMap { case (key, value) =>
      filterKey match {
        case Some(filter) => if (key == filter) value else None
        case None => value
      }
    }
}

object Operator {

  final val ClassSuffix = "Operator"
  final val OldValuesKey = "old"
  final val NewValuesKey = "new"

}