/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */


package com.stratio.sparta.plugin.workflow.transformation.column


import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.column.CaseLetter.CaseLetterTransformStepBatch
import com.stratio.sparta.plugin.workflow.transformation.column.CommaDelimiter.CommaDelimiterTransformStepBatch
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.scalatest.Matchers

class CommaDelimiterTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CommaDelimiterTransformStepBatchIT" should "Include a decimal separator at a selected position from the end of the data" in {

    val fields =
      """[{"name": "Column1",
        |"decimalLength": "3",
        |"decimalSeparator": ".",
        |"displacementDirection": "COMMA_LEFT"
        |},
        |{
        |"name": "Column2",
        |"decimalLength": "1",
        |"decimalSeparator": ",",
        |"displacementDirection": "COMMA_RIGHT"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("333", "333"), inputSchema),
        new GenericRowWithSchema(Array("555", "555"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("0.333", "3330,0"), outputSchema),
      new GenericRowWithSchema(Array("0.555", "5550,0"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CommaDelimiterTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToDelimit" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }


}
