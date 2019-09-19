/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import org.apache.commons.lang3.StringUtils

object RegexUtils {

  def replaceWholeWord(inputText: String, wordToReplace: String, replacement: String): String = {
    val patternWholeWord = s"\\b$wordToReplace\\b".r
    patternWholeWord.replaceAllIn(inputText, replacement)
  }

  def containsWholeWord(inputText: String, wordToFind: String) : Boolean = {
    val patternContainsWholeWord = s".*\\b$wordToFind\\b.*"
    inputText.matches(patternContainsWholeWord)
  }

  def sizeAndUnit(sizeString: String): (Int, String) = {
    val patternSizeUnit = "^(\\d*)\\s?([kmgtKMGT][Bb]*)$".r
     sizeString match {
       case patternSizeUnit(size, measureUnit) => (size.toInt, measureUnit)
       case _ => throw new RuntimeException(s"Cannot extract size and unit of measure from string $sizeString")
     }
  }

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

    def trimAndNormalizeString: String = inputString.trim.replaceAll("\\s+", " ")
  }

}
