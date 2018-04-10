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
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class JsonTransformStepStreaming(
                                  name: String,
                                  outputOptions: OutputOptions,
                                  transformationStepsManagement: TransformationStepManagement,
                                  ssc: Option[StreamingContext],
                                  xDSession: XDSession,
                                  properties: Map[String, JSerializable]
                                ) extends JsonTransformStep[DStream](
  name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] = {
    transformFunc(inputData).ds.transform { rdd =>
      getSchemaFromSessionOrModel(xDSession, name, inputsModel)
        .orElse{
          val inputSchema = getSchemaFromSessionOrModel(xDSession, inputData.head._1, inputsModel)
          jsonSchema.flatMap { schema =>
            getNewOutputSchema(
              inputSchema,
              preservationPolicy,
              schema.fields.toSeq, inputFieldName
            )
          }
        }
        .orElse(getSchemaFromRdd(rdd.ds))
        .foreach(schema => xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(name))
      rdd
    }
  }
}