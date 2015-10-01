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

import com.stratio.sparkta.sdk.test.OutputTest
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class OutputSpec extends WordSpec with Matchers {

  "Output" should {

    val tableSchema = TableSchema("outputName", "dim1_dim2", StructType(Array(
      StructField("dim1", StringType, false),
      StructField("dim2", StringType, false),
      StructField("minute", DateType, false),
      StructField("op1", LongType, true))), "minute")
    val outputName = "outName"

    val output = new OutputTest(outputName,
      Map(),
      Some(Map("op1" -> (WriteOp.Set, TypeOp.Long))),
      Some(Seq(tableSchema)))

    "Name must be " in {

      val expected = outputName

      val result = output.getName

      result should be equals Some(expected)
    }
  }
}
