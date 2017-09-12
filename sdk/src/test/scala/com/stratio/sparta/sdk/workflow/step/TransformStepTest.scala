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

package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class TransformStepTest extends WordSpec with Matchers with MockitoSugar {

  val sparkSession = mock[XDSession]
  val ssc = mock[StreamingContext]

  "TransformStep" should {
    val name = "transform"
    val schema = StructType(Seq(StructField("inputField", StringType)))
    val inputSchemas = Map("input" -> schema)
    val outputsFields = Seq(OutputFields("color", "string"), OutputFields("price", "double"))
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val properties = Map("addAllInputFields" -> true.asInstanceOf[Serializable])


    "Transform classSuffix must be corrected" in {
      val expected = "TransformStep"
      val result = TransformStep.ClassSuffix
      result should be(expected)
    }
  }

}
