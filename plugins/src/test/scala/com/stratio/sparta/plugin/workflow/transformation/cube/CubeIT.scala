/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.cube.operators.CountOperator
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.sdk.enumerators.{WhenError, WhenFieldError, WhenRowError}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable


@RunWith(classOf[JUnitRunner])
class CubeIT extends TemporalSparkContext with Matchers {

  override val batchWindow = 1000

  "Cube" should "create stream" in {

    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("dim2", IntegerType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val dimensions = Seq(Dimension("dim1"))
    val operators = Seq(new CountOperator("count1", WhenRowError.RowError, WhenFieldError.FieldError))
    val cube = new Cube(dimensions, operators)
    val fields = new GenericRowWithSchema(Array("foo", 1, 2), initSchema)
    val rdd = sc.parallelize(Seq(fields))
    val elements = new mutable.Queue[RDD[GenericRowWithSchema]]()
    elements += rdd
    val resultDStream = cube.createDStream(ssc.queueStream(elements).asInstanceOf[DStream[Row]])

    resultDStream.foreachRDD{rdd =>
      val cubeData = rdd.collect()

      cubeData.length should be(1)

      cubeData.head._1 should be(DimensionValues(Seq(
        DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = false))
      )))

      cubeData.head._2 should be(InputFields(fields, 1))
    }

    ssc.start()
    ssc.awaitTerminationOrTimeout(1000L)
    ssc.stop()
  }
}
