/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import akka.event.slf4j.SLF4JLogging

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

object Utils extends SLF4JLogging {

  object implicits extends optionOps

  object optionImplicits extends optionOps

  sealed trait optionOps {
    implicit class OptionExt[T](option: Option[T]) {
      def getOrThrown(message: String): T = option.getOrElse(throw new RuntimeException(message))
    }
  }

  @annotation.tailrec
  def retry[T](attempts: Int, wait: Int)(fn: => T): T = {
    Try { fn } match {
      case Success(result) => result
      case Failure(e) if attempts > 1 && NonFatal(e) =>
        log.debug(s"Wait to next attempt in retry function, with exception ${e.getLocalizedMessage}")
        Thread.sleep(wait)
        retry(attempts - 1, wait)(fn)
      case Failure(e) => throw e
    }
  }

}
