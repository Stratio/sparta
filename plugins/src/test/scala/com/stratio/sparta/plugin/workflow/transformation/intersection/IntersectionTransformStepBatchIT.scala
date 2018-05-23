/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.intersection

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IntersectionTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A IntersectionTransformStepBatch" should "intersect RDD" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("black", 12.3), schema).asInstanceOf[Row]
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val rdd1 = sc.parallelize(data1)
    val rdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> rdd1, "step2" -> rdd2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val discardConditions =
      """[
        |{
        |   "previousField":"color",
        |   "transformedField":"color"
        |},
        |{
        |   "previousField":"price",
        |   "transformedField":"price"
        |}
        |]
        | """.stripMargin
    val result = new IntersectionTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("discardConditions" -> discardConditions)
    ).transformWithDiscards(inputData)

    //Test distinct events
    val intersectionData = result._1
    val streamingEvents = intersectionData.ds.count()
    val streamingRegisters = intersectionData.ds.collect()

    if (!intersectionData.ds.isEmpty())
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)

    //Test discarded events
    val discardedData = result._3.get
    val discardedEvents = discardedData.ds.count()
    val discardedRegisters = discardedData.ds.collect()

    if (discardedRegisters.nonEmpty)
      discardedRegisters.foreach(row => assert(data1.contains(row)))

    assert(discardedEvents === 1)

  }
}