/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{BufferedInputStream, File, InputStreamReader, Serializable => JSerializable}
import java.nio.file.Paths
import java.util.UUID

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.column.Integrity.IntegrityTransformStepBatch
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.scalatest.Matchers


class IntegrityTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A IntegrityTransformStepBatchIT" should "Convert all fields of a column that no match with the file of the path given by the user" in {

    val fields =
      """[{"name": "city",
        |"defaultValue": "null"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("name", StringType), StructField("city", StringType)))
    val outputSchema = StructType(Seq(StructField("name", StringType), StructField("city", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("Paco", "bilbao "), inputSchema),
        new GenericRowWithSchema(Array("Antonio", "madrid"), inputSchema),
        new GenericRowWithSchema(Array("Miguel", " "), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("Paco", "null"), inputSchema),
      new GenericRowWithSchema(Array("Antonio", "madrid"), inputSchema),
      new GenericRowWithSchema(Array("Miguel", "null"), inputSchema)
    )

    val dataSet: RDD[Row] = sc.parallelize(dataIn)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new IntegrityTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map(
        "path" -> s"${Paths.get(getClass.getResource("/citiesTest.csv").toURI()).toString}",
        "columnsIntegrity" -> fields.asInstanceOf[JSerializable]
      )
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }
    assert(arrayValues.length === 3)
  }

}



