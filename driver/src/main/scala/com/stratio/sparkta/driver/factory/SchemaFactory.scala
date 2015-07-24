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

import com.stratio.sparkta.aggregator.Cube
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

object SchemaFactory {

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

  def cubesOperatorsSchemas(cubes: Seq[Cube],
                              configOptions: Seq[(String, Map[String, String])]): Seq[TableSchema] = {
    val dimensionsSorted = cubes.map(cube =>
      (cube.getDimensionsSorted, cube.operators, cube.checkpointTimeDimension))
    configOptions.flatMap{ case (outputName, configOptions) => {
      for {
        (dimensionCombinations, operators, timeDimension) <- getCombinationsWithOperators(configOptions,
          dimensionsSorted)
        dimensionNames = dimensionCombinations.map(_.name)
        schema = StructType( getDimensionsFields(dimensionCombinations) ++ timeDimensionFieldType(timeDimension) ++
          (getOperatorsFields(operators) ++ getFixedFieldAggregation(configOptions)).sortWith(_.name < _.name))
      } yield TableSchema(outputName, dimensionNames.mkString(Output.Separator), schema, timeDimension)
    }}.distinct
  }

  private def getCombinationsWithOperators(configOptions: Map[String, String],
                                           dimensionsSorted: Seq[(Seq[Dimension], Seq[Operator], String)])
  : Seq[(Seq[Dimension], Seq[Operator], String)] =
    if (Try(configOptions.get(Output.Multiplexer).get.toBoolean).getOrElse(false)) {
      dimensionsSorted.flatMap{ case (dimSorted, operators, timeDimension) =>
        Multiplexer.combine(dimSorted, operators, timeDimension)
      }.distinct
    } else dimensionsSorted.map{ case (compSorted, operators, timeDimension) =>
      (compSorted, operators, timeDimension)
    }.distinct

  def timeDimensionFieldType(timeDimension: String) : Seq[StructField] = {
    Seq(Output.getFieldType(TypeOp.DateTime, timeDimension, false))
  }

  private def getOperatorsFields(operators: Seq[Operator]) : Seq[StructField] =
    operators.map(operator => StructField(operator.key, rowTypeFromOption(operator.returnType), true))

  private def getDimensionsFields(fields: Seq[Dimension]) : Seq[StructField] =
    fields.map(field => StructField(field.name, rowTypeFromOption(field.precision.typeOp), false))

  private def getFixedFieldAggregation(options: Map[String, String]) : Seq[StructField] =
    options.get(Output.FixedAggregation) match {
      case Some(field) => if(!field.isEmpty && field != "") Seq(Output.defaultStringField(field, true)) else Seq()
      case None => Seq()
    }

  def operatorsKeyOperation(operators: Seq[Operator]): Map[String, (WriteOp, TypeOp)] =
    operators.map(operator => (operator.key ->(operator.writeOperation, operator.returnType))).toMap
}
