/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators.WhenError
import com.stratio.sparta.core.enumerators.WhenError.WhenError
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait ErrorCheckingDStream extends SLF4JLogging {

  val whenErrorDo: WhenError

  val ssc: Option[StreamingContext]

  def returnDStreamFromTry[T: ClassTag](errorMessage: String, warningMessage: Option[String] = None)
                                       (actionFunction: => Try[DStream[T]]): DStream[T] =
    actionFunction match {
      case Success(value) => value
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard =>
          warningMessage.foreach(message => log.warn(s"$message. ${e.getLocalizedMessage}"))
          ssc.get.queueStream[T](new mutable.Queue[RDD[T]])
        case _ => throw new Exception(errorMessage, e)
      }
    }
}
