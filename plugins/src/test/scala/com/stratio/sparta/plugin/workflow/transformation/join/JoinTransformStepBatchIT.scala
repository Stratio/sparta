/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.join

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JoinTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  val schema1 = StructType(Seq(
    StructField("color", StringType),
    StructField("price", DoubleType))
  )
  val schema2 = StructType(Seq(
    StructField("color", StringType),
    StructField("company", StringType),
    StructField("name", StringType))
  )
  val schemaResult = StructType(Seq(
    StructField("color", StringType),
    StructField("company", StringType),
    StructField("name", StringType),
    StructField("price", DoubleType)
  ))
  val data1 = Seq(
    new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
    new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row]
  )
  val data2 = Seq(
    new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
    new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
  )
  val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
  val conditions =
    s"""[
       |{
       |   "leftField":"color",
       |   "rightField":"color"
       |}]
       | """.stripMargin

  "A TriggerTransformStepBatch" should "make INNER join over two RDD" in {
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "all",
        "joinType" -> "INNER",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
      new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(2)
  }

  "A TriggerTransformStepBatch" should "make INNER join over two RDD selecting left fields" in {
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "left",
        "joinType" -> "INNER",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schemaResult),
      new GenericRowWithSchema(Array("red", 12.2), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(2)
  }

  "A TriggerTransformStepBatch" should "make INNER join over two RDD selecting right fields" in {
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "right",
        "joinType" -> "INNER",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schemaResult),
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(2)
  }

  "A TriggerTransformStepBatch" should "make INNER join over two RDD selecting columns" in {
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val schemaResult = StructType(Seq(
      StructField("color2", StringType)
    ))
    val columns =
      s"""[
         |{
         |   "tableSide":"LEFT",
         |   "column":"color",
         |   "alias": "color2"
         |}]
         | """.stripMargin
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "COLUMNS",
        "joinType" -> "INNER",
        "joinReturnColumns" -> columns,
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue"), schemaResult),
      new GenericRowWithSchema(Array("red"), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach { row =>
      queryData.contains(row) should be(true)
      row.schema should be(schemaResult)
    }

    batchEvents should be(2)
  }

  "A TriggerTransformStepBatch" should "make LEFT join over two RDD" in {
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "all",
        "joinType" -> "LEFT",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
      new GenericRowWithSchema(Array("yellow", 12.3, null, null, null), schemaResult),
      new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(3)
  }

  "A TriggerTransformStepBatch" should "make RIGHT join over two RDD" in {
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "all",
        "joinType" -> "RIGHT",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
      new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(2)
  }

  "A TriggerTransformStepBatch" should "make RIGHT_ONLY join over two RDD" in {
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "RIGHT_ONLY",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(new GenericRowWithSchema(Array("yellow", "Stratio", "Stratio employee"), schema2))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(1)
  }

  "A TriggerTransformStepBatch" should "make LEFT_ONLY join over two RDD" in {
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "LEFT_ONLY",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row])
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(1)
  }

  "A TriggerTransformStepBatch" should "make LEFT_RIGHT_ONLY join over two RDD" in {
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("brown", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "LEFT_RIGHT_ONLY",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("yellow", 12.3, null, null, null), schemaResult),
      new GenericRowWithSchema(Array(null, null, "brown", "Stratio", "Stratio employee"), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(2)
  }

  "A TriggerTransformStepBatch" should "make FULL join over two RDD" in {
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("brown", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "FULL",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
      new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult),
      new GenericRowWithSchema(Array("yellow", 12.3, null, null, null), schemaResult),
      new GenericRowWithSchema(Array(null, null, "brown", "Stratio", "Stratio employee"), schemaResult))
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(4)
  }

  "A TriggerTransformStepBatch" should "make CROSS join over two RDD" in {
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val result = new JoinTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "CROSS",
        "joinConditions" -> conditions
      )
    ).transformWithDiscards(inputData)._1
    val queryData = Seq(
      new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
      new GenericRowWithSchema(Array("blue", 12.1, "red", "Paradigma", "Paradigma employee"), schemaResult),
      new GenericRowWithSchema(Array("red", 12.2, "blue", "Stratio", "Stratio employee"), schemaResult),
      new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult)
    )
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    batchRegisters.foreach(row =>
      queryData.contains(row) should be(true)
    )

    batchEvents should be(4)
  }

}