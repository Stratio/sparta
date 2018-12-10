/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.repartition

import java.io.{Serializable => JSerializable}

import akka.http.scaladsl.server.util.Tuple
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable


@RunWith(classOf[JUnitRunner])
class RepartitionTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A RepartitionTransformStepStreamingIT" should "repartition RDD" in {
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val inputSchema = StructType(Seq(StructField("color", StringType), StructField("age", StringType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array("red", "12"), inputSchema),
      new GenericRowWithSchema(Array("blue", "12"), inputSchema),
      new GenericRowWithSchema(Array("red", "32"), inputSchema),
      new GenericRowWithSchema(Array("red", "42"), inputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)

    val result = new RepartitionTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("partitions" -> "10", "columns" ->
        """[
          |{
          |   "name": "age"
          |}
          |]""".stripMargin)
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.longAccumulator("Number of events received")
    var allSame: Boolean = false

    result.ds.foreachRDD { rdd =>
      val streamingEvents = rdd.partitions.length
      totalEvents.add(streamingEvents)

      val mapped = rdd.mapPartitionsWithIndex { case (index, iterator) =>
        val myList = iterator.toList
        myList.map(x => x + "-> " + index).iterator
      }
      val listWithSameAges = mapped.collect().filter(x => x.contains("12")).map(_.takeRight(1))
      allSame = listWithSameAges.forall(_ == listWithSameAges.head)
    }

    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value==10L)
    allSame shouldBe true
  }
}