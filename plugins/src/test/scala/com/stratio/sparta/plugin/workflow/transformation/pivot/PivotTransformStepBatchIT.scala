/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.pivot

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class PivotTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A PivotTransformStepBatch for input mode column" should "transform the input RDD" in {

    val inputSchema = StructType(Seq(StructField("A", StringType), StructField("B", StringType), StructField("C", StringType),
      StructField("D", DoubleType)))

    val dataIn = Seq(
      new GenericRowWithSchema(Array("foo", "one", "small", 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "large", 4.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "small", 5.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "small", 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "large", 7.0), inputSchema).asInstanceOf[Row]
    )

    val dataOut = Seq(
      new GenericRowWithSchema(Array("foo", "one", 4.0, 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", null, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", 7.0, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", 4.0, 5.0), inputSchema).asInstanceOf[Row]
    )

    val rddInput: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val transformationsStepManagement = TransformationStepManagement()

    val propertiesWithColumn = Map(
      "selectType" -> "COLUMNS",
      "columns" ->
        """[
          |{
          |   "name": "A"
          |   },
          |{
          |   "name": "B"
          |   },
          |{
          |   "name": "C"
          |   },
          |{
          |   "name": "D"
          |   }
          |   ]""".stripMargin.asInstanceOf[JSerializable],
      "groupByColumns" ->
        """[
          |{
          |   "name": "A"
          |   },
          |{
          |   "name": "B"
          |   }
          |   ]""".stripMargin.asInstanceOf[JSerializable],
      "pivotColumn" -> "C",
      "aggOperations" ->
        """[
          |   {
          |   "operationType": "sum",
          |   "name": "D"
          |   }
          |]""".stripMargin.asInstanceOf[JSerializable]
    )

    val result = new PivotTransformStepBatch(
      "dummy",
      outputOptions,
      transformationsStepManagement,
      Option(ssc),
      sparkSession,
      propertiesWithColumn
    ).transformWithDiscards(inputData)._1
    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
    }

    assert(arrayValues.length === dataOut.size)
  }

  "A PivotTransformStepBatch for input mode expression" should "transform the input RDD" in {

    val inputSchema = StructType(Seq(StructField("A", StringType), StructField("B", StringType), StructField("C", StringType),
      StructField("D", DoubleType)))

    val dataIn = Seq(
      new GenericRowWithSchema(Array("foo", "one", "small", 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "large", 4.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "small", 5.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "small", 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "large", 7.0), inputSchema).asInstanceOf[Row]
    )

    val dataOut = Seq(
      new GenericRowWithSchema(Array("foo", "one", 4.0, 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", null, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", 7.0, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", 4.0, 5.0), inputSchema).asInstanceOf[Row]
    )

    val rddInput: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val transformationsStepManagement = TransformationStepManagement()

    val propertiesWithExp = Map(
      "selectType" -> "EXPRESSION",
      "pivotColumn" -> " C",
      "selectExp" -> "  A, B, C, D",
      "groupByExp" -> "A,    B",
      "aggregateExp" -> "   sum( D)"
    )

    val result = new PivotTransformStepBatch(
      "dummy",
      outputOptions,
      transformationsStepManagement,
      Option(ssc),
      sparkSession,
      propertiesWithExp
    ).transformWithDiscards(inputData)._1
    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
    }

    assert(arrayValues.length === dataOut.size)
  }
}