/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.crossdata

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.models.OutputOptions
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
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
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val crossdataInput = new CrossdataInputStepBatch(
      "crossdata", outputOptions, Option(ssc), sparkSession, datasourceParams)
    val inputRdd = crossdataInput.initWithSchema()._1
    val batchEvents = inputRdd.ds.count()
    val batchRegisters = inputRdd.ds.collect()

    assert(batchRegisters === registers)
    assert(batchEvents === totalRegisters.toLong)
  }
}

