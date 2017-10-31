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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait ErrorCheckingDStream extends SLF4JLogging {

  val whenErrorDo: WhenError

  val ssc: StreamingContext

  def returnDStreamFromTry[T: ClassTag](errorMessage: String, warningMessage: Option[String] = None)
                                       (actionFunction: => Try[DStream[T]]): DStream[T] =
    actionFunction match {
      case Success(value) => value
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard =>
          warningMessage.foreach(log.warn(_, e))
          ssc.queueStream[T](new mutable.Queue[RDD[T]])
        case _ => throw new Exception(errorMessage, e)
      }
    }
}
