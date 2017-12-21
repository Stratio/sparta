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
