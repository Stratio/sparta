/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.{Associative, Operator}
import com.stratio.sparta.core.enumerators.WhenError.WhenError
import com.stratio.sparta.core.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.core.enumerators.WhenRowError.WhenRowError
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
