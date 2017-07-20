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
package org.apache.spark.streaming.datasource.models

import org.apache.spark.streaming.datasource.models.OffsetOperator._

case class OffsetField(
                        name: String,
                        operator: OrderRelation = >=,
                        value: Option[Any] = None
                      ) {

  override def toString: String =
    s"$name,$value,${operator.toString}"

  def toStringPretty: String =
    s"name: $name value: $value , operator: ${operator.toString}"

  def extractConditionSentence(sqlQuery: String): String = {
    value.fold("") { case valueExtracted =>
      if(valueExtracted.toString.nonEmpty)
        s" WHERE $name ${operator.toString} ${valueToSqlSentence(valueExtracted)} "
      else ""
    }
  }

  def extractOrderSentence(sqlQuery: String, inverse: Boolean = true): String =
    s" ORDER BY $name ${
      if (inverse) OffsetOperator.toInverseOrderOperator(operator).toString
      else OffsetOperator.toOrderOperator(operator)
    }"

  private def valueToSqlSentence(value: Any): String =
    value match {
      case valueString : String => s"'$valueString'"
      case valueDate : java.sql.Timestamp => s"'${valueDate.toString}'"
      case _ => value.toString
    }
}

object OffsetField {

  /**
   * Construct objects that contains the field options for monitoring tables in datasources
   *
   * @param name     Field name of the table
   * @param operator Operator that compare the table data with the last offset field value
   * @param value    Value for the offset field
   * @return
   */
  def apply(
             name: String,
             operator: OrderRelation,
             value: Any
           ): OffsetField = new OffsetField(name, operator, Option(value))

  /**
   * Construct objects that contains the field options for monitoring tables in datasources
   *
   * @param name  Field name of the table
   * @param value Value for the offset field, initially can be empty
   * @return
   */
  def apply(
             name: String,
             value: Any
           ): OffsetField = new OffsetField(name, >=, Option(value))

  /**
    * Construct objects that contains the field options for monitoring tables in datasources
    *
    * @param name  Field name of the table
    * @return
    */
  def apply(
             name: String
           ): OffsetField = new OffsetField(name, >=, None)

  /**
    * Construct objects that contains the field options for monitoring tables in datasources
    *
    * @param name  Field name of the table
    * @param operator Operator optional that compare the table data with the last offset field value
    * @param value    Value optional for the offset field
    * @return
    */
  def apply(
             name: String,
             operator: Option[OrderRelation],
             value: Option[Any]
           ): OffsetField = new OffsetField(name, operator.getOrElse(>=), value)
}
