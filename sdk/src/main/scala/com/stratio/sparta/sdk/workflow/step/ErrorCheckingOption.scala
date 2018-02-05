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
package com.stratio.sparta.sdk.workflow.step

import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError
import com.stratio.sparta.sdk.workflow.enumerators.{WhenFieldError, WhenRowError}

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
