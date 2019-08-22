/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.catalog.duplicateColumns

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.catalog.CatalogTransformStepBatch
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CatalogTransformStepBatchIT extends TemporalSparkContext
  with Matchers
  with DistributedMonadImplicits  {

  "A CatalogTransformStepBatch" should "change a value of a column from a dictionary" in {

    val inputSchema = StructType(Seq(StructField("column1", StringType)))
    val outputSchema = StructType(Seq(StructField("column1", StringType)))

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("hello"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataSet: RDD[Row] = sc.parallelize(dataIn)

    val dataOut = Seq(
      new GenericRowWithSchema(Array("bye"), outputSchema)
    )

    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CatalogTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map(
        "columnName" -> "column1",
        "catalogStrategy" -> "CATALOG_FROM_DICTIONARY",
        "catalogFromDictionary" -> """[{"dictionaryKey":"hello","dictionaryValue":"bye"}]"""
      )
    ).transformWithDiscards(inputData)._1

    result.ds.collect() should be (dataOut)
  }
}
