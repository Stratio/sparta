/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.caseWhen

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.helper.SchemaHelper.getSchemaFromRdd
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.plugin.workflow.transformation.column.AddColumns.AddColumnsTransformStep

class CaseTransformStepBatch(name: String,
                             outputOptions: OutputOptions,
                             transformationStepsManagement: TransformationStepManagement,
                             ssc: Option[StreamingContext],
                             xDSession: XDSession,
                             properties: Map[String, JSerializable]
                            )
  extends CaseTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties){
  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType], Option[StructType]) = {
    applyHeadTransformSchema(inputData) { (stepName, inputDistributedMonad) =>

      val (rdd, schema, inputSchema) = applyCase(inputDistributedMonad.ds, stepName)

      (rdd, schema.orElse(getSchemaFromRdd(rdd)), inputSchema)
    }
  }
}