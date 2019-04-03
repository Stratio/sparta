/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.binary

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.plugin.TemporalSparkContext
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BinaryInputStepBatchTestIT extends TemporalSparkContext with Matchers {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName")
  val resourcePath = getClass().getResource("/test.binary")

  "Events in binary file" should "match the number of events and the content" in {
    val properties = Map(
      "path" -> s"$resourcePath",
      "parallelism" -> "1"
    )
    val input = new BinaryInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 1
  }
}