/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
