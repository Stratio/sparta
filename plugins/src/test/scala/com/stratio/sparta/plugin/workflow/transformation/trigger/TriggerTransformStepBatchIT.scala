/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.trigger

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.sdk.enumerators.SaveModeEnum

@RunWith(classOf[JUnitRunner])
class TriggerTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A TriggerTransformStepBatch" should "make trigger over one RDD" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.2), schema1).asInstanceOf[Row]
    )
    val inputRdd = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val query = s"SELECT * FROM step1 WHERE price > 12 ORDER BY step1.color"
    val inputSchema = """[{"stepName":"step1","schema":"{\"color\":\"1\",\"price\":15.5}"}]"""
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
    val result = new TriggerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("sql" -> query, "inputSchemas" -> JsoneyString(inputSchema), "discardConditions" -> discardConditions)
    ).transformWithDiscards(inputData)

    //Test filtered events
    val triggerData = result._1
    val streamingEvents = triggerData.ds.count()
    val streamingRegisters = triggerData.ds.collect()

    if (streamingRegisters.nonEmpty)
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
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val query = s"SELECT step1.color, step2.company, step2.name, step1.price " +
      s"FROM step2 JOIN step1 ON step2.color = step1.color ORDER BY step1.color"
    val result = new TriggerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("sql" -> query)
    ).transformWithDiscards(inputData)._1
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

  "A TriggerTransformStepBatch" should "make trigger over two RDD one empty" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.emptyRDD[Row]
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val query = s"SELECT step1.color, step2.company, step2.name, step1.price " +
      s"FROM step2 JOIN step1 ON step2.color = step1.color ORDER BY step1.color"
    val result = new TriggerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("sql" -> query, "executeSqlWhenEmpty" -> "false")
    ).transformWithDiscards(inputData)._1
    val batchEvents = result.ds.count()

    batchEvents should be(0)
  }

  "A TriggerTransformStepBatch" should "make trigger over one RDD empty" in {
    val inputRdd1 = sc.emptyRDD[Row]
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val query = s"SELECT step1.color, step2.company, step2.name, step1.price " +
      s"FROM step2 JOIN step1 ON step2.color = step1.color ORDER BY step1.color"
    val result = new TriggerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("sql" -> query, "executeSqlWhenEmpty" -> "false")
    ).transformWithDiscards(inputData)._1
    val batchEvents = result.ds.count()

    batchEvents should be(0)
  }

  "A TriggerTransformStepBatch" should "make trigger over two RDD one empty, but executes the query" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.emptyRDD[Row]
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val query = s"SELECT step1.color FROM step1"
    val result = new TriggerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("sql" -> query, "executeSqlWhenEmpty" -> "true")
    ).transformWithDiscards(inputData)._1
    val batchEvents = result.ds.count()

    batchEvents should be(2)
  }

}