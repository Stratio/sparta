
/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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

package com.stratio.sparkta.driver.factory

import com.stratio.sparkta.aggregator.Rollup
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.sql.types._

/**
 * Created by jcgarcia on 16/04/15.
 */
object PolicyFactory {

  final val SEPARATOR = "_"

  def rowTypeFromOption(option: TypeOp): DataType= {
    option match {
        case TypeOp.Long =>  LongType
        case TypeOp.Double =>  DoubleType
        case TypeOp.Int =>  IntegerType
        case TypeOp.Boolean =>  BooleanType
        case TypeOp.Date =>  DateType
        case TypeOp.DateTime =>  TimestampType
        case TypeOp.String =>  StringType
        case _ => BinaryType
    }
  }

  def defaultRollupField(fieldName: String) : StructField ={
    StructField(fieldName, StringType, true)
  }

  def rollupsOperatorsSchemas(rollups: Seq[Rollup],
                              outputs: Seq[(String, Output)],
                              operators: Seq[Operator]) : Seq[TableSchema] = {
    val componentsSorted: Seq[(Seq[String], Seq[(Dimension, BucketType)])] = rollups.map(rollup =>
      (rollup.sortedComponentsNames, rollup.sortComponents))
    val operatorsFields: Seq[StructField] = operators.sortWith(_.key < _.key)
      .map(operator => StructField(operator.key, rowTypeFromOption(operator.returnType), true))
    val tablesSchemas: Seq[Seq[TableSchema]] = outputs.map(output => {
      for {
        rollupsCombinations: Seq[String] <- if (output._2.multiplexer){
          componentsSorted.map(component => Multiplexer.combine(component._1)).flatten.distinct
        } else componentsSorted.map(_._1).distinct
        schema : StructType = StructType(
          rollupsCombinations.map(fieldName => defaultRollupField(fieldName)) ++ operatorsFields)
      } yield TableSchema(output._1, rollupsCombinations.mkString(SEPARATOR), schema)
    }).distinct
    tablesSchemas.flatten
  }

  def operatorsKeyOperation(operators: Seq[Operator]): Map[String, (WriteOp, TypeOp)] = {
      operators.map(operator => (operator.key -> (operator.writeOperation, operator.returnType))).toMap
  }
}
