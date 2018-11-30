/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import breeze.linalg._
import breeze.stats._
import com.stratio.sparta.core.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.core.enumerators.WhenRowError.WhenRowError
import com.stratio.sparta.core.helpers.CastingHelper
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.Operator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import scala.util.Try

class MedianOperator(
                      name: String,
                      val whenRowErrorDo: WhenRowError,
                      val whenFieldErrorDo: WhenFieldError,
                      inputField: Option[String] = None
                    ) extends Operator(name, whenRowErrorDo, whenFieldErrorDo, inputField) {

  assert(inputField.isDefined)

  override val defaultOutputType: DataType = DoubleType

  override def processMap(inputRow: Row): Option[Any] = processMapFromInputField(inputRow)

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] =
    returnFromTryWithNullCheck("Error in MedianOperator when reducing values") {
      Try {
        val valuesFlattened = values.flatten
        if (valuesFlattened.nonEmpty)
          checkingType(valuesFlattened)
        else 0d
      }
    }

  def checkingType(values: Iterable[Any]): Any =
    values.head match {
      case _: Double =>
        median(DenseVector(values.map(CastingHelper.castingToSchemaType(DoubleType, _).asInstanceOf[Double]).toArray))
      case _: Float =>
        median(DenseVector(values.map(CastingHelper.castingToSchemaType(FloatType, _).asInstanceOf[Float]).toArray))
      case _: Int =>
        median(DenseVector(values.map(CastingHelper.castingToSchemaType(IntegerType, _).asInstanceOf[Int]).toArray))
      case _: Long =>
        median(DenseVector(values.map(CastingHelper.castingToSchemaType(LongType, _).asInstanceOf[Long]).toArray))
      case _: java.sql.Timestamp =>
        median(DenseVector(values.map(CastingHelper.castingToSchemaType(LongType, _).asInstanceOf[Long]).toArray))
      case _ =>
        throw new Exception(s"Unsupported type in MedianOperator $name")
    }
}
