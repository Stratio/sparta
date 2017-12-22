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

import java.io.{Serializable => JSerializable}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputFields, OutputOptions}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CrossdataInputStepIT extends TemporalSparkContext with Matchers {

  "CrossdataInput " should "read all the records in one streaming batch" in {
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

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    val offsetFields =
      """[
        |{
        |"offsetField":"id",
        |"offsetOperator":">",
        |"offsetValue": "500"
        |},
        |{
        |"offsetField":"id2",
        |"offsetOperator":">",
        |"offsetValue": "750"
        |}
        |]
      """.stripMargin

    val datasourceParams = Map(
      "query" -> s"select * from $tableName",
      "rememberDuration" -> "20000",
      "offsetFields" -> offsetFields.asInstanceOf[JSerializable]
    )

    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val crossdataInput = new CrossdataInputStep(
      "crossdata", outputOptions, Option(ssc), sparkSession, datasourceParams)
    val inputStream = crossdataInput.init

    inputStream.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        assert(streamingRegisters === registers.reverse)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(3000L)
    ssc.stop()

    assert(totalEvents.value === (totalRegisters.toLong/4))
  }
}

