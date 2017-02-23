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
package com.stratio.sparta.sdk.pipeline.output

import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, DimensionTypeMock, DimensionValue, DimensionValuesTime}
import com.stratio.sparta.sdk.pipeline.transformation.OutputMock
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class OutputTest extends WordSpec with Matchers {

  trait CommonValues {
    val timeDimension = "minute"
    val tableName = "table"
    val timestamp = 1L
    val defaultDimension = new DimensionTypeMock(Map())
    val dimensionValuesT = DimensionValuesTime("testCube", Seq(
      DimensionValue(Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(Dimension("dim2", "eventKey", "identity", defaultDimension), "value2"),
      DimensionValue(Dimension("minute", "eventKey", "identity", defaultDimension), 1L)))
    val dimensionValuesTFixed = DimensionValuesTime("testCube", Seq(
      DimensionValue(Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(Dimension("minute", "eventKey", "identity", defaultDimension), 1L)))
    val outputName = "outputName"
    val output = new OutputMock(outputName, Map())
    val outputOperation = new OutputMock(outputName, Map())
    val outputProps = new OutputMock(outputName, Map())
  }

  "Output" should {

    "Name must be " in new CommonValues {
      val expected = outputName
      val result = output.name
      result should be(expected)
    }

    "the spark geo field returned must be " in new CommonValues {
      val expected = StructField("field", ArrayType(DoubleType), false)
      val result = Output.defaultGeoField("field", false)
      result should be(expected)
    }

    "classSuffix must be " in {
      val expected = "Output"
      val result = Output.ClassSuffix
      result should be(expected)
    }
  }
}
