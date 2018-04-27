/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{getNewOutputSchema, getSchemaFromRdd, getSchemaFromSessionOrModel}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

class JsonPathTransformStepBatch(
                                  name: String,
                                  outputOptions: OutputOptions,
                                  transformationStepsManagement: TransformationStepManagement,
                                  ssc: Option[StreamingContext],
                                  xDSession: XDSession,
                                  properties: Map[String, JSerializable]
                                ) extends JsonPathTransformStep[RDD](
  name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType], Option[StructType]) = {
    val rdd = transformFunc(inputData)
    val finalSchema = getSchemaFromSessionOrModel(xDSession, name, inputsModel)
      .orElse {
        val inputSchema = getSchemaFromSessionOrModel(xDSession, inputData.head._1, inputsModel)
        getNewOutputSchema(inputSchema, preservationPolicy, outputFieldsSchema, inputField.get)
      }
      .orElse(getSchemaFromRdd(rdd.ds))

    (rdd, finalSchema, None)
  }
}