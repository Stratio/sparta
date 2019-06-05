/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column


import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.catalog.CatalogTransformStepBatch
import com.stratio.sparta.plugin.workflow.transformation.column.PartialInfo.PartialInfoTransformStepBatch
import com.stratio.sparta.plugin.workflow.transformation.duplicateColumns.DuplicateColumnsTransformStepBatch
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PartialInfoTransformationStepBatchIT extends TemporalSparkContext
  with Matchers
  with DistributedMonadImplicits  {

  "A DuplicateColumnsTransformStepBatchIT" should "Create a new column with a substring of another column." in {

    val fields =
      """[{"columnToExtractPartialInfo": "type",
        |"newColumnName": "subtype",
        |"start": "6",
        |"length": "1"
        |}
        |]""".stripMargin


    val inputSchema = StructType(Seq(StructField("type", StringType)))
    val outputSchema = StructType(Seq(StructField("type", StringType), StructField("subtype", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("type-A"), inputSchema),
        new GenericRowWithSchema(Array("type-B"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataSet: RDD[Row] = sc.parallelize(dataIn)

    val dataOut = Seq(
      new GenericRowWithSchema(Array("type-A", "A"), outputSchema),
      new GenericRowWithSchema(Array("type-B", "B"), outputSchema)
    )

    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new PartialInfoTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToPartialInfo" -> fields)
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      print(row.toString())
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }
    assert(arrayValues.length === 2)
  }
}

