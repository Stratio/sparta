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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}
import java.sql.Timestamp

import com.stratio.sparkta.sdk.test.DimensionTypeTest
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AggregateOperationsSpec extends FlatSpec with ShouldMatchers {

  trait CommonValues {

    val timeDimension = "minute"
    val timestamp = 1L
    val defaultDimension = new DimensionTypeTest(Map())
    val dimensionValuesT = DimensionValuesTime(Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("dim2", "eventKey", "identity", defaultDimension), "value2"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)),
      timestamp, timeDimension)
    val aggregations = Map("field" -> Some("value"))
    val fixedDimensionsName = Seq("dim2")
    val fixedDimensions = Some(Seq(("dim3", "value3")))
    val fixedAggregation = Map("agg2" -> Some("2"))
  }

  "AggregateOperationsSpec" should "return a correct string" in new CommonValues {
    val result = "dim1_dim2_minute DIMENSIONS: dim1|dim2|minute AGGREGATIONS: Map(field -> Some(value)) TIME: 1"
    AggregateOperations.toString(dimensionValuesT, aggregations, timeDimension, fixedDimensionsName) should be(result)
  }

  it should "return a correct keyString" in new CommonValues {
    val result = "dim1_dim2_minute"
    AggregateOperations.keyString(dimensionValuesT, timeDimension, fixedDimensionsName) should be(result)
  }

  it should "return a correct toKeyRow tuple" in new CommonValues {
    val result = (Some("dim1_dim2_dim3_minute"), Row("value1", "value2", "value3", new Timestamp(1L), "2", "value"))
    AggregateOperations.toKeyRow(
      dimensionValuesT, aggregations, fixedAggregation, fixedDimensions, false) should be(result)
  }

  it should "return a correct toKeyRow tuple without fixedAggregation" in new CommonValues {
    val result = (Some("dim1_dim2_dim3_minute"), Row("value1", "value2", "value3", new Timestamp(1L), "value"))
    AggregateOperations.toKeyRow(
      dimensionValuesT, aggregations, Map(), fixedDimensions, false) should be(result)
  }

  it should "return a correct toKeyRow tuple without fixedDimensions" in new CommonValues {
    val result = (Some("dim1_dim2_minute"), Row("value1", "value2", new Timestamp(1L), "2", "value"))
    AggregateOperations.toKeyRow(
      dimensionValuesT, aggregations, fixedAggregation, None, false) should be(result)
  }

  it should "return a correct toKeyRow tuple without fixedDimensions and fixedAggregation" in new CommonValues {
    val result = (Some("dim1_dim2_minute"), Row("value1", "value2", new Timestamp(1L), "value"))
    AggregateOperations.toKeyRow(
      dimensionValuesT, aggregations, Map(), None, false) should be(result)
  }

  it should "return a correct toKeyRow tuple without aggregations and  fixedDimensions and fixedAggregation" in
    new CommonValues {
      val result = (Some("dim1_dim2_minute"), Row("value1", "value2", new Timestamp(1L)))
      AggregateOperations.toKeyRow(
        dimensionValuesT, Map(), Map(), None, false) should be(result)
    }

  it should "return a correct toKeyRow tuple without dimensions and aggregations and  fixedDimensions and " +
    "fixedAggregation" in
    new CommonValues {
      val result = (Some("minute"), Row(new Timestamp(1L)))
      AggregateOperations.toKeyRow(
        DimensionValuesTime(Seq(), timestamp, timeDimension), Map(), Map(), None, false) should be(result)
    }

  it should "return a correct sequence of values with aggregations and dimensions" in
    new CommonValues {
      val result = (Seq("value1", "value2", 1L), Seq("value"))
      AggregateOperations.toSeq(
        dimensionValuesT.dimensionValues, aggregations) should be(result)
    }

  it should "return a correct sequence of values with empty aggregations and dimensions" in
    new CommonValues {
      val result = (Seq("value1", "value2", 1L), Seq())
      AggregateOperations.toSeq(dimensionValuesT.dimensionValues, Map()) should be(result)
    }

  it should "return a correct sequence of values with aggregations and empty dimensions" in
    new CommonValues {
      val result =
        (Seq(), Seq("value"))
      AggregateOperations.toSeq(Seq(), aggregations) should be(result)
    }

  it should "return a correct sequence of values with empty aggregations and empty dimensions" in
    new CommonValues {
      val result = (Seq(), Seq())
      AggregateOperations.toSeq(Seq(), Map()) should be(result)
    }

  it should "return a correct names and values without idcalculated" in
    new CommonValues {
      val result = (Seq(), Seq())
      AggregateOperations.getNamesValues(Seq(), Seq(), false) should be(result)
    }

  it should "return a correct names and values with idcalculated" in
    new CommonValues {
      val result = (Seq("id"), Seq(""))
      AggregateOperations.getNamesValues(Seq(), Seq(), true) should be(result)
    }

}
