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
package com.stratio.sparta.driver.step

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.writer.WriterOptions
import com.stratio.sparta.sdk.pipeline.aggregation.cube._
import com.stratio.sparta.sdk.pipeline.aggregation.operator.{Associative, Operator}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import org.apache.spark.HashPartitioner
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

import scala.util.Try

/**
 * Use this class to describe a cube that you want the multicube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Cube
 */

case class Cube(name: String,
                dimensions: Seq[Dimension],
                operators: Seq[Operator],
                initSchema: StructType,
                schema: StructType,
                dateType: TypeOp.Value = TypeOp.Timestamp,
                expiringDataConfig: Option[ExpiringData] = None,
                triggers: Seq[Trigger],
                writerOptions: WriterOptions,
                rememberPartitioner : Boolean) extends SLF4JLogging {

  private val associativeOperators = operators.filter(op => op.isAssociative)
  private lazy val associativeOperatorsMap = associativeOperators.map(op => op.key -> op).toMap
  private val nonAssociativeOperators = operators.filter(op => !op.isAssociative)
  private lazy val nonAssociativeOperatorsMap = nonAssociativeOperators.map(op => op.key -> op).toMap
  private final val NotUpdatedValues = 0
  private final val UpdatedValues = 1

  /**
   * Aggregation process that have 4 ways:
   * 1. Cube with associative operators only.
   * 2. Cube with non associative operators only.
   * 3. Cube with associtaive and non associative operators.
   * 4. Cube with no operators.
   */

  def aggregate(dimensionsValues: DStream[(DimensionValuesTime, InputFields)])
  : DStream[(DimensionValuesTime, MeasuresValues)] = {

    val associativesCalculated = if (associativeOperators.nonEmpty)
      Option(updateAssociativeState(associativeAggregation(dimensionsValues)))
    else None
    val nonAssociativesCalculated = if (nonAssociativeOperators.nonEmpty)
      Option(aggregateNonAssociativeValues(updateNonAssociativeState(dimensionsValues)))
    else None

    (associativesCalculated, nonAssociativesCalculated) match {
      case (Some(associativeValues), Some(nonAssociativeValues)) =>
        associativeValues.cogroup(nonAssociativeValues)
          .mapValues { case (associativeAggregations, nonAssociativeAggregations) => MeasuresValues(
            (associativeAggregations.flatMap(_.values) ++ nonAssociativeAggregations.flatMap(_.values)).toMap)
          }
      case (Some(associativeValues), None) => associativeValues
      case (None, Some(nonAssociativeValues)) => nonAssociativeValues
      case _ =>
        log.warn("You should define operators for aggregate input values")
        noAggregationsState(dimensionsValues)
    }
  }

  private[driver] def updateFuncNonAssociativeWithTime =
    (iterator: Iterator[(DimensionValuesTime, Seq[InputFields], Option[AggregationsValues])]) => {

      iterator.filter {
        case (DimensionValuesTime(_, _, Some(timeConfig)), _, _) =>
          timeConfig.eventTime >= dateFromGranularity
        case (DimensionValuesTime(_, _, None), _, _) =>
          throw new IllegalArgumentException("Time configuration expected")
      }.flatMap { case (dimensionsKey, values, state) =>
        updateNonAssociativeFunction(values, state).map(result => (dimensionsKey, result))
      }
    }

  def dateFromGranularity: Long =
    DateTime.now().getMillis - AggregationTime.parseValueToMilliSeconds(expiringDataConfig.get.timeAvailability)

  private[driver] def updateFuncNonAssociativeWithoutTime =
    (iterator: Iterator[(DimensionValuesTime, Seq[InputFields], Option[AggregationsValues])]) => {
      iterator
        .flatMap { case (dimensionsKey, values, state) =>
          updateNonAssociativeFunction(values, state).map(result => (dimensionsKey, result))
        }
    }

  protected def updateNonAssociativeState(dimensionsValues: DStream[(DimensionValuesTime, InputFields)])
  : DStream[(DimensionValuesTime, Seq[Aggregation])] = {

    val newUpdateFunc = expiringDataConfig match {
      case None => updateFuncNonAssociativeWithoutTime
      case Some(_) => updateFuncNonAssociativeWithTime
    }

    val valuesCheckpointed = dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), rememberPartitioner)

    filterUpdatedAggregationsValues(valuesCheckpointed)
  }

  protected def updateNonAssociativeFunction(values: Seq[InputFields], state: Option[AggregationsValues])
  : Option[AggregationsValues] = {
    val proccessMapValues = values.flatMap(aggregationsValues =>
      nonAssociativeOperators.map(op => Aggregation(op.key, op.processMap(aggregationsValues.fieldsValues))))
    val lastState = state match {
      case Some(measures) => measures.values
      case None => Seq.empty
    }
    val (aggregations, newValues) = getUpdatedAggregations(lastState ++ proccessMapValues, values.nonEmpty)

    Option(AggregationsValues(aggregations, newValues))
  }

  protected def aggregateNonAssociativeValues(dimensionsValues: DStream[(DimensionValuesTime, Seq[Aggregation])])
  : DStream[(DimensionValuesTime, MeasuresValues)] =

    dimensionsValues.mapValues(aggregationValues => {
      val measures = aggregationValues.groupBy(aggregation => aggregation.name)
        .map { case (name, aggregations) =>
          (name, nonAssociativeOperatorsMap(name).processReduce(aggregations.map(aggregation => aggregation.value)))
        }
      MeasuresValues(measures)
    })

  private[driver] def updateFuncAssociativeWithTime =
    (iterator: Iterator[(DimensionValuesTime, Seq[AggregationsValues], Option[Measures])]) => {
      iterator.filter {
        case (DimensionValuesTime(_, _, Some(timeConfig)), _, _) =>
          timeConfig.eventTime >= dateFromGranularity
        case (DimensionValuesTime(_, _, None), _, _) =>
          throw new IllegalArgumentException("Time configuration expected")
      }
        .flatMap { case (dimensionsKey, values, state) =>
          updateAssociativeFunction(values, state).map(result => (dimensionsKey, result))
        }
    }

  private[driver] def updateFuncAssociativeWithoutTime =
    (iterator: Iterator[(DimensionValuesTime, Seq[AggregationsValues], Option[Measures])]) => {
      iterator
        .flatMap { case (dimensionsKey, values, state) =>
          updateAssociativeFunction(values, state).map(result => (dimensionsKey, result))
        }
    }

  protected def updateAssociativeState(dimensionsValues: DStream[(DimensionValuesTime, AggregationsValues)])
  : DStream[(DimensionValuesTime, MeasuresValues)] = {

    val newUpdateFunc = expiringDataConfig match {
      case None => updateFuncAssociativeWithoutTime
      case Some(_) => updateFuncAssociativeWithTime
    }

    val valuesCheckpointed = dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), rememberPartitioner)

    filterUpdatedMeasures(valuesCheckpointed)
  }

  protected def associativeAggregation(dimensionsValues: DStream[(DimensionValuesTime, InputFields)])
  : DStream[(DimensionValuesTime, AggregationsValues)] = {

    val initialAggregation = (inputFields: InputFields) => {
      AggregationsValues(extractAssociativeAggregations(inputFields.fieldsValues), inputFields.newValues)
    }

    val combineAggregations = (aggregations: AggregationsValues, inputFields: InputFields) => {
      val combinedAggregations = aggregations.values ++ extractAssociativeAggregations(inputFields.fieldsValues)
      groupAssociativeAggregations(combinedAggregations)
    }

    val mergeAggregationValues = (agg1: AggregationsValues, agg2: AggregationsValues) => {
      val combinedAggregations = agg1.values ++ agg2.values
      groupAssociativeAggregations(combinedAggregations)
    }

    dimensionsValues.combineByKey(initialAggregation,
      combineAggregations,
      mergeAggregationValues,
      new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism))
  }

  protected def extractAssociativeAggregations(inputFieldsValues: Row): Seq[Aggregation] =
    associativeOperators.map(op => Aggregation(op.key, op.processMap(inputFieldsValues)))

  protected def groupAssociativeAggregations(aggregations: Seq[Aggregation]): AggregationsValues = {
    val aggregationsGrouped = aggregations.groupBy { aggregation => aggregation.name }
      .map { case (nameOp, valuesOp) =>
        val op = associativeOperatorsMap(nameOp)
        val values = valuesOp.map { aggregation => aggregation.value }
        Aggregation(nameOp, op.processReduce(values))
      }.toSeq
    AggregationsValues(aggregationsGrouped, UpdatedValues)
  }

  //scalastyle:off
  protected def updateAssociativeFunction(values: Seq[AggregationsValues], state: Option[Measures])
  : Option[Measures] = {

    val stateWithoutUpdateVar = state match {
      case Some(measures) => measures.measuresValues.values
      case None => Map.empty
    }
    val actualState = stateWithoutUpdateVar.toSeq.map { case (key, value) => (key, (Operator.OldValuesKey, value)) }
    val newWithoutUpdateVar = values.map(aggregationsValues => aggregationsValues.values)
    val newValues = newWithoutUpdateVar.flatten.map(aggregation =>
      (aggregation.name, (Operator.NewValuesKey, aggregation.value)))
    val processAssociative = (newValues ++ actualState)
      .groupBy { case (key, _) => key }
      .map { case (opKey, opValues) =>
        associativeOperatorsMap(opKey) match {
          case op: Associative => (opKey, op.associativity(opValues.map { case (nameOp, valuesOp) => valuesOp }))
          case _ => (opKey, None)
        }
      }
    val (measuresValues, isNewMeasure) =
      getUpdatedAggregations(MeasuresValues(processAssociative), values.nonEmpty)

    Option(Measures(measuresValues, isNewMeasure))
  }

  //scalastyle:on

  protected def noAggregationsState(dimensionsValues: DStream[(DimensionValuesTime, InputFields)])
  : DStream[(DimensionValuesTime, MeasuresValues)] =
    dimensionsValues.mapValues(aggregations =>
      MeasuresValues(operators.map(op => op.key -> None).toMap))

  /**
   * Filter measuresValues that are been changed in this window
   */

  protected def filterUpdatedMeasures(values: DStream[(DimensionValuesTime, Measures)])
  : DStream[(DimensionValuesTime, MeasuresValues)] =
    values.flatMapValues(measures => if (measures.newValues == UpdatedValues) Some(measures.measuresValues) else None)

  /**
   * Filter aggregationsValues that are been changed in this window
   */

  protected def filterUpdatedAggregationsValues(values: DStream[(DimensionValuesTime, AggregationsValues)])
  : DStream[(DimensionValuesTime, Seq[Aggregation])] =
    values.flatMapValues(aggregationsValues => {
      if (aggregationsValues.newValues == UpdatedValues) Some(aggregationsValues.values) else None
    })

  /**
   * Return the aggregations with the correct key in case of the actual streaming window have new values for the
   * dimensions values.
   */

  protected def getUpdatedAggregations[T](aggregations: T, haveNewValues: Boolean): (T, Int) =
    if (haveNewValues)
      (aggregations, UpdatedValues)
    else (aggregations, NotUpdatedValues)
}