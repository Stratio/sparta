/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import java.util.Date

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.{Associative, Operator}
import com.stratio.sparta.sdk.utils.CastingUtils
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import scala.util.Try

class MinOperator(
                   name: String,
                   val whenRowErrorDo: WhenRowError,
                   val whenFieldErrorDo: WhenFieldError,
                   inputField: Option[String] = None
                 ) extends Operator(name, whenRowErrorDo, whenFieldErrorDo, inputField) with Associative {

  assert(inputField.isDefined)

  override val defaultOutputType: DataType = StringType

  override def processMap(inputRow: Row): Option[Any] = processMapFromInputField(inputRow)

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] = {
    val flattenedValues = values.flatten
    if (flattenedValues.nonEmpty) {
      returnFromTryWithNullCheck("Error in MinOperator when reducing values") {
        Try {
          minCheckingType(flattenedValues)
        }
      }
    } else None
  }

  def associativity(values: Iterable[(String, Option[Any])]): Option[Any] =
    returnFromTryWithNullCheck("Error in MinOperator when associating values") {
      Try {
        minCheckingType(extractValues(values, None))
      }
    }

  private[operators] def minCheckingType(values: Iterable[Any]): Any =
    values.head match {
      case _: Double =>
        values.map(CastingUtils.castingToSchemaType(DoubleType, _).asInstanceOf[Double]).min
      case _: Float =>
        values.map(CastingUtils.castingToSchemaType(FloatType, _).asInstanceOf[Float]).min
      case _: Int =>
        values.map(CastingUtils.castingToSchemaType(IntegerType, _).asInstanceOf[Int]).min
      case _: Short =>
        values.map(CastingUtils.castingToSchemaType(ShortType, _).asInstanceOf[Short]).min
      case _: Long =>
        values.map(CastingUtils.castingToSchemaType(LongType, _).asInstanceOf[Long]).min
      case _: String =>
        values.map(CastingUtils.castingToSchemaType(StringType, _).asInstanceOf[String]).min
      case _: java.sql.Timestamp =>
        values.map(CastingUtils.castingToSchemaType(LongType, _).asInstanceOf[Long]).min
      case _: Date =>
        values.map(CastingUtils.castingToSchemaType(DateType, _).asInstanceOf[Date]).min
      case _ =>
        throw new Exception(s"Unsupported type in MinOperator")
    }
}
