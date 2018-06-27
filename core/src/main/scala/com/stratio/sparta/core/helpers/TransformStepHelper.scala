/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.helpers

import com.stratio.sparta.core.enumerators.WhenRowError
import com.stratio.sparta.core.enumerators.WhenRowError.WhenRowError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

object TransformStepHelper {

  def sparkBatchDiscardFunction[T](inputRDD: RDD[Row], whenRowError: WhenRowError)
                                  (func: Row => T)
  : (RDD[Row], RDD[Row]) = {
    implicit val rowErrorDo: WhenRowError = whenRowError
    val rowTransformed = inputRDD.map { row =>
      returnDataWithDiscard(row, Try(func(row)))
    }
    val discardedData = rowTransformed.flatMap { case (discarded, _) => discarded }
    val validData = rowTransformed.flatMap { case (_, valid) => manageDataWithTry(valid) }

    (discardedData, validData)
  }

  def sparkStreamingDiscardFunction[T](inputDStream: DStream[Row], whenRowError: WhenRowError)
                                      (func: Row => T)
  : (DStream[Row], DStream[Row]) = {
    implicit val rowErrorDo: WhenRowError = whenRowError
    val rowTransformed = inputDStream.map { row =>
      returnDataWithDiscard(row, Try(func(row)))
    }
    val discardedData = rowTransformed.flatMap { case (discarded, _) => discarded }
    val validData = rowTransformed.flatMap { case (_, valid) => manageDataWithTry(valid) }

    (discardedData, validData)
  }


  def returnDataWithDiscard[T](inputRow: Row, newRow: Try[T])
                              (implicit whenRowErrorDo: WhenRowError): (Option[Row], Try[T]) = {
    newRow match {
      case Success(data) => (manageDiscardWithInput(inputRow, data), newRow)
      case Failure(_) => whenRowErrorDo match {
        case WhenRowError.RowDiscard => (Option(inputRow), newRow)
        case _ => (None, newRow)
      }
    }
  }

  /* PRIVATE METHODS */

  private def manageDiscardWithInput[T](inputRow: Row, newData: T): Option[Row] =
    newData match {
      case data: Seq[GenericRowWithSchema] => if (data.isEmpty) Option(inputRow) else None
      case data: Option[GenericRowWithSchema] => if (data.isEmpty) Option(inputRow) else None
      case _: GenericRowWithSchema => None
      case _ => None
    }

  private def manageDataWithTry[T](newData: Try[T])
                                  (implicit whenRowErrorDo: WhenRowError): Seq[Row] =
    newData match {
      case Success(data) => manageSuccess(data)
      case Failure(e) => whenRowErrorDo match {
        case WhenRowError.RowDiscard => Seq.empty[GenericRowWithSchema]
        case _ => throw e
      }
    }

  private def manageSuccess[T](newData: T)
                              (implicit whenRowErrorDo: WhenRowError): Seq[Row] =
    newData match {
      case data: Seq[GenericRowWithSchema] => data
      case data: GenericRowWithSchema => Seq(data)
      case data: Option[GenericRowWithSchema] =>
        data match {
          case Some(unwrappedData: GenericRowWithSchema) => Seq(unwrappedData)
          case None => Seq.empty[GenericRowWithSchema]
        }
      case _ => whenRowErrorDo match {
        case WhenRowError.RowDiscard => Seq.empty[GenericRowWithSchema]
        case _ => throw new Exception("Invalid new data struct in step")
      }
    }
}
