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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}
import java.sql.Timestamp

import com.stratio.sparkta.sdk.test.DimensionTypeMock
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AggregateOperationsTest extends FlatSpec with ShouldMatchers {

  trait CommonValues {

    val timeDimension = "minute"
    val timestamp = 1L
    val defaultDimension = new DimensionTypeMock(Map())
    val dimensionValuesT = DimensionValuesTime("testCube",Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("dim2", "eventKey", "identity", defaultDimension), "value2"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)),
      timestamp, timeDimension)
    val measures = MeasuresValues(Map("field" -> Some("value")))
    val fixedDimensionsName = Seq("dim2")
    val fixedDimensions = Some(Seq(("dim3", "value3")))
    val fixedMeasures = MeasuresValues(Map("agg2" -> Some("2")))
    val measureEmpty = MeasuresValues(Map())
  }

  it should "return a correct toKeyRow tuple" in new CommonValues {
    val expect = (Some("testCube"), Row("value1", "value2", "value3", new Timestamp(1L), "2", "value"))
    val result = AggregateOperations.toKeyRowWithTime(
      dimensionValuesT, measures, fixedMeasures, fixedDimensions, false, TypeOp.Timestamp)
    result should be(expect)
  }

  it should "return a correct toKeyRow tuple without fixedMeasure" in new CommonValues {
    val expect = (Some("testCube"), Row("value1", "value2", "value3", new Timestamp(1L), "value"))
    val result = AggregateOperations.toKeyRowWithTime(dimensionValuesT,
      measures,
      measureEmpty,
      fixedDimensions,
      false,
      TypeOp.Timestamp)
    result should be(expect)
  }

  it should "return a correct toKeyRow tuple without fixedDimensions" in new CommonValues {
    val expect = (Some("testCube"), Row("value1", "value2", new Timestamp(1L), "2", "value"))
    val result = AggregateOperations.toKeyRowWithTime(
      dimensionValuesT, measures, fixedMeasures, None, false, TypeOp.Timestamp)
    result should be(expect)
  }

  it should "return a correct toKeyRow tuple without fixedDimensions and fixedMeasure" in new CommonValues {
    val expect = (Some("testCube"), Row("value1", "value2", new Timestamp(1L), "value"))
    val result = AggregateOperations.toKeyRowWithTime(
      dimensionValuesT, measures, measureEmpty, None, false, TypeOp.Timestamp)
    result should be(expect)
  }

  it should "return a correct toKeyRow tuple without measures and  fixedDimensions and fixedMeasure" in
    new CommonValues {
      val expect = (Some("testCube"), Row("value1", "value2", new Timestamp(1L)))
      val result = AggregateOperations.toKeyRowWithTime(dimensionValuesT,
        measureEmpty,
        measureEmpty,
        None,
        false,
        TypeOp.Timestamp)
      result should be(expect)
    }

  it should "return a correct toKeyRow tuple without dimensions and aggregations and  fixedDimensions and " +
    "fixedMeasure" in
    new CommonValues {
      val expect = (Some("testCube"), Row(new Timestamp(1L)))
      val result =
        AggregateOperations.toKeyRowWithTime(DimensionValuesTime("testCube",Seq(), timestamp, timeDimension),
          measureEmpty,
          measureEmpty,
          None,
          false,
          TypeOp.Timestamp)
      result should be(expect)
    }

  it should "return a correct sequence of values with aggregations and dimensions" in
    new CommonValues {
      val expect = (Seq("value1", "value2", 1L), Seq("value"))
      val result = AggregateOperations.toSeq(dimensionValuesT.dimensionValues, measures.values)
      result should be(expect)
    }

  it should "return a correct sequence of values with empty aggregations and dimensions" in
    new CommonValues {
      val expect = (Seq("value1", "value2", 1L), Seq())
      val result = AggregateOperations.toSeq(dimensionValuesT.dimensionValues, Map())
      result should be(expect)
    }

  it should "return a correct sequence of values with aggregations and empty dimensions" in
    new CommonValues {
      val expect = (Seq(), Seq("value"))
      val result = AggregateOperations.toSeq(Seq(), measures.values)
      result should be(expect)
    }

  it should "return a correct sequence of values with empty measures and empty dimensions" in
    new CommonValues {
      val expect = (Seq(), Seq())
      val result = AggregateOperations.toSeq(Seq(), Map())
      result should be(expect)
    }

  it should "return a correct names and values without idcalculated" in
    new CommonValues {
      val expect = ("names", Seq())
      val result = AggregateOperations.getNamesValues("names", Seq(), false)
      result should be(expect)
    }

  it should "return a correct names and values with idcalculated" in
    new CommonValues {
      val expect = ("names", Seq(""))
      val result = AggregateOperations.getNamesValues("names", Seq(), true)
      result should be(expect)
    }
}
