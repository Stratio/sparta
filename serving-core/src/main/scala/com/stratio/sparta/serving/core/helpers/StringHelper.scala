/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import org.apache.commons.lang3.StringUtils

object StringHelper {

  implicit class StringHelperSparta(inputString: String) {

    def endsWithIgnoreCase(suffix: String): Boolean =
      inputString.regionMatches(true,
        inputString.length - suffix.length,
        suffix,
        0,
        suffix.length)

    def startWithIgnoreCase(prefix:String) : Boolean =
      inputString.regionMatches(true, 0, prefix, 0, prefix.length)

    def stripPrefixWithIgnoreCase(prefix:String) : String =
      if(inputString.startWithIgnoreCase(prefix)) inputString.substring(prefix.length) else inputString

    def containsIgnoreCase(regex: String): Boolean =
      StringUtils.containsIgnoreCase(inputString, regex)
  }

}