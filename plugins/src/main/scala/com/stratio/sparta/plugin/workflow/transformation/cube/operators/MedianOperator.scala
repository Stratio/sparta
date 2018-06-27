/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import breeze.linalg._
import breeze.stats._
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.Operator
import com.stratio.sparta.core.enumerators.WhenError.WhenError
import com.stratio.sparta.core.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.core.enumerators.WhenRowError.WhenRowError
import com.stratio.sparta.core.helpers.CastingHelper
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DataType, DoubleType}

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
          median(DenseVector(valuesFlattened.map(value =>
            CastingHelper.castingToSchemaType(defaultOutputType, value).asInstanceOf[Double]).toArray))
        else 0d
      }
    }
}
