/**
  * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.aggregator

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.constants.AppConstant
import org.apache.spark.HashPartitioner
import org.apache.spark.streaming.dstream.DStream

import scala.reflect.ClassTag
import scala.util.Try

abstract class BaseCube[T: ClassTag] extends SLF4JLogging with Serializable with Cube {

  private val associativeOperators = operators.filter(op => op.isAssociative)
  private lazy val associativeOperatorsMap = associativeOperators.map(op => op.key -> op).toMap
  private val nonAssociativeOperators = operators.filter(op => !op.isAssociative)
  private lazy val nonAssociativeOperatorsMap = nonAssociativeOperators.map(op => op.key -> op).toMap
  protected lazy val rememberPartitioner =
    Try(SparktaConfig.getDetailConfig.get.getBoolean(AppConstant.ConfigRememberPartitioner))
      .getOrElse(AppConstant.DefaultRememberPartitioner)
  private final val NotUpdatedValues = 0
  protected final val UpdatedValues = 1

  /**
    * Aggregation process that have 4 ways:
    * 1. Cube with associative operators only.
    * 2. Cube with non associative operators only.
    * 3. Cube with associtaive and non associative operators.
    * 4. Cube with no operators.
    */

  def aggregate(dimensionsValues: DStream[(T, InputFieldsValues)])
  : DStream[(T, MeasuresValues)] = {

    val filteredValues = filterDimensionValues(dimensionsValues)
    val associativesCalculated = if (associativeOperators.nonEmpty)
      Option(updateAssociativeState(associativeAggregation(filteredValues)))
    else None
    val nonAssociativesCalculated = if (nonAssociativeOperators.nonEmpty)
      Option(aggregateNonAssociativeValues(updateNonAssociativeState(filteredValues)))
    else None

    (associativesCalculated, nonAssociativesCalculated) match {
      case (Some(associativeValues), Some(nonAssociativeValues)) =>
        associativeValues.cogroup(nonAssociativeValues)
          .mapValues { case (associativeAggregations, nonAssociativeAggregations) => MeasuresValues(
            (associativeAggregations.flatMap(_.values) ++ nonAssociativeAggregations.flatMap(_.values))
              .toMap)
          }
      case (Some(associativeValues), None) => associativeValues
      case (None, Some(nonAssociativeValues)) => nonAssociativeValues
      case _ =>
        log.warn("You should define operators for aggregate input values")
        noAggregationsState(dimensionsValues)
    }
  }

  protected def updateNonAssociativeState(dimensionsValues: DStream[(T, InputFields)])
  : DStream[(T, Seq[Aggregation])]


  protected def filterDimensionValues(dimensionValues: DStream[(T, InputFieldsValues)])
  : DStream[(T, InputFields)]

  protected def updateAssociativeState(dimensionsValues: DStream[(T, AggregationsValues)])
  : DStream[(T, MeasuresValues)]


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

  protected def aggregateNonAssociativeValues(dimensionsValues: DStream[(T, Seq[Aggregation])])
  : DStream[(T, MeasuresValues)] =

    dimensionsValues.mapValues(aggregationValues => {
      val measures = aggregationValues.groupBy(aggregation => aggregation.name)
        .map { case (name, aggregations) =>
          (name, nonAssociativeOperatorsMap(name).processReduce(aggregations.map(aggregation => aggregation.value)))
        }
      MeasuresValues(measures)
    })

  def associativeAggregation(dimensionsValues: DStream[(T, InputFields)])
  : DStream[(T, AggregationsValues)] = {
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

  protected def extractAssociativeAggregations(inputFieldsValues: InputFieldsValues) : Seq[Aggregation] =
    associativeOperators.map(op => Aggregation(op.key, op.processMap(inputFieldsValues)))

  protected def groupAssociativeAggregations(aggregations: Seq[Aggregation]) : AggregationsValues = {
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


  protected def noAggregationsState(dimensionsValues: DStream[(T, InputFieldsValues)])
  : DStream[(T, MeasuresValues)] =
    dimensionsValues.mapValues(aggregations =>
      MeasuresValues(operators.map(op => op.key -> None).toMap))

  /**
    * Filter measuresValues that are been changed in this window
    */

  protected def filterUpdatedMeasures(values: DStream[(T, Measures)])
  : DStream[(T, MeasuresValues)] =
    values.flatMapValues(measures => if (measures.newValues == UpdatedValues) Some(measures.measuresValues) else None)

  /**
    * Filter aggregationsValues that are been changed in this window
    */

  protected def filterUpdatedAggregationsValues(values: DStream[(T, AggregationsValues)])
  : DStream[(T, Seq[Aggregation])] =
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
