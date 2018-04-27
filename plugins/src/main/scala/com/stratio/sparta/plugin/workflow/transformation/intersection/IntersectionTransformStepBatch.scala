/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.intersection

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{getSchemaFromRdd, getSchemaFromSessionOrModel}
import com.stratio.sparta.plugin.helper.SqlHelper
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

  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[RDD]]
                                    ): (DistributedMonad[RDD], Option[StructType], Option[DistributedMonad[RDD]], Option[StructType]) = {
    require(inputData.size == 2,
      s"The intersection step $name must have two input steps, now have: ${inputData.keys}")

    val (firstStep, firstStream) = inputData.head
    val (secondStep, secondStream) = inputData.drop(1).head
    val rdd = partitions.fold(firstStream.ds.intersection(secondStream.ds)) { numPartitions =>
      Try {
        firstStream.ds.intersection(secondStream.ds, numPartitions)
      } match {
        case Success(result) =>
          result
        case Failure(e) =>
          xDSession.sparkContext.union(
            SqlHelper.failWithException(firstStream.ds, e),
            SqlHelper.failWithException(secondStream.ds, e)
          )
      }
    }
    val firstSchema = getSchemaFromSessionOrModel(xDSession, firstStep, inputsModel)
    val secondSchema = getSchemaFromSessionOrModel(xDSession, secondStep, inputsModel)
    val resultSchema = firstSchema.orElse(secondSchema).orElse(getSchemaFromRdd(rdd))
    val firstDiscarded = applyDiscardedData(firstStep, firstStream, resultSchema, rdd, resultSchema)._3
    val secondDiscarded = applyDiscardedData(secondStep, secondStream, resultSchema, rdd, resultSchema)._3
    val discardedData = if (firstDiscarded.isDefined && secondDiscarded.isDefined) {
      Option(xDSession.sparkContext.union(firstDiscarded.get.ds, secondDiscarded.get.ds))
    } else firstDiscarded.map(_.ds).orElse(secondDiscarded.map(_.ds))

    (rdd, resultSchema, discardedData, resultSchema)
  }
}
