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
package com.stratio.sparta.sdk

import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class TableSchemaTest extends WordSpec with Matchers {

  "TableSchema" should {

    val tableSchema = TableSchema(Seq("outputName"), "myCube", StructType(Array(
      StructField("dim1", StringType, false))), Some("minute"))

    "toString must be " in {
      val expected = "TableSchema(List(outputName),myCube,StructType(StructField(dim1,StringType,false)),Some(minute)" +
        ",Timestamp,List())"
      val result = tableSchema.toString
      result should be(expected)
    }
  }
}
