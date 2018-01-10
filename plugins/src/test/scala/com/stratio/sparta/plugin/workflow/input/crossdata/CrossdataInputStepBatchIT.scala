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

package com.stratio.sparta.plugin.workflow.input.crossdata

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CrossdataInputStepBatchIT extends TemporalSparkContext with Matchers {

  "CrossdataInput " should "read all the records in one batch" in {

    SparkSession.clearActiveSession()

    val schema = new StructType(Array(
      StructField("id", IntegerType, nullable = true),
      StructField("id2", IntegerType, nullable = true)
    ))
    val tableName = "tableName"
    val totalRegisters = 1000
    val registers = for (a <- 1 to totalRegisters) yield Row(a,a)
    val rdd = sc.parallelize(registers)

    sparkSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName)

    val datasourceParams = Map("query" -> s"select * from $tableName")
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val crossdataInput = new CrossdataInputStepBatch(
      "crossdata", outputOptions, Option(ssc), sparkSession, datasourceParams)
    val inputRdd = crossdataInput.init()
    val batchEvents = inputRdd.ds.count()
    val batchRegisters = inputRdd.ds.collect()

    assert(batchRegisters === registers)
    assert(batchEvents === totalRegisters.toLong)
  }
}

