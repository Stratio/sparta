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
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema

import scala.util.{Failure, Success, Try}

trait ErrorCheckingStepRow {

  self: GraphStep =>

  def returnSeqDataFromRow(newData: => Row): Seq[Row] = manageErrorWithTry(newData)

  def returnSeqDataFromRows(newData: => Seq[Row]): Seq[Row] = manageErrorWithTry(newData)

  private def manageErrorWithTry[T](newData: => T): Seq[Row] =
    Try(newData) match {
      case Success(data) => manageSuccess(data)
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard => Seq.empty[GenericRowWithSchema]
        case _ => throw e
      }
    }

  private def manageSuccess[T](newData: T): Seq[Row] =
    newData match {
      case data: Seq[GenericRowWithSchema] => data
      case data: GenericRowWithSchema => Seq(data)
      case _ => whenErrorDo match {
        case WhenError.Discard => Seq.empty[GenericRowWithSchema]
        case _ => throw new Exception("Invalid new data struct in step")
      }
    }
}
