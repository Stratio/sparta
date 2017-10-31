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
package com.stratio.sparta.plugin.workflow.transformation.cube

import akka.event.slf4j.SLF4JLogging
import com.github.nscala_time.time.Imports.DateTime
import com.stratio.sparta.plugin.workflow.transformation.cube.Cube._
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.sdk.utils.AggregationTimeUtils._
import com.stratio.sparta.sdk.utils.CastingUtils
import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.step.ErrorCheckingOption
import org.apache.spark.HashPartitioner
import org.apache.spark.sql.Row
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, State, StateSpec, Time}

import scala.util.{Failure, Success, Try}

case class Cube(
                 dimensions: Seq[Dimension],
                 operators: Seq[Operator],
                 whenErrorDo: WhenError = WhenError.Error,
                 partitions: Option[Int] = None,
                 timeOutKey: Option[Int] = None,
                 waterMarkPolicy: Option[WaterMarkPolicy] = None
               ) extends SLF4JLogging with ErrorCheckingOption {

  private lazy val associativeOperators = operators.filter(op => op.isAssociative)
  private lazy val associativeOperatorsMap = associativeOperators.map(op => op.name -> op).toMap
  private lazy val nonAssociativeOperators = operators.filter(op => !op.isAssociative)
  private lazy val nonAssociativeOperatorsMap = nonAssociativeOperators.map(op => op.name -> op).toMap


  /* PUBLIC METHODS */

  /**
    * Extract a modified stream that will be needed to calculate measures.
    *
    * @param inputStream with the original stream of data.
    * @return a modified stream after join dimensions and measures.
    */

  def createDStream(inputStream: DStream[Row]): DStream[(DimensionValues, InputFields)] =
    inputStream.flatMap { row =>
      returnFromTry(s"Error creating cube value from row: $row") {
        Try {
          val schema = row.schema
          val dimValues = dimensions.map { dimension =>
            DimensionValue(
              dimension,
              row.get(schema.fieldIndex(dimension.name)),
              schema.find(field => field.name == dimension.name).get
            )
          }
          val waterMark = waterMarkPolicy.map(policy =>
            WaterMark(getWaterMark(row, policy), policy.name)
          )

          (DimensionValues(dimValues, waterMark), InputFields(row, UpdatedValues))
        }
      }
    }

  /**
    * Aggregation process that have 4 options:
    * 1. Cube with associative measures only.
    * 2. Cube with non associative measures only.
    * 3. Cube with associtaive and non associative measures.
    * 4. Cube with no measures.
    */

  def execute(dimensionsValues: DStream[(DimensionValues, InputFields)]): DStream[(DimensionValues, MeasuresValues)] = {

    val associativeStream = associativeOperators.headOption map { _ =>
      updateAssociativeState(calculateWithCombiner(dimensionsValues))
    }

    val nonAssociativeStream = nonAssociativeOperators.headOption map { _ =>
      reduceNoAssociativeValues(updateNoAssociativeState(dimensionsValues))
    }

    val both = for (associativeValues <- associativeStream; nonAssociativeValues <- nonAssociativeStream) yield {
      associativeValues.cogroup(nonAssociativeValues)
        .mapValues { case (associativeAggregations, nonAssociativeAggregations) =>
          MeasuresValues(
            (associativeAggregations.flatMap(_.values) ++ nonAssociativeAggregations.flatMap(_.values)).toMap)
        }
    }

    both orElse associativeStream orElse nonAssociativeStream getOrElse {
      throw new Exception("You should define measures for aggregate input values")
    }

  }


  /* PRIVATE METHODS */

  //WaterMark functions

  private[cube] def getWaterMark(inputValues: Row, waterMarkPolicy: WaterMarkPolicy): Long =
    Try(inputValues.schema.fieldIndex(waterMarkPolicy.name)) match {
      case Success(index) =>
        CastingUtils.checkLongType(inputValues.get(index)).asInstanceOf[Long]
      case Failure(e) =>
        val message = s"Impossible to extract waterMark from row ${inputValues.mkString(",")}"
        log.error(message, e)
        throw new Exception(message, e)
    }

  private[cube] def waterMarkLimit: Long =
    new DateTime().getMillis - parseValueToMilliSeconds(waterMarkPolicy.get.availability)

  /* Not associative States functions */

  //scalastyle:off
  private[cube] def noAssociativeState(
                                        batchTime: Time,
                                        key: DimensionValues,
                                        values: Option[InputFields],
                                        state: State[Aggregations]
                                      ): Option[(DimensionValues, Aggregations)] = {
    if (!state.isTimingOut()) {
      if (waterMarkPolicy.forall(_ => key.waterMark.get.value >= waterMarkLimit)) {
        val currentStateValues = state.getOption.toSeq.flatMap(_.values)
        val newValues = for {
          inputFields <- values.toSeq
          op <- nonAssociativeOperators
        } yield AggregationValue(op.name, op.processMap(inputFields.fieldsValues))
        val (aggregationValues, hasNewValues) = getUpdatedAggregations(currentStateValues ++ newValues, newValues.nonEmpty)
        val aggregationsCalculated = Aggregations(aggregationValues, hasNewValues)

        state.update(aggregationsCalculated)

        //Now the cube only use the states, the returned value is ignored
        None
      } else {
        state.remove()
        None
      }
    } else None
  }

  //scalastyle:on


  private[cube] def updateNoAssociativeState(dimensionsValues: DStream[(DimensionValues, InputFields)])
  : DStream[(DimensionValues, Seq[AggregationValue])] = {
    val updateStateFunc = StateSpec.function(noAssociativeState _)
    val stateWithPartitions = partitions.map(updateStateFunc.numPartitions).getOrElse(updateStateFunc)
    val stateWithPartitionsTimeout = timeOutKey.map(seconds => stateWithPartitions.timeout(Seconds(seconds)))
      .getOrElse(stateWithPartitions)

    dimensionsValues.mapWithState(stateWithPartitionsTimeout)
      .stateSnapshots()
      .flatMapValues(aggregations => if (aggregations.newValues == UpdatedValues) Some(aggregations.values) else None)
  }

  private[cube] def reduceNoAssociativeValues(dimensionsValues: DStream[(DimensionValues, Seq[AggregationValue])])
  : DStream[(DimensionValues, MeasuresValues)] =
    dimensionsValues.mapValues { aggregationValues =>
      val measures = aggregationValues.groupBy(aggregation => aggregation.name)
        .map { case (name, aggregations) =>
          (name, nonAssociativeOperatorsMap(name).processReduce(aggregations.map(aggregation => aggregation.value)))
        }
      MeasuresValues(measures)
    }


  /* Associative state functions */

  private[cube] def updateAssociativeState(dimensionsValues: DStream[(DimensionValues, Aggregations)])
  : DStream[(DimensionValues, MeasuresValues)] = {
    val updateStateFunc = StateSpec.function(associativeState _)
    val stateWithPartitions = partitions.map(updateStateFunc.numPartitions).getOrElse(updateStateFunc)
    val stateWithPartitionsTimeout = timeOutKey.map(seconds => stateWithPartitions.timeout(Seconds(seconds)))
      .getOrElse(stateWithPartitions)

    dimensionsValues.mapWithState(stateWithPartitionsTimeout)
      .stateSnapshots()
      .flatMapValues(measures => if (measures.newValues == UpdatedValues) Some(measures.measuresValues) else None)
  }

  //scalastyle:off
  private[cube] def associativeState(
                                      batchTime: Time,
                                      key: DimensionValues,
                                      values: Option[Aggregations],
                                      state: State[Measures]
                                    ): Option[(DimensionValues, Measures)] = {
    if (!state.isTimingOut()) {
      if (waterMarkPolicy.forall(_ => key.waterMark.get.value >= waterMarkLimit)) {
        val currentStateValues = for {
          measures <- state.getOption().toSeq
          (measureKey, measureValue) <- measures.measuresValues.values
        } yield (measureKey, (Operator.OldValuesKey, measureValue))
        val newValues = values.toSeq.flatMap(_.values.map(aggregation => (aggregation.name, (Operator.NewValuesKey, aggregation.value))))
        val processAssociative = (newValues ++ currentStateValues)
          .groupBy { case (aggregationName, _) => aggregationName }
          .map { case (opKey, opValues) =>
            associativeOperatorsMap(opKey) match {
              case op: Associative => (opKey, op.associativity(opValues.map { case (_, valuesOp) => valuesOp }))
              case _ => (opKey, None)
            }
          }
        val (measuresValues, isNewMeasure) = getUpdatedAggregations(MeasuresValues(processAssociative), newValues.nonEmpty)
        val measuresCalculated = Measures(measuresValues, isNewMeasure)

        state.update(measuresCalculated)

        //Now the cube only use the states, the returned value is ignored
        None
      } else {
        state.remove()
        None
      }
    } else None
  }

  //scalastyle:on

  private[cube] def calculateWithCombiner(dimensionsValues: DStream[(DimensionValues, InputFields)])
  : DStream[(DimensionValues, Aggregations)] = {
    val initialAggregation = (inputFields: InputFields) => {
      Aggregations(mapAssociative(inputFields.fieldsValues), inputFields.newValues)
    }
    val combineAggregations = (aggregations: Aggregations, inputFields: InputFields) => {
      val combinedAggregations = aggregations.values ++ mapAssociative(inputFields.fieldsValues)
      reduceAssociative(combinedAggregations)
    }
    val mergeAggregationValues = (agg1: Aggregations, agg2: Aggregations) => {
      val combinedAggregations = agg1.values ++ agg2.values
      reduceAssociative(combinedAggregations)
    }

    dimensionsValues.combineByKey(initialAggregation,
      combineAggregations,
      mergeAggregationValues,
      new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism))
  }

  private[cube] def mapAssociative(inputFieldsValues: Row): Seq[AggregationValue] =
    associativeOperators.map(op => AggregationValue(op.name, op.processMap(inputFieldsValues)))

  private[cube] def reduceAssociative(aggregations: Seq[AggregationValue]): Aggregations = {
    val aggregationsGrouped = aggregations.groupBy { aggregation => aggregation.name }
      .map { case (nameOp, valuesOp) =>
        val op = associativeOperatorsMap(nameOp)
        val values = valuesOp.map { aggregation => aggregation.value }
        AggregationValue(nameOp, op.processReduce(values))
      }.toSeq

    Aggregations(aggregationsGrouped, UpdatedValues)
  }


  /* Return updated values */

  private[cube] def getUpdatedAggregations[T](aggregations: T, haveNewValues: Boolean): (T, Int) =
    if (haveNewValues) (aggregations, UpdatedValues)
    else (aggregations, NotUpdatedValues)
}

object Cube {

  val NotUpdatedValues = 0
  val UpdatedValues = 1

}