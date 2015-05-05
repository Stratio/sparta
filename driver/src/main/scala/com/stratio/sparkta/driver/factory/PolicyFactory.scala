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

import com.stratio.sparkta.aggregator.Rollup
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.sql.types._

object PolicyFactory {

  final val GeoLabel = "precision"

  def rowTypeFromOption(optionType: TypeOp): DataType =
    optionType match {
      case TypeOp.Long => LongType
      case TypeOp.Double => DoubleType
      case TypeOp.Int => IntegerType
      case TypeOp.Boolean => BooleanType
      case TypeOp.Date => DateType
      case TypeOp.DateTime => TimestampType
      case TypeOp.ArrayDouble => ArrayType(DoubleType)
      case TypeOp.ArrayString => ArrayType(StringType)
      case _ => StringType
    }

  def defaultRollupField(fieldName: String) : StructField = StructField(fieldName, StringType, false)

  def geoRollupField(fieldName: String) : StructField = StructField(fieldName, ArrayType(DoubleType), false)

  def rollupsOperatorsSchemas(rollups: Seq[Rollup],
                              outputs: Seq[(String, Boolean)],
                              operators: Seq[Operator]) : Seq[TableSchema] = {
    val componentsSorted = rollups.map(rollup => (rollup.sortedComponentsNames, rollup.sortComponents))
    val operatorsFields = operators.sortWith(_.key < _.key)
      .map(operator => StructField(operator.key, rowTypeFromOption(operator.returnType), true))
    outputs.flatMap(output => {
      for {
        rollupsCombinations <- if (output._2){
          componentsSorted.flatMap(component => Multiplexer.combine(component._1)).distinct
        } else componentsSorted.map(_._1).distinct
        schema = StructType(rollupsCombinations.map(fieldName => {
          if(fieldName.toLowerCase().contains(GeoLabel)) geoRollupField(fieldName) else defaultRollupField(fieldName)
        }) ++ operatorsFields)
      } yield TableSchema(output._1, rollupsCombinations.mkString(Output.SEPARATOR), schema)
    }).distinct
  }

  def operatorsKeyOperation(operators: Seq[Operator]): Map[String, (WriteOp, TypeOp)] = {
      operators.map(operator => (operator.key -> (operator.writeOperation, operator.returnType))).toMap
  }
}
