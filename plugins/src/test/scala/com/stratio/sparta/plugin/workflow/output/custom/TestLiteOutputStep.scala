/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.custom

import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.sdk.lite.common.LiteCustomOutput
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.Matchers


object TestLiteOutputStep {
  val PrimaryKeyValue = "pkeyValue"
  val TableValue = "tableValue"
  val PartitionByKeyValue = "partvalue"
}

class TestLiteOutputStep(
                            xdSession: SparkSession,
                            properties: Map[String, String]
                            )
  extends LiteCustomOutput(xdSession, properties) with Matchers{

  import TestLiteOutputStep._

  override def save(data: DataFrame, saveMode: String, saveOptions: Map[String, String]): Unit = {
    saveOptions.get(PrimaryKey) shouldBe Some(PrimaryKeyValue)
    saveOptions.get(TableNameKey) shouldBe Some(TableValue)
    saveOptions.get(PartitionByKey) shouldBe Some(PartitionByKeyValue)
  }
}