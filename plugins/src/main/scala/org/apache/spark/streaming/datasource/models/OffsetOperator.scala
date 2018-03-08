/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.models

object OffsetOperator extends Enumeration {

  type OrderRelation = Value

  val < = Value("<")
  val > = Value(">")
  val <= = Value("<=")
  val >= = Value(">=")
  val incrementalOperator = Value("incremental")
  val decrementalOperator = Value("decremental")


  def toInverseOrderOperator(orderRelation: OrderRelation): OrderOperator.OrderRelation = orderRelation match {
    case `<` | `<=` => OrderOperator.ASC
    case `>` | `>=` => OrderOperator.DESC
    case `incrementalOperator` => OrderOperator.DESC
    case `decrementalOperator` => OrderOperator.ASC
  }

  def toOrderOperator(orderRelation: OrderRelation): OrderOperator.OrderRelation = orderRelation match {
    case `<` | `<=` => OrderOperator.DESC
    case `>` | `>=` => OrderOperator.ASC
    case `incrementalOperator` => OrderOperator.ASC
    case `decrementalOperator` => OrderOperator.DESC
  }

  def toProgressOperator(orderRelation: OrderRelation): OffsetOperator.OrderRelation = orderRelation match {
    case `<` | `<=` => <
    case `>` | `>=` => >
    case `incrementalOperator` => >
    case `decrementalOperator` => <
  }

  def toMultiProgressOperator(orderRelation: OrderRelation): OffsetOperator.OrderRelation = orderRelation match {
    case `<` | `<=` | `decrementalOperator` => decrementalOperator
    case `>` | `>=` | `incrementalOperator` => incrementalOperator
  }
}
