/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.persist

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream


class PersistTransformStepStreaming(
                                     name: String,
                                     outputOptions: OutputOptions,
                                     transformationStepsManagement: TransformationStepManagement,
                                     ssc: Option[StreamingContext],
                                     xDSession: XDSession,
                                     properties: Map[String, JSerializable]
                                   )
  extends PersistTransformStep[DStream](
    name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] =
    applyHeadTransform(inputData) { (inputSchema, inputStream) =>
      inputStream.ds.transform{rdd =>
        val newRdd = transformFunc(rdd)

        getSchemaFromSessionOrModel(xDSession, name, inputsModel)
          .orElse(getSchemaFromSessionOrModelOrRdd(xDSession, inputSchema, inputsModel, newRdd))
          .foreach(sc => xDSession.createDataFrame(newRdd, sc).createOrReplaceTempView(name))
        newRdd
      }
    }
}