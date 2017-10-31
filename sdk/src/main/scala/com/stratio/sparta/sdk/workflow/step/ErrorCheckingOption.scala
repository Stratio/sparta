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

import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError

import scala.util.{Failure, Success, Try}

trait ErrorCheckingOption {

  val whenErrorDo: WhenError

  //scalastyle:off
  def returnFromTryWithNullCheck[T](errorMessage: String)(actionFunction: => Try[T]): Option[Any] =
    actionFunction match {
      case Success(value) => Option(value)
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard => None
        case WhenError.Null => Some(null)
        case _ => throw new Exception(errorMessage, e)
      }
    }

  //scalastyle:on

  def returnFromTry[T](errorMessage: String)(actionFunction: => Try[T]): Option[T] =
    actionFunction match {
      case Success(value) => Option(value)
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard => None
        case _ => throw new Exception(errorMessage, e)
      }
    }
}
