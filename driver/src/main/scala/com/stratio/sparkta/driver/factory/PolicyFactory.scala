/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.factory

import scala.util.Try

import org.apache.spark.sql.types._

import com.stratio.sparkta.aggregator.Rollup
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

object PolicyFactory {

  //scalastyle:off
  def rowTypeFromOption(optionType: TypeOp): DataType =
    optionType match {
      case TypeOp.Long => LongType
      case TypeOp.Double => DoubleType
      case TypeOp.BigDecimal => DecimalType(None)
      case TypeOp.Int => IntegerType
      case TypeOp.Boolean => BooleanType
      case TypeOp.Date => DateType
      case TypeOp.DateTime => TimestampType
      case TypeOp.ArrayDouble => ArrayType(DoubleType)
      case TypeOp.ArrayString => ArrayType(StringType)
      case TypeOp.String => StringType
      case _ => BinaryType
    }
  //scalastyle:on

  def rollupsOperatorsSchemas(rollups: Seq[Rollup],
                              configOptions: Seq[(String, Map[String, String])]): Seq[TableSchema] = {
    val componentsSorted = rollups.map(rollup =>
      (rollup.getComponentsSorted, rollup.operators))
    configOptions.flatMap{ case (outputName, configOptions) => {
      for {
        (rollupsCombinations, operators) <- getCombinationsWithOperators(configOptions, componentsSorted)
        rollupsNames = rollupsCombinations.map(_.getNameDimension)
        schema = StructType(getDimensionsFields(rollupsCombinations) ++
          (getOperatorsFields(operators) ++
          getFixedFieldAggregation(configOptions)).sortWith(_.name < _.name))
      } yield TableSchema(outputName, rollupsNames.mkString(Output.Separator), schema)
    }}.distinct
  }

  private def getCombinationsWithOperators(configOptions: Map[String, String],
                                           componentsSorted: Seq[(Seq[DimensionBucket], Seq[Operator])])
  : Seq[(Seq[DimensionBucket], Seq[Operator])] =
    if (Try(configOptions.get(Output.Multiplexer).get.toBoolean).getOrElse(false)) {
      componentsSorted.flatMap{ case (compSorted, operators) =>
        Multiplexer.combine(compSorted, operators)
      }.distinct
    } else componentsSorted.map{ case (compSorted, operators) =>
      (compSorted, operators)
    }.distinct

  private def getOperatorsFields(operators: Seq[Operator]) : Seq[StructField] =
    operators.map(operator => StructField(operator.key, rowTypeFromOption(operator.returnType), true))

  private def getDimensionsFields(fields: Seq[DimensionBucket]) : Seq[StructField] =
    fields.map(field => StructField(field.getNameDimension, rowTypeFromOption(field.bucketType.typeOp), false))

  private def getFixedFieldAggregation(options: Map[String, String]) : Seq[StructField] =
    options.get(Output.FixedAggregation) match {
      case Some(field) => if(!field.isEmpty && field != "") Seq(Output.defaultStringField(field, true)) else Seq()
      case None => Seq()
    }

  def operatorsKeyOperation(operators: Seq[Operator]): Map[String, (WriteOp, TypeOp)] =
    operators.map(operator => (operator.key ->(operator.writeOperation, operator.returnType))).toMap
}
