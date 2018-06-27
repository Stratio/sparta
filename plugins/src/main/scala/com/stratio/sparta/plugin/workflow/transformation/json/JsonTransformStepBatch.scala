/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{getNewOutputSchema, getSchemaFromRdd, getSchemaFromSessionOrModel, getSchemaFromSessionOrModelOrRdd}
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.TransformStepHelper.sparkBatchDiscardFunction
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

class JsonTransformStepBatch(
                              name: String,
                              outputOptions: OutputOptions,
                              transformationStepsManagement: TransformationStepManagement,
                              ssc: Option[StreamingContext],
                              xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            ) extends JsonTransformStep[RDD](
  name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[RDD]]
                                    ): (DistributedMonad[RDD], Option[StructType], Option[DistributedMonad[RDD]], Option[StructType]) = {
    val (rddDiscarded, rdd) = applyHeadTransformWithDiscards(inputData) { (_, inputStream) =>
      val (discardedData, validData) = sparkBatchDiscardFunction(inputStream.ds, whenRowErrorDo)(generateNewRow)

      (discardedData, validData)
    }
    val finalSchema = getSchemaFromSessionOrModel(xDSession, name, inputsModel)
      .orElse {
        val inputSchema = getSchemaFromSessionOrModel(xDSession, inputData.head._1, inputsModel)
        jsonSchema.flatMap { schema =>
          getNewOutputSchema(
            inputSchema,
            preservationPolicy,
            schema.fields.toSeq, inputField.get
          )
        }
      }
      .orElse(getSchemaFromRdd(rdd.ds))
    val inputSchema = getSchemaFromSessionOrModelOrRdd(xDSession, inputData.head._1, inputsModel, inputData.head._2.ds)

    (rdd, finalSchema, Option(rddDiscarded), inputSchema)
  }
}