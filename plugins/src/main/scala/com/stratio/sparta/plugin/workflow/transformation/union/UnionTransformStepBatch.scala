/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.union

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

class UnionTransformStepBatch(
                               name: String,
                               outputOptions: OutputOptions,
                               transformationStepsManagement: TransformationStepManagement,
                               ssc: Option[StreamingContext],
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             ) extends UnionTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType], Option[StructType]) = {
    val rdds = inputData.map { case (_, rdd) =>
      rdd.ds
    }.toSeq

    val rdd = rdds.size match {
      case 1 => rdds.head
      case x if x > 1 => xDSession.sparkContext.union(rdds)
      case _ => throw new Exception("Invalid input steps")
    }
    val schema = getSchemaFromSessionOrModel(xDSession, name, inputsModel)
      .orElse(getSchemaFromSessionOrModelOrRdd(xDSession, inputData.head._1, inputsModel, rdd))

    (rdd, schema, None)
  }
}
