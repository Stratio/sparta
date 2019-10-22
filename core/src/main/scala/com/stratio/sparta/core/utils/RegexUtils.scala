/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.models.{MetadataPath, ResourcePlannedQuery}
import org.apache.commons.lang3.StringUtils

import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object RegexUtils {

  val wrappedVariable: Regex = "\\$\\{[0-9]+\\}".r
  val nameVariable: Regex = "(?<=\\$\\{)[0-9]+(?=\\})".r

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

  def resourceUniqueTableName(res: ResourcePlannedQuery) : String =
    res.typeResource match {
      case QualityRuleResourceTypeEnum.XD =>
        val database = MetadataPath.fromMetadataPathString(res.metadataPath).path.fold(""){ db =>
          s"${db.stripPrefixWithIgnoreCase("/")}."}
        s"$database${res.resource}"
      case QualityRuleResourceTypeEnum.JDBC => //Parse out the eventual schema and take only the name
        val resourceWithoutSchema =  res.resource.split("\\.").last
        s"${resourceWithoutSchema}_${res.id}"
      case _ =>
        s"${res.resource}_${res.id}"
      }

  def replacePlaceholdersWithResources(query: String, resourcesPlannedQuery: Seq[ResourcePlannedQuery]): String = {

    def replaceVariableWithResourceName: Match => Option[String] =
      (m: Match) =>
        {
          Try {
            val idVariable = nameVariable.findFirstIn(m.group(0)).get
            val resource = resourcesPlannedQuery.find(_.id.toString == idVariable)
            resourceUniqueTableName(resource.get)
          }.toOption
        }
     wrappedVariable.replaceSomeIn(query, replaceVariableWithResourceName)
  }

  def valuePlaceholder(text: String): String = {
    nameVariable.findFirstIn(text).getOrElse(text)
  }

  def containsPlaceholders(text: String): Boolean =
    wrappedVariable.findAllIn(text).nonEmpty

  def findResourcesUsed(query: String, resourcesPlannedQuery: Seq[ResourcePlannedQuery]): Seq[ResourcePlannedQuery] = {
    Try {
      val seqMatches = wrappedVariable.findAllMatchIn(query)
      seqMatches.flatMap(m => {
        val idVariable = nameVariable.findFirstIn(m.toString()).get
        resourcesPlannedQuery.find(_.id.toString == idVariable)
      }
      ).toList.toArray.distinct.toSeq //It is an iterator, therefore it would return a Stream
    }.toOption.getOrElse(Seq.empty[ResourcePlannedQuery])
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

    def removeAllNewlines: String = inputString.replaceAll("[\n\r]", "")

    def cleanOutControlChar: String = {
      val extendedCode : Char => Boolean = (c:Char) => c < 32 || c >= 127
      inputString.map(x => if(extendedCode(x)) " " else x ).mkString
    }
  }

}
