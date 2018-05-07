/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.split

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SplitTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A SplitTransformStepBatchIT" should "split events the input Dataset" in {
    val subFamily = 333210
    val fields =
      """[
        |{
        |   "name":"sector"
        |},
        |{
        |   "name":"section"
        |},
        |{
        |   "name":"familyGroup"
        |},
        |{
        |   "name":"family"
        |},
        |{
        |   "name":"subFamily"
        |}]
        |""".stripMargin

    val inputField = "split"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(
      StructField("sector", StringType),
      StructField("section", StringType),
      StructField("familyGroup", StringType),
      StructField("family", StringType),
      StructField("subFamily", StringType)
    ))
    val dataIn = Seq(new GenericRowWithSchema(Array(subFamily), inputSchema))
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(new GenericRowWithSchema(Array("3", "33", "2", "1", "0"), outputSchema))
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val listIndexes = "1,3,4,5"
    val result = new SplitTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("splitMethod" -> "BYINDEX",
        "byIndexPattern" -> listIndexes,
        "excludeIndexes" -> JsoneyString.apply("false"),
        "inputField" -> inputField,
        "schema.fields" -> fields.asInstanceOf[JSerializable],
        "schema.inputMode" -> "FIELDS"
      )
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
          assert(dataOut.contains(row))
          assert(outputSchema == row.schema)
        }

    assert(arrayValues.length === 1)
  }

  "A SplitTransformStepBatchIT" should "discard rows" in {
    val subFamily = 333210
    val fields =
      """[
        |{
        |   "name":"sector"
        |},
        |{
        |   "name":"section"
        |},
        |{
        |   "name":"familyGroup"
        |},
        |{
        |   "name":"family"
        |},
        |{
        |   "name":"subFamily"
        |}]
        |""".stripMargin

    val inputField = "split"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(
      StructField("sector", StringType),
      StructField("section", StringType),
      StructField("familyGroup", StringType),
      StructField("family", StringType),
      StructField("subFamily", StringType)
    ))
    val dataIn = Seq(
      new GenericRowWithSchema(Array(subFamily), inputSchema),
      new GenericRowWithSchema(Array(1), inputSchema)
    )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(new GenericRowWithSchema(Array("3", "33", "2", "1", "0"), outputSchema))
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val listIndexes = "1,3,4,5"
    val result = new SplitTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("splitMethod" -> "BYINDEX",
        "byIndexPattern" -> listIndexes,
        "excludeIndexes" -> JsoneyString.apply("false"),
        "inputField" -> inputField,
        "schema.fields" -> fields.asInstanceOf[JSerializable],
        "schema.inputMode" -> "FIELDS",
        "whenRowError" -> "RowDiscard"
      )
    ).transformWithDiscards(inputData)

    val validData = result._1.ds.collect()
    val discardedData = result._3.get.ds.collect()

    validData.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    discardedData.foreach { row =>
      assert(dataIn.contains(row))
      assert(inputSchema == row.schema)
    }

    assert(validData.length === 1)
    assert(discardedData.length === 1)
  }
}