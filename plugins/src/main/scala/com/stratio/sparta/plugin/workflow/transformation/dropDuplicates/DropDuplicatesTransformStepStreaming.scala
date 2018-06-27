/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.dropDuplicates

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.getSchemaFromRdd
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class DropDuplicatesTransformStepStreaming(
                                            name: String,
                                            outputOptions: OutputOptions,
                                            transformationStepsManagement: TransformationStepManagement,
                                            ssc: Option[StreamingContext],
                                            xDSession: XDSession,
                                            properties: Map[String, JSerializable]
                                          ) extends DropDuplicatesTransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[DStream]]
                                    ): (DistributedMonad[DStream], Option[StructType], Option[DistributedMonad[DStream]], Option[StructType]) = {
    val transformedData = applyHeadTransform(inputData) { (stepName, inputDistributedMonad) =>
      val inputStream = inputDistributedMonad.ds
      inputStream.transform { inputRdd =>
        val (rdd, schema, _) = applyDropDuplicates(inputRdd, columns, stepName)

        schema.orElse(getSchemaFromRdd(rdd))
          .foreach(sc => xDSession.createDataFrame(rdd, sc).createOrReplaceTempView(name))
        rdd
      }
    }

    applyHeadDiscardedData(inputData, None, transformedData, None)
  }
}
