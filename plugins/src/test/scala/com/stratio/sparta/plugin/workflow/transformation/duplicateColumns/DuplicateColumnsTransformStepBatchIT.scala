/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.duplicateColumns

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD

@RunWith(classOf[JUnitRunner])
class DuplicateColumnsTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits  {

  "A DuplicateColumnsTransformStepBatchIT" should "duplicate columns with a new name" in {

    val fields =
      """[{"columnToDuplicate": "Column3",
        |"newColumnName": "newColumn3"
        |},{
        |"columnToDuplicate": "Column4",
        |"newColumnName": "newColumn4"
        |}
        |]""".stripMargin



    val inputSchema = StructType(Seq(StructField("Column3", StringType), StructField("Column4", StringType)))
    val outputSchema = StructType(Seq(StructField("Column3", StringType), StructField("Column4", StringType),
      StructField("newColumn3", StringType), StructField("newColumn4", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("col3_val1", "col4_val1"), inputSchema),
        new GenericRowWithSchema(Array("col3_val2", "col4_val2"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataSet: RDD[Row] = sc.parallelize(dataIn)

    val dataOut = Seq(
      new GenericRowWithSchema(Array("col3_val1", "col4_val1", "col3_val1", "col4_val1"), outputSchema),
      new GenericRowWithSchema(Array("col3_val2", "col4_val2", "col3_val2", "col4_val2"), outputSchema)
    )

    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)



    val result = new DuplicateColumnsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToDuplicate" -> fields)
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
