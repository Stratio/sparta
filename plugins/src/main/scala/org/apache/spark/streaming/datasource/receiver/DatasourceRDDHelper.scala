/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.receiver

import org.apache.commons.lang.StringUtils
import org.apache.spark.streaming.datasource.models.OffsetConditions

import scala.util.Try

trait DatasourceRDDHelper {

  val InitTableName = "initTable"
  val TempInitQuery = s"select * from $InitTableName"
  private val complexOperands = Seq("UNION","JOIN","CLUSTER BY","INTERSECT", "HAVING")
  private val delimiters = Seq("LIMIT","OFFSET","ORDER","GROUP BY")
  
  def checkIfComplexQuery(inputQuery: String) : Boolean = {
    val upperCaseQuery = inputQuery.toUpperCase
    StringUtils.countMatches(upperCaseQuery, "SELECT") > 1 ||
      complexOperands.exists(operand => upperCaseQuery.contains(operand))
  }

  def retrieveWhereCondition(inputQuery : String): Option[String] = {
    if(inputQuery.toUpperCase.contains("WHERE")) {
      Try(inputQuery.split("(?i)WHERE").last
        .split(s"""(?<=.+)(?i)(${delimiters.mkString("|")})""").head).toOption
    } else None
  }

  def parseInitialQuery(complexQuery : Boolean, inputQuery : String): String =
    (complexQuery,
      Try(inputQuery.split(s"(?i)(WHERE|${delimiters.mkString("|")})").head).toOption) match {
      case (true, _) => TempInitQuery
      case (false, Some(query)) => query
      case _ => TempInitQuery
    }

  def possibleConflictsWRTColumns(initialWhereCondition : Option[String],
                                  offsetConditions: OffsetConditions): Boolean =
    initialWhereCondition match {
      case None => false
      case Some(cond) =>
        offsetConditions.fromOffset.map(_.name).intersect(cond.split("\\W+")).nonEmpty
    }
}
