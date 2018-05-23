/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.filter

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.getSchemaFromRdd
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

class FilterTransformStepBatch(
                                name: String,
                                outputOptions: OutputOptions,
                                transformationStepsManagement: TransformationStepManagement,
                                ssc: Option[StreamingContext],
                                xDSession: XDSession,
                                properties: Map[String, JSerializable]
                              ) extends FilterTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[RDD]]
                                    ): (DistributedMonad[RDD], Option[StructType], Option[DistributedMonad[RDD]], Option[StructType]) = {
    val (data, schema, _) = applyHeadTransformSchema(inputData) { (stepName, inputDistributedMonad) =>
      val (rdd, schema, inputSchema) = applyFilter(
        inputDistributedMonad.ds,
        filterExpression.getOrElse(throw new Exception("Invalid filter expression")),
        stepName
      )
      (rdd, schema.orElse(getSchemaFromRdd(rdd)), inputSchema)
    }
    val (discardedData, discardedSchema, _) = applyHeadTransformSchema(inputData) { (stepName, inputDistributedMonad) =>
      val discardsExpression = s" NOT (${filterExpression.getOrElse(throw new Exception("Invalid filter expression"))})"
      val (rdd, schema, inputSchema) = applyFilter(inputDistributedMonad.ds, discardsExpression, stepName)
      (rdd, schema.orElse(getSchemaFromRdd(rdd)), inputSchema)
    }

    (data, schema, Option(discardedData), discardedSchema)
  }
}
