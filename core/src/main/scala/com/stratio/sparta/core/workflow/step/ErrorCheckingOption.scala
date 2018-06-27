/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import com.stratio.sparta.core.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.core.enumerators.WhenRowError.WhenRowError
import com.stratio.sparta.core.enumerators.{WhenFieldError, WhenRowError}

import scala.util.{Failure, Success, Try}

trait ErrorCheckingOption {

  val whenFieldErrorDo: WhenFieldError
  val whenRowErrorDo: WhenRowError

  //scalastyle:off
  def returnFromTryWithNullCheck[T](errorMessage: String)(actionFunction: => Try[T]): Option[Any] =
    actionFunction match {
      case Success(value) => Option(value)
      case Failure(e) => whenFieldErrorDo match {
        case WhenFieldError.FieldDiscard => None
        case WhenFieldError.Null => Some(null)
        case _ => throw new Exception(errorMessage, e)
      }
    }

  //scalastyle:on

  def returnRowFromTry[T](errorMessage: String)(actionFunction: => Try[T]): Option[T] =
    actionFunction match {
      case Success(value) => Option(value)
      case Failure(e) => whenRowErrorDo match {
        case WhenRowError.RowDiscard => None
        case _ => throw new Exception(errorMessage, e)
      }
    }

  def returnFieldFromTry[T](errorMessage: String)(actionFunction: => Try[T]): Option[T] =
    actionFunction match {
      case Success(value) => Option(value)
      case Failure(e) => whenFieldErrorDo match {
        case WhenFieldError.FieldDiscard => None
        case _ => throw new Exception(errorMessage, e)
      }
    }
}
