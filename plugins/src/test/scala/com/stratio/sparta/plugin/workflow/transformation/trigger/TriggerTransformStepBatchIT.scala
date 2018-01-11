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

package com.stratio.sparta.plugin.workflow.transformation.trigger

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class TriggerTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A TriggerTransformStepBatch" should "make trigger over one RDD" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row]
    )
    val inputRdd = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val query = s"SELECT * FROM step1 ORDER BY step1.color"
    val result = new TriggerTransformStepBatch(
      "dummy",
      outputOptions,
      Option(ssc),
      sparkSession,
      Map("sql" -> query)
    ).transform(inputData)
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.toSeq should be(data1)

    batchEvents should be(2)
  }

  "A TriggerTransformStepBatch" should "make trigger over two RDD" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val schema2 = StructType(Seq(StructField("color", StringType),
      StructField("company", StringType), StructField("name", StringType)))
    val schemaResult = StructType(Seq(StructField("color", StringType),
      StructField("company", StringType), StructField("name", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row]
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val query = s"SELECT step1.color, step2.company, step2.name, step1.price " +
      s"FROM step2 JOIN step1 ON step2.color = step1.color ORDER BY step1.color"
    val result = new TriggerTransformStepBatch(
      "dummy",
      outputOptions,
      Option(ssc),
      sparkSession,
      Map("sql" -> query)
    ).transform(inputData)
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee", 12.1), schemaResult),
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee", 12.2), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(2)
  }
}