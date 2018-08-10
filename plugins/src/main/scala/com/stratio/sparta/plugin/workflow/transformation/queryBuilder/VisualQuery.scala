/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.queryBuilder

import com.stratio.sparta.plugin.enumerations.JoinTypes
import com.stratio.sparta.plugin.enumerations.JoinTypes.JoinTypes
import com.stratio.sparta.plugin.enumerations.OrderByType.OrderByType
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.plugin.models.JoinCondition


case class VisualQuery(
                        selectClauses: Seq[SelectClause],
                        fromClause: Option[TableExpression],
                        joinClause: Option[JoinClause],
                        whereClause: Option[String],
                        orderByClauses: Seq[OrderByClause]
                      ) {

  def toSql: String = {
    val selectSentence = {
      val selectSql = {
        val selectFiltered = selectClauses.filter(_.expression.nonEmpty)
        if (selectFiltered.nonEmpty)
          s"${selectFiltered.map(_.toSql).filter(_.nonEmpty).mkString(",")}"
        else "*"
      }

      s"SELECT $selectSql"
    }
    val fromSentence = fromClause.map(from => s"FROM ${from.toSql}")
    val joinSentence = joinClause.map(_.toSql)
    val joinWhereConditions: Option[String] = joinClause.flatMap { join =>
      join.joinTypes match {
        case JoinTypes.LEFT_RIGHT_ONLY =>
          Option(join.joinConditions.map { condition =>
            s"${join.leftTable.alias.notBlank.getOrElse(join.leftTable.tableName)}.${condition.leftField} IS NULL OR " +
              s"${join.rightTable.alias.notBlank.getOrElse(join.rightTable.tableName)}.${condition.rightField} IS NULL"
          }.mkString(" AND "))
        case _ => None
      }
    }.notBlank
    val whereSentence = whereClause.notBlank.map { where =>
      s"WHERE $where ${joinWhereConditions.notBlank.fold("") { conditions => s"AND $conditions" }}"
    }.getOrElse(joinWhereConditions.notBlank.fold("") { conditions => s"WHERE $conditions" })
    val orderBySentence = {
      val clausesFiltered = orderByClauses.filter(_.field.nonEmpty)
      if (clausesFiltered.nonEmpty)
        s"ORDER BY ${clausesFiltered.sortBy(_.position.getOrElse(0)).map(_.toSql).mkString(",")}"
      else ""
    }

    s"$selectSentence ${fromSentence.orElse(joinSentence).getOrElse("")} $whereSentence $orderBySentence"
  }
}


case class SelectClause(expression: String, alias: Option[String] = None) {

  def toSql: String = {
    if(expression.nonEmpty)
      s"$expression ${alias.fold("") { as => s"AS $as" }}"
    else ""
  }

}

case class TableExpression(tableName: String, alias: Option[String] = None) {

  def toSql: String = s"$tableName ${alias.notBlank.fold("") { as => s"AS $as" }}"

}

case class JoinClause(
                       leftTable: TableExpression,
                       rightTable: TableExpression,
                       joinTypes: JoinTypes,
                       joinConditions: Seq[JoinCondition]
                     ) {

  def toSql: String = {
    val conditions: String = joinTypes match {
      case JoinTypes.CROSS => ""
      case _ =>
        if (joinConditions.nonEmpty)
          s" ON ${
            joinConditions.map(_.toSql(
              leftTable.alias.notBlank.getOrElse(leftTable.tableName),
              rightTable.alias.notBlank.getOrElse(rightTable.tableName)
            )).mkString(" AND ")
          }"
        else ""
    }
    val leftTableSql: String = joinTypes match {
      case JoinTypes.RIGHT_ONLY => rightTable.toSql
      case _ => leftTable.toSql
    }
    val rightTableSql: String = joinTypes match {
      case JoinTypes.RIGHT_ONLY => leftTable.toSql
      case _ => rightTable.toSql
    }

    s"FROM $leftTableSql ${JoinTypes.joinTypeToSql(joinTypes)} JOIN $rightTableSql $conditions"
  }

}

case class OrderByClause(field: String, order: OrderByType, position: Option[Int] = None) {

  def toSql: String = s"$field ${order.toString}"

}

