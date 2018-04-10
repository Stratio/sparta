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
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class FilterTransformStepStreaming(
                                    name: String,
                                    outputOptions: OutputOptions,
                                    transformationStepsManagement: TransformationStepManagement,
                                    ssc: Option[StreamingContext],
                                    xDSession: XDSession,
                                    properties: Map[String, JSerializable]
                                  ) extends FilterTransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] = {
    applyHeadTransform(inputData) { (stepName, inputDistributedMonad) =>
      val inputStream = inputDistributedMonad.ds
      inputStream.transform { inputRdd =>
        val (rdd, schema) = applyFilter(
          inputRdd,
          filterExpression.getOrElse(throw new Exception("Invalid filter expression")),
          stepName
        )
        schema.orElse(getSchemaFromRdd(rdd))
          .foreach(sc => xDSession.createDataFrame(rdd, sc).createOrReplaceTempView(name))
        rdd
      }
    }
  }
}
