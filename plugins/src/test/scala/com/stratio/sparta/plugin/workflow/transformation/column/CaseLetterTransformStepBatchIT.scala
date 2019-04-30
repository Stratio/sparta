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
import com.stratio.sparta.plugin.workflow.transformation.column.CaseLetter.CaseLetterTransformStepBatch
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.scalatest.Matchers

class CaseLetterTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CaseLetterTransformStepBatchIT" should "Convert all letters to uppercase in one column and lowercase in the other" in {

    val fields =
      """[{"name": "Column1",
        |"operationCaseLetter": "LOWERCASE"
        |},
        |{
        |"name": "Column2",
        |"operationCaseLetter": "UPPERCASE"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("A", "leonidas"), inputSchema),
        new GenericRowWithSchema(Array("B", "sparta"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("a", "LEONIDAS"), outputSchema),
      new GenericRowWithSchema(Array("b", "SPARTA"), outputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CaseLetterTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToCaseLetter" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }


}
