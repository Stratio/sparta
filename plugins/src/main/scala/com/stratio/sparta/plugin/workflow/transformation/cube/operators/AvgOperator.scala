/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.{Associative, Operator}
import com.stratio.sparta.sdk.utils.CastingUtils
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import scala.util.Try

class AvgOperator(
                    name: String,
                    val whenRowErrorDo: WhenRowError,
                    val whenFieldErrorDo: WhenFieldError,
                    inputField: Option[String] = None
                  ) extends Operator(name, whenRowErrorDo, whenFieldErrorDo, inputField) with Associative {

  assert(inputField.isDefined)

  override val defaultOutputType: DataType = MapType(StringType, DoubleType)

  private val SumKey = "sum"
  private val MeanKey = "mean"
  private val CountKey = "count"

  override def processMap(inputRow: Row): Option[Any] = processMapFromInputField(inputRow)

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] =
    returnFromTryWithNullCheck("Error in AvgOperator when reducing values") {
      Try {
        values.flatten.flatMap { value =>
          value match {
            case v if v.isInstanceOf[Seq[Double]] => v.asInstanceOf[Seq[Double]]
            case _ => Seq(CastingUtils.castingToSchemaType(DoubleType, value).asInstanceOf[Double])
          }
        }
      }
    }

  //scalastyle:off
  def associativity(values: Iterable[(String, Option[Any])]): Option[Any] = {
    returnFromTryWithNullCheck("Error in AvgOperator when associating values") {
      Try {
        val oldValues = extractValues(values, Option(Operator.OldValuesKey)).map { value =>
          CastingUtils.castingToSchemaType(MapType(StringType, DoubleType), value).asInstanceOf[Map[String, Double]]
        }.headOption
        val newValues = extractValues(values, Option(Operator.NewValuesKey)).flatMap{value =>
          CastingUtils.castingToSchemaType(ArrayType(DoubleType), value).asInstanceOf[Seq[Double]]
        }.toList

        if (newValues.nonEmpty) {
          val oldCount = oldValues.fold(0d) { oldV => oldV.getOrElse(CountKey, 0d) }
          val oldSum = oldValues.fold(0d) { oldV => oldV.getOrElse(SumKey, 0d) }
          val calculatedSum = oldSum + newValues.sum
          val calculatedCount = oldCount + newValues.size.toDouble
          val calculatedMean = if (calculatedCount != 0d) calculatedSum / calculatedCount else 0d

          Map(SumKey -> calculatedSum, CountKey -> calculatedCount, MeanKey -> calculatedMean)
        } else oldValues.getOrElse(Map(SumKey -> 0d, CountKey -> 0d, MeanKey -> 0d))
      }
    }
  }
}
