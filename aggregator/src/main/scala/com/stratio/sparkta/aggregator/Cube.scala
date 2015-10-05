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

package com.stratio.sparkta.aggregator

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.{AppConstant, SparktaConfig}
import org.apache.spark.HashPartitioner
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

import scala.util.Try

/**
 * Use this class to describe a cube that you want the multicube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Cube or
 * multipelexer the output
 */

case class Cube(name: String,
                dimensions: Seq[Dimension],
                operators: Seq[Operator],
                multiplexer: Boolean,
                checkpointTimeDimension: String,
                checkpointInterval: Int,
                checkpointGranularity: String,
                checkpointTimeAvailability: Long) extends SLF4JLogging {

  private val associativeOperators = operators.filter(op => op.associative)
  private val associativeOperatorsMap = associativeOperators.map(op => op.key -> op).toMap
  private val nonAssociativeOperators = operators.filter(op => !op.associative)
  private val nonAssociativeOperatorsMap = nonAssociativeOperators.map(op => op.key -> op).toMap
  private lazy val rememberPartitioner =
    Try(SparktaConfig.getDetailConfig.get.getBoolean(AppConstant.ConfigRememberPartitioner)).getOrElse(true)

  def this(name: String,
           dimension: Dimension,
           operators: Seq[Operator],
           multiplexer: Boolean,
           checkpointTimeDimension: String,
           checkpointInterval: Int,
           checkpointGranularity: String,
           checkpointAvailable: Int) {
    this(name,
      Seq(dimension),
      operators,
      multiplexer,
      checkpointTimeDimension,
      checkpointInterval,
      checkpointGranularity,
      checkpointAvailable)
  }

  protected def filterDimensionValues(dimensionValues: DStream[(DimensionValuesTime, Map[String, JSerializable])])
  : DStream[(DimensionValuesTime, Map[String, JSerializable])] = {
    dimensionValues.map { case (dimensionsValuesTime, aggregationValues) => {
      val dimensionsFiltered = dimensionsValuesTime.dimensionValues.filter(dimVal =>
        dimensions.exists(comp => comp.name == dimVal.dimension.name))
      (DimensionValuesTime(dimensionsFiltered, dimensionsValuesTime.time, checkpointTimeDimension), aggregationValues)
    }
    }
  }

  def aggregate(dimensionsValues: DStream[(DimensionValuesTime,
    Map[String, JSerializable])]): DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    val valuesFiltered = filterDimensionValues(dimensionsValues)
    val withAssociative = operators.exists(op => op.associative)
    val withNonAssociative = operators.exists(op => !op.associative)
    (withAssociative, withNonAssociative) match {
      case (true, true) => {
        val nonAssociativeValues = aggregateNonAssociativeValues(updateNonAssociativeState(valuesFiltered))
        val associativeValues = updateAssociativeState(associativeAggregation(valuesFiltered))

        associativeValues.cogroup(nonAssociativeValues)
          .mapValues(aggregations => (aggregations._1.flatten ++ aggregations._2.flatten).toMap)
      }
      case (true, false) => updateAssociativeState(associativeAggregation(valuesFiltered))
      case (false, true) => aggregateNonAssociativeValues(updateNonAssociativeState(valuesFiltered))
      case _ => {
        log.warn("You should define operators for aggregate input values")
        dimensionsValues.map{ case (dimensionValueTime, aggregations) =>
          (dimensionValueTime, operators.map(op => op.key -> None).toMap)
        }
      }
    }
  }

  def noAggregationsState(dimensionsValues: DStream[(DimensionValuesTime,
    Map[String, JSerializable])]): DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    dimensionsValues.map {
      case (dimensionValueTime, aggregations) =>
        (dimensionValueTime, operators.map(op => op.key -> None).toMap)
    }
  }

  def associativeAggregation(dimensionsValues: DStream[(DimensionValuesTime, Map[String, JSerializable])]):
  DStream[(DimensionValuesTime, Seq[(String, Option[Any])])] = {
    dimensionsValues
      .mapValues(inputFields =>
      associativeOperators.flatMap(op => op.processMap(inputFields).map(op.key -> Some(_))).toMap)
      .groupByKey()
      .map { case (dimValues, aggregations) => {
      val aggregatedValues = aggregations.flatMap(_.toSeq)
        .groupBy(_._1)
        .map { case (nameOp, valuesOp) => {
        val op = associativeOperatorsMap(nameOp)
        val values = valuesOp.map(_._2)
        (nameOp, op.processReduce(values))
      }
      }.toSeq
      (dimValues, aggregatedValues)
    }
    }
  }

  protected def updateAssociativeState(dimensionsValues: DStream[(DimensionValuesTime, Seq[(String, Option[Any])])]):
  DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    dimensionsValues.checkpoint(new Duration(checkpointInterval))
    val newUpdateFunc = (iterator: Iterator[(DimensionValuesTime,
      Seq[Seq[(String, Option[Any])]],
      Option[Map[String, Option[Any]]])]) => {
      val eventTime =
        DateOperations.dateFromGranularity(DateTime.now(), checkpointGranularity) - checkpointTimeAvailability
      iterator.filter(dimensionsData => {
        dimensionsData._1.time >= eventTime
      })
        .flatMap { case (dimensionsKey, values, state) => updateAssociativeFunction(values, state)
        .map(result => (dimensionsKey, result))
      }
    }
    dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), rememberPartitioner)
  }

  protected def updateAssociativeFunction(values: Seq[Seq[(String, Option[Any])]],
                                          state: Option[Map[String, Option[Any]]])
  : Option[Map[String, Option[Any]]] = {
    val actualState = state.getOrElse(Map()).toSeq
    val processAssociative = (values.flatten ++ actualState)
      .groupBy(_._1)
      .map(aggregation => {
      val op = associativeOperatorsMap(aggregation._1)
      (aggregation._1, op.processAssociative(aggregation._2.map(_._2)))
    })
    Some(processAssociative)
  }

  protected def updateNonAssociativeState(dimensionsValues: DStream[(DimensionValuesTime, Map[String, JSerializable])]):
  DStream[(DimensionValuesTime, Seq[(String, Option[Any])])] = {
    dimensionsValues.checkpoint(new Duration(checkpointInterval))
    val newUpdateFunc = (iterator: Iterator[(DimensionValuesTime,
      Seq[Map[String, JSerializable]],
      Option[Seq[(String, Option[Any])]])]) => {
      val eventTime =
        DateOperations.dateFromGranularity(DateTime.now(), checkpointGranularity) - checkpointTimeAvailability
      iterator.filter(dimensionsData => {
        dimensionsData._1.time >= eventTime
      })
        .flatMap { case (dimensionsKey, values, state) => updateNonAssociativeFunction(values, state)
        .map(result => (dimensionsKey, result))
      }
    }
    dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), rememberPartitioner)
  }

  protected def updateNonAssociativeFunction(values: Seq[Map[String, JSerializable]],
                                             state: Option[Seq[(String, Option[Any])]])
  : Option[Seq[(String, Option[Any])]] = {
    val proccessMapValues = values.flatMap(inputFields =>
      nonAssociativeOperators.flatMap(op => op.processMap(inputFields).map(op.key -> Some(_))))
    Some(state.getOrElse(Seq()) ++ proccessMapValues)
  }

  protected def aggregateNonAssociativeValues(
                                               dimensionsValues: DStream[(DimensionValuesTime,
                                                 Seq[(String, Option[Any])])])
  : DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    dimensionsValues.mapValues(aggregationValues => {
      aggregationValues.groupBy { case (name, value) => name }
        .map { case (name, value) => {
        val as = "as"
        (name, nonAssociativeOperatorsMap(name).processReduce(value.map(_._2)))
      } }
    })
  }

  override def toString: String = "[Cube over " + dimensions + "]"

  def getDimensionsSorted: Seq[Dimension] = dimensions.sorted

  def getDimensionsNames: Seq[String] = dimensions.map(dimension => dimension.name)

  def getPrecisionsNames: Seq[String] = dimensions.map(dimension => dimension.getNamePrecision)

  def getDimensionsNames(dimensions: Seq[Dimension]): Seq[String] = dimensions.map(dimension => dimension.name)

  def getPrecisionsNames(dimensions: Seq[Dimension]): Seq[String] =
    dimensions.map(dimension => dimension.getNamePrecision)

  def getDimensionsNamesSorted: Seq[String] = getDimensionsNames(getDimensionsSorted)

  def getOperatorsSorted: Seq[Operator] = operators.sorted

  def getOperatorsNames(operatorsNames: Seq[Operator]): Seq[String] = operatorsNames.map(operator => operator.key)

  def getOperatorsNames: Seq[String] = operators.map(operator => operator.key)

  def getOperatorsNamesSorted: Seq[String] = getOperatorsNames(getOperatorsSorted)
}

