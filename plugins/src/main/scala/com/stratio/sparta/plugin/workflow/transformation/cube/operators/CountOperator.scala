/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.{Associative, Operator}
import com.stratio.sparta.sdk.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.enumerators.WhenRowError.WhenRowError
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DataType, LongType}

import scala.util.Try

class CountOperator(
                     name: String,
                     val whenRowErrorDo: WhenRowError,
                     val whenFieldErrorDo: WhenFieldError,
                     inputField: Option[String] = None
                   ) extends Operator(name, whenRowErrorDo, whenFieldErrorDo, inputField) with Associative {

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
