/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

object Utils {

  object implicits extends optionOps

  object optionImplicits extends optionOps

  sealed trait optionOps {
    implicit class OptionExt[T](option: Option[T]) {
      def getOrThrown(message: String): T = option.getOrElse(throw new RuntimeException(message))
    }
  }

}
