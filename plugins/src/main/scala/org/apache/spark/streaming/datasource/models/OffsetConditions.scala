/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.models

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._

import scala.annotation.tailrec

case class OffsetConditions(fromOffset: Seq[OffsetField],
                            limitRecords: Option[Long]) {

  override def toString: String =
    s"[FromOffsets: {\n ${fromOffset.map(_.toStringPretty).mkString(";\n")}}" +
      s"LimitRecords: ${limitRecords.getOrElse("")}]"

  def extractConditionSentence(previousWhereCondition: Option[String]): String = {
    val changeToComplexQuery: Boolean = fromOffset.forall( item =>
      item.operator == OffsetOperator.incrementalOperator || item.operator == OffsetOperator.decrementalOperator)
    val stringConditions = if(changeToComplexQuery) createDisjointCondition else createJoinCondition
    Option(stringConditions).notBlank.fold(previousWhereCondition.getOrElse("")){conditions =>
      s" WHERE $conditions ${previousWhereCondition.fold(""){ prevCond => " AND " + prevCond}}"}
  }

  def createJoinCondition: String = {
    fromOffset.map( condition =>
      if(condition.value.isDefined)
        s" ${condition.name} ${condition.operator.toString} ${valueToSqlSentence(condition.value.get)} "
      else "" ).filter(_.nonEmpty).mkString(" AND ")
  }

  def createDisjointCondition: String = {
    val reversedConditions = fromOffset.reverse
    @tailrec
    def createDisjointCondition(conditions: Seq[OffsetField], buildingString: String): String =
      conditions match {
        case condition :: Nil =>
          buildingString + s" ${condition.name} ${OffsetOperator.toProgressOperator(condition.operator).toString}" +
            s"${valueToSqlSentence(condition.value.get)} "
        case condition :: tail => {
          val stringCurrent =
            " ( " + tail.map(currentCondition =>
              s" ${currentCondition.name} = " +
                s"${valueToSqlSentence(currentCondition.value.get)} ").mkString(" AND ") +
              s" AND ${condition.name} ${OffsetOperator.toProgressOperator(condition.operator).toString}" +
              s"${valueToSqlSentence(condition.value.get)} ) " + " OR "
          createDisjointCondition(tail, buildingString + stringCurrent)
        }
      }
    createDisjointCondition(reversedConditions, "")
  }

  def extractOrderSentence(sqlQuery: String, inverse: Boolean = true): String = {
    val stringsOrdering: String = fromOffset.map(condition => s"${condition.name} ${
      if (inverse) OffsetOperator.toInverseOrderOperator(condition.operator).toString
      else OffsetOperator.toOrderOperator(condition.operator)} ").mkString(",")
    Option(stringsOrdering).notBlank.fold(""){ordering => s" ORDER BY $ordering"}
  }

  def valueToSqlSentence(value: Any): String =
    value match {
      case valueString : String => s"'$valueString'"
      case valueDate : java.sql.Timestamp => s"'${valueDate.toString}'"
      case _ => value.toString
    }
}

object OffsetConditions {

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals. The user can limit the results on each query if the number of records are very big
    *
    * @param fromOffset   Field objects containing the fields' name, value and operation (> or <)
    * @param limitRecords Limit the number of records returned on each query
    * @return The offset conditions object
    */
  def apply(
             fromOffset: Seq[OffsetField],
             limitRecords: Long
           ): OffsetConditions = new OffsetConditions(fromOffset, Option(limitRecords))

  /**
    * Constructor Offset conditions for monitoring tables, one field is necessary, the initial value and the operator
    * can be optionals.
    *
    * @param fromOffset Field objects containing the fields' name, value and operation (> or <)
    * @return The offset conditions object
    */
  def apply(fromOffset: Seq[OffsetField]): OffsetConditions = new OffsetConditions(fromOffset, None)
}
