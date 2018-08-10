/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.coalesce

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

class CoalesceTransformStepBatch(
                                  name: String,
                                  outputOptions: OutputOptions,
                                  transformationStepsManagement: TransformationStepManagement,
                                  ssc: Option[StreamingContext],
                                  xDSession: XDSession,
                                  properties: Map[String, JSerializable]
                                )
  extends CoalesceTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType], Option[StructType]) = {
    val rdd = applyHeadTransform(inputData) { (_, inputStream) =>
      partitions.fold(inputStream.ds) { partition => inputStream.ds.coalesce(partition, shuffle) }
    }
    val schema = getSchemaFromSessionOrModel(xDSession, name, inputsModel)
      .orElse(getSchemaFromSessionOrModelOrRdd(xDSession, inputData.head._1, inputsModel, rdd.ds))

    (rdd, schema, schema)
  }

}
