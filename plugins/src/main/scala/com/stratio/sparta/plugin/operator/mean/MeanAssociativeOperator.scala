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
package com.stratio.sparta.plugin.operator.mean

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.TypeOp
import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.{TypeOp, _}
import org.apache.spark.sql.types.StructType

import scala.util.Try

class MeanAssociativeOperator(name: String, schema: StructType, properties: Map[String, JSerializable])
  extends Operator(name, schema, properties) with OperatorProcessMapAsNumber with Associative {

  private val SumKey = "sum"
  private val MeanKey = "mean"
  private val CountKey = "count"

  val inputSchema = schema

  override val defaultTypeOperation = TypeOp.MapStringDouble

  override val defaultCastingFilterType = TypeOp.Number

  override def processReduce(values: Iterable[Option[Any]]): Option[Seq[Double]] = {
    Try(Option(getDistinctValues(values.flatten.flatMap(value => {
      value match {
        case value if value.isInstanceOf[Seq[Double]] => value.asInstanceOf[Seq[Double]]
        case _ => List(TypeOp.transformValueByTypeOp(TypeOp.Double, value).asInstanceOf[Double])
      }
    })))).getOrElse(Some(Seq.empty[Double]))
  }

  def associativity(values: Iterable[(String, Option[Any])]): Option[Map[String, Any]] = {
    val oldValues = extractValues(values, Option(Operator.OldValuesKey))
      .map(_.asInstanceOf[Map[String, Double]]).headOption
    val newValues =  extractValues(values, Option(Operator.NewValuesKey)).flatMap(value => {
      value match {
        case value if value.isInstanceOf[Seq[Double]] => value.asInstanceOf[Seq[Double]]
        case _ => List(TypeOp.transformValueByTypeOp(TypeOp.Double, value).asInstanceOf[Double])
      }
    }).toList

    val returnValues = if(newValues.nonEmpty) {
      val oldCount = oldValues.fold(0d) { case oldV => oldV.getOrElse(CountKey, 0d)}
      val oldSum = oldValues.fold(0d) { case oldV => oldV.getOrElse(SumKey, 0d)}
      val calculatedSum = oldSum + newValues.sum
      val calculatedCount = oldCount + newValues.size.toDouble
      val calculatedMean = if (calculatedCount != 0d) calculatedSum / calculatedCount else 0d

      Map(SumKey -> calculatedSum, CountKey -> calculatedCount, MeanKey -> calculatedMean)
    } else oldValues.getOrElse(Map(SumKey -> 0d, CountKey -> 0d, MeanKey -> 0d))

    Try(Option(transformValueByTypeOp(returnType, returnValues))).getOrElse(Option(Map.empty[String, Double]))
  }

}
