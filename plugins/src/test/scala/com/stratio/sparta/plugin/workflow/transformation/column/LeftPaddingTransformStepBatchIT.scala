/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.column.LeftPadding.LeftPaddingTransformStepBatch
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class LeftPaddingTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A LeftPaddingTransformStepBatchIT" should "Prepend a character given a length and a value to place." in {

    val fields =
      """[{"name": "Column1",
        |"lengthPaddingLeft": "4",
        |"characterFill": "0"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("5"), inputSchema),
        new GenericRowWithSchema(Array("22"), inputSchema),
        new GenericRowWithSchema(Array("8"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("0005"), outputSchema),
      new GenericRowWithSchema(Array("0022"), outputSchema),
      new GenericRowWithSchema(Array("0008"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new LeftPaddingTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToLeftPadding" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 3)
  }

  "A LeftPaddingTransformStepBatchIT" should "Prepend a character given a length and a value to place into multiple columns" in {

    val fields =
      """[{"name": "Column1",
        |"lengthPaddingLeft": "4",
        |"characterFill": "0"
        |},
        |{"name": "Column2",
        |"lengthPaddingLeft": "5",
        |"characterFill": "0"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("5", "999"), inputSchema),
        new GenericRowWithSchema(Array("22", "333"), inputSchema),
        new GenericRowWithSchema(Array("8", "1"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("0005", "00999"), outputSchema),
      new GenericRowWithSchema(Array("0022", "00333"), outputSchema),
      new GenericRowWithSchema(Array("0008", "00001"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new LeftPaddingTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToLeftPadding" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 3)
  }

}


