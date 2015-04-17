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
import com.stratio.sparkta.sdk._
import org.apache.spark.sql.types._
import org.joda.time._
import java.util.Date

/**
 * Created by jcgarcia on 16/04/15.
 */
object PolicyFactory {

  final val SEPARATOR = "_"
  // scalastyle:ignore

  def rowTypeFromOption(option: Option[_<:Any]): DataType= {
    option.get match {
        case value : Class[String] =>  StringType
        case value : Class[Long] =>  LongType
        case value : Class[Double] =>  DoubleType
        case value : Class[Int] =>  IntegerType
        case value : Class[Boolean] =>  BooleanType
        case value : Class[Date] =>  DateType
        case value : Class[DateTime] =>  TimestampType
        case _ => BinaryType
    }
  }

  def defaultRollupField(fieldName: String) : StructField ={
    StructField(fieldName, StringType, true)
  }

  def rollupsOperatorsSchemas(rollups: Seq[Rollup],
                              outputs: Seq[(String, Output)],
                              operators: Seq[Operator]) : Map[String, StructType] = {

    val componentsSorted: Seq[(Seq[String], Seq[(Dimension, BucketType)])] = rollups.map(rollup =>
      (rollup.sortedComponentsNames, rollup.sortComponents))
    val operatorsFields: Seq[StructField] = operators.sortWith(_.key < _.key)
      .map(operator => StructField(operator.key, rowTypeFromOption(Some(operator.returnType)), false))
    val tablesSchemas: Seq[Seq[(String, StructType)]] = outputs.map(output => {
      for {
        rollupsCombinations: (Seq[String], Seq[(Dimension, BucketType)]) <- if (output._2.multiplexer){
          Multiplexer.combine(componentsSorted).flatten
        } else componentsSorted
        schema : StructType = StructType(
          rollupsCombinations._1.map(fieldName => defaultRollupField(fieldName)) ++ operatorsFields)
      } yield (output._1 + SEPARATOR + rollupsCombinations._1.mkString(SEPARATOR), schema)
    }).distinct
    tablesSchemas.flatten.toMap
  }
}
