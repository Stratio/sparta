/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.intersection

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{getSchemaFromRdd, getSchemaFromSessionOrModel}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

class IntersectionTransformStepBatch(
                                      name: String,
                                      outputOptions: OutputOptions,
                                      transformationStepsManagement: TransformationStepManagement,
                                      ssc: Option[StreamingContext],
                                      xDSession: XDSession,
                                      properties: Map[String, JSerializable]
                                    )
  extends IntersectionTransformStep[RDD](
    name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  def transformFunc: (RDD[Row], RDD[Row]) => RDD[Row] = {
    case (rdd1, rdd2) =>
      partitions.fold(rdd1.intersection(rdd2)) { numPartitions =>
        Try{
          rdd1.intersection(rdd2, numPartitions)
        } match {
          case Success(result) =>
            result
          case Failure(e) =>
            xDSession.sparkContext.union(rdd1.map(_ => Row.fromSeq(throw e)), rdd2.map(_ => Row.fromSeq(throw e)))
        }
      }
  }

  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType]) = {
    require(inputData.size == 2,
      s"The intersection step $name must have two input steps, now have: ${inputData.keys}")

    val (firstStep, firstStream) = inputData.head
    val (secondStep, secondStream) = inputData.drop(1).head

    val rdd = transformFunc(firstStream.ds, secondStream.ds)
    val firstSchema = getSchemaFromSessionOrModel(xDSession, firstStep, inputsModel)
    val secondSchema = getSchemaFromSessionOrModel(xDSession, secondStep, inputsModel)

    (rdd, firstSchema.orElse(secondSchema).orElse(getSchemaFromRdd(rdd)))
  }
}
