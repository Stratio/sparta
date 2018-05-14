/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.utils

import org.scalactic._

object TestUtils {

  //Set up some common string normalizers

  val whiteSpaceNormalised: Uniformity[String] =
    new AbstractStringUniformity {
      /**Returns the string with all consecutive white spaces reduced to a single space.*/
      def normalized(s: String): String = s.replaceAll("\\s+", " ")
      override def toString: String = "whiteSpaceNormalised"
    }

  val whiteSpaceNormalisedAndTrimmed: Uniformity[String] =
    new AbstractStringUniformity {
      /**Returns the string with all consecutive white spaces reduced to a single space.*/
      def normalized(s: String): String = s.trim.replaceAll("\\s+", " ")
      override def toString: String = "whiteSpaceNormalisedAndTrimmed"
    }

  val whiteSpaceAndNewlinesNormalised: Uniformity[String] =
    new AbstractStringUniformity {
      /**Returns the string with all consecutive white spaces reduced to a single space.*/
      def normalized(s: String): String = s.replaceAll("\\s+", " ").replaceAll("\\n+", "\n")
      override def toString: String = "whiteSpaceNewlinesNormalised"
    }
}
