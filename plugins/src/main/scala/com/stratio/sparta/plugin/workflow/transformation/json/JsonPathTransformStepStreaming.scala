/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{getNewOutputSchema, getSchemaFromRdd, getSchemaFromSessionOrModel, getSchemaFromSessionOrModelOrRdd}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.helpers.TransformStepHelper.sparkStreamingDiscardFunction
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class JsonPathTransformStepStreaming(
                                      name: String,
                                      outputOptions: OutputOptions,
                                      transformationStepsManagement: TransformationStepManagement,
                                      ssc: Option[StreamingContext],
                                      xDSession: XDSession,
                                      properties: Map[String, JSerializable]
                                    ) extends JsonPathTransformStep[DStream](
  name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[DStream]]
                                    ): (DistributedMonad[DStream], Option[StructType], Option[DistributedMonad[DStream]], Option[StructType]) = {
    val (streamDiscarded, stream) = applyHeadTransformWithDiscards(inputData) { (_, inputStream) =>
      val (discardedData, validData) = sparkStreamingDiscardFunction(inputStream.ds, whenRowErrorDo)(generateNewRow)

      (discardedData, validData)
    }
    val finalStreamDiscarded = streamDiscarded.ds.transform { rdd =>
      val tableName = SdkSchemaHelper.discardTableName(name)
      getSchemaFromSessionOrModel(xDSession, inputData.head._1, inputsModel)
        .orElse(getSchemaFromSessionOrModelOrRdd(xDSession, tableName, inputsModel, rdd.ds))
        .foreach(schema => xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName))
      rdd
    }
    val finalStream = stream.ds.transform { rdd =>
      val tableName = name
      getSchemaFromSessionOrModel(xDSession, tableName, inputsModel)
        .orElse{
          getNewOutputSchema(
            getSchemaFromSessionOrModel(xDSession, inputData.head._1, inputsModel),
            preservationPolicy,
            outputFieldsSchema,
            inputField.get
          )
        }
        .orElse(getSchemaFromRdd(rdd.ds))
        .foreach(schema => xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName))
      rdd
    }

    (finalStream, None, Option(finalStreamDiscarded), None)
  }
}