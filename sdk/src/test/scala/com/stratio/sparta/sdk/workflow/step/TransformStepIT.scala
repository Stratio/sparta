/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.{DistributedMonad, TemporalSparkContext}
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class TransformStepIT extends TemporalSparkContext with Matchers {

  import DistributedMonad.Implicits._

  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
  val transformationsStepManagement = TransformationStepManagement()
  def passSameStream(
                      stepName: String,
                      generateDStream: DistributedMonad[DStream]
                    ): DistributedMonad[DStream] = generateDStream

  "TransformStep" should "applyHeadTransform return exception with no input data" in {
    val name = "transform"
    val schema = StructType(Seq(
      StructField("inputField", StringType),
      StructField("color", StringType),
      StructField("price", DoubleType)
    ))
    val inputSchemas = Map("input" -> schema)
    val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Error")
    val outputsFields = Seq(OutputFields("inputField", "string"), OutputFields("price", "string"))
    val transformStep = new MockTransformStep(
      name,
      outputOptions,
      transformationsStepManagement,
      Option(ssc),
      sparkSession,
      properties
    )

    an[AssertionError] should be thrownBy transformStep.applyHeadTransform(
      Map.empty[String, DistributedMonad[DStream]]
    )(passSameStream)

  }

  "TransformStep" should "applyHeadTransform return DStream with empty rdd" in {
    val name = "transform"
    val schema = StructType(Seq(
      StructField("inputField", StringType),
      StructField("color", StringType),
      StructField("price", DoubleType)
    ))
    val inputSchemas = Map("input" -> schema)
    val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Error")
    val outputsFields = Seq(OutputFields("inputField", "string"), OutputFields("price", "string"))
    val transformStep = new MockTransformStep(
      name,
      outputOptions,
      transformationsStepManagement,
      Option(ssc),
      sparkSession,
      properties
    )
    val rddQueue = new mutable.Queue[RDD[Row]]
    val rddData = sc.emptyRDD[Row]
    rddQueue += rddData
    val inputDStream = ssc.queueStream(rddQueue)
    val inputData = Map("input" -> inputDStream)

    val result = transformStep.applyHeadTransform[DStream](inputData)(passSameStream)
    val expected = sc.emptyRDD[Row].collect()

    result.ds.foreachRDD(rdd =>
      rdd.collect() should be(expected)
    )
  }

  "TransformStep" should "applyHeadTransform return DStream with output fields data" in {
    val name = "transform"
    val schema = StructType(Seq(
      StructField("inputField", StringType),
      StructField("color", StringType),
      StructField("price", DoubleType)
    ))
    val inputSchemas = Map("input" -> schema)
    val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Error")
    val outputsFields = Seq(OutputFields("inputField", "string"), OutputFields("price", "string"))
    val transformStep = new MockTransformStep(
      name,
      outputOptions,
      transformationsStepManagement,
      Option(ssc),
      sparkSession,
      properties
    )

    val rddQueue = new mutable.Queue[RDD[Row]]
    val rddData = sc.parallelize(Seq(Row.fromSeq(Seq("inputfield", "blue", 12.1))))
    rddQueue += rddData
    val inputDStream = ssc.queueStream(rddQueue)
    val inputData = Map("input" -> inputDStream)

    val result = transformStep.applyHeadTransform[DStream](inputData)(passSameStream)
    val expected = sc.parallelize(Seq(Row.fromSeq(Seq("inputfield", "12.1")))).collect()

    result.ds.foreachRDD(rdd =>
      rdd.collect() should be(expected)
    )
  }
}
