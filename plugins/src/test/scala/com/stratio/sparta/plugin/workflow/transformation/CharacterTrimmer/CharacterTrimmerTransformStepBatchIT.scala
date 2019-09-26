/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.CharacterTrimmer

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.column.CharacterTrimmer.CharacterTrimmerTransformStepBatch
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


//scalastyle:off
@RunWith(classOf[JUnitRunner])
class CharacterTrimmerTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CharacterTrimmerTransformStepBatchIT" should "Remove selected characters into the left" in {

    val fields =
      """[{"name": "Column1",
        |"characterToTrim": "b",
        |"trimType": "TRIM_LEFT"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("baae"), inputSchema),
        new GenericRowWithSchema(Array("booe"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("aae"), outputSchema),
      new GenericRowWithSchema(Array("ooe"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CharacterTrimmerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }


  "A CharacterTrimmerTransformStepBatchIT" should "Remove selected characters and whitespaces into the right" in {

    val fields =
      """[{"name": "Column1",
        |"characterToTrim": "$",
        |"trimType": "TRIM_RIGHT"
        |},
        |{"name": "Column2",
        |"characterToTrim": " ",
        |"trimType": "TRIM_RIGHT"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("baae$", "whitespace$"), inputSchema),
        new GenericRowWithSchema(Array("boae", "whitespace "), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("baae", "whitespace$"), outputSchema),
      new GenericRowWithSchema(Array("boae", "whitespace"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CharacterTrimmerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }

  "A CharacterTrimmerTransformStepBatchIT" should "Remove selected characters into the left and right" in {

    val fields =
      """[{"name": "Column1",
        |"characterToTrim": "e",
        |"trimType": "TRIM_BOTH"
        |},
        |{"name": "Column2",
        |"characterToTrim": " ",
        |"trimType": "TRIM_BOTH"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType),StructField("Column2", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("ebe", " whitespace "), inputSchema),
        new GenericRowWithSchema(Array("ebe", "   whitespace  "), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("b", "whitespace"), outputSchema),
      new GenericRowWithSchema(Array("b", "  whitespace "), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CharacterTrimmerTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }



}
