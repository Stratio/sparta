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

import com.stratio.sparkta.sdk._
import org.apache.spark.HashPartitioner
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

/**
 * Use this class to describe a cube that you want the multicube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Cube
 */

case class CubeWithoutTime(name: String,
                           dimensions: Seq[Dimension],
                           operators: Seq[Operator],
                           checkpointInterval: Int) extends BaseCube[DimensionValuesWithoutTime] {


  /**
    * Filter dimension values that correspond with the current cube dimensions
    */

  protected override def filterDimensionValues(dimensionValues:
                                               DStream[(DimensionValuesWithoutTime, InputFieldsValues)])
  : DStream[(DimensionValuesWithoutTime, InputFields)] = {

    dimensionValues.map { case (dimensionsValuesTime, aggregationValues) =>
      val dimensionsFiltered = dimensionsValuesTime.dimensionValues.filter(dimVal =>
        dimensions.exists(comp => comp.name == dimVal.dimension.name))

      (dimensionsValuesTime.copy(dimensionValues = dimensionsFiltered),
        InputFields(aggregationValues, UpdatedValues))
    }
  }

  protected override def  updateNonAssociativeState(dimensionsValues:
                                                    DStream[(DimensionValuesWithoutTime, InputFields)])
  : DStream[(DimensionValuesWithoutTime, Seq[Aggregation])] = {

    dimensionsValues.checkpoint(new Duration(checkpointInterval))

    val newUpdateFunc = (iterator:
                         Iterator[(DimensionValuesWithoutTime, Seq[InputFields], Option[AggregationsValues])]) => {

      iterator
        .flatMap { case (dimensionsKey, values, state) =>
          updateNonAssociativeFunction(values, state).map(result => (dimensionsKey, result))
        }
    }
    val valuesCheckpointed = dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), rememberPartitioner)

    filterUpdatedAggregationsValues(valuesCheckpointed)
  }

  protected override def updateAssociativeState(dimensionsValues:
                                                DStream[(DimensionValuesWithoutTime, AggregationsValues)])
  : DStream[(DimensionValuesWithoutTime, MeasuresValues)] = {

    dimensionsValues.checkpoint(new Duration(checkpointInterval))

    val newUpdateFunc = (iterator:
                         Iterator[(DimensionValuesWithoutTime, Seq[AggregationsValues], Option[Measures])]) => {

      iterator
        .flatMap { case (dimensionsKey, values, state) =>
          updateAssociativeFunction(values, state).map(result => (dimensionsKey, result))
        }
    }

    val valuesCheckpointed = dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), rememberPartitioner)

    filterUpdatedMeasures(valuesCheckpointed)
  }


}