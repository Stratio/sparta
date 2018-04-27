/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.persist

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class PersistTransformStepBatchIT  extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A PersistTransformStepBatchIT" should "persist RDD" in {
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val unorderedData = Seq(
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("blue"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row]

    )

    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"double"
        |}]
        |""".stripMargin

    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val inputRDD = sc.parallelize(unorderedData)
    val inputData = Map("step1" -> inputRDD)
    val inputField = "csv"

    val result = new PersistTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fields" -> fields.asInstanceOf[JSerializable],
        "inputField" -> inputField,
        "schema.inputMode" -> "FIELDS",
        "storageLevel" -> "MEMORY_AND_DISK_SER_2",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1

    assert(result.ds.getStorageLevel == StorageLevel.MEMORY_AND_DISK_SER_2)
  }
}
