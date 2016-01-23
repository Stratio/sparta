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

import com.stratio.sparkta.sdk.test.DimensionTypeMock
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import java.io.{Serializable => JSerializable}
import java.sql.Timestamp

import com.stratio.sparkta.sdk.test.DimensionTypeMock
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner


//@RunWith(classOf[JUnitRunner])
class DimensionValuesTimeTest extends FlatSpec with ShouldMatchers {

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

  "DimensionValuesTime" should "return a correct keyString" in new CommonValues{
//    val expect = "dim1_dim2_minute"
    val expect = "[[ TIME: 1]]"

    val dimensionValuesTime =
      new DimensionValuesTime("cubeName", dimensionValuesT.dimensionValues, timestamp, timeDimension)

    val result = dimensionValuesTime.toString(measures, fixedDimensionsName.toArray)

    result should be(expect)
  }
}

