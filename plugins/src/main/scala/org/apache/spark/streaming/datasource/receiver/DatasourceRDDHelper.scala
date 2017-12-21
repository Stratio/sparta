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
