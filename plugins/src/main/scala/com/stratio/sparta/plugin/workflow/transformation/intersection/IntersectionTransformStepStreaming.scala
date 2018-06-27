/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.intersection

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{getSchemaFromRdd, getSchemaFromSessionOrModel, getSchemaFromSessionOrModelOrRdd}
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

class IntersectionTransformStepStreaming(
                                          name: String,
                                          outputOptions: OutputOptions,
                                          transformationStepsManagement: TransformationStepManagement,
                                          ssc: Option[StreamingContext],
                                          xDSession: XDSession,
                                          properties: Map[String, JSerializable]
                                        )
  extends IntersectionTransformStep[DStream](
    name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  def transformFunc(firstStep: String, secondStep: String): (RDD[Row], RDD[Row]) => RDD[Row] = {
    case (rdd1, rdd2) =>
      partitions.fold(rdd1.intersection(rdd2)) { numPartitions =>
        Try{
          val newRdd = rdd1.intersection(rdd2, numPartitions)
          val tableName = SdkSchemaHelper.discardTableName(name)

          getSchemaFromSessionOrModel(xDSession, name, inputsModel)
            .orElse(getSchemaFromSessionOrModel(xDSession, firstStep, inputsModel))
            .orElse(getSchemaFromSessionOrModel(xDSession, secondStep, inputsModel))
            .orElse(getSchemaFromSessionOrModel(xDSession, tableName, inputsModel))
            .orElse(getSchemaFromRdd(newRdd))
            .foreach(sc => xDSession.createDataFrame(newRdd, sc).createOrReplaceTempView(tableName))

          newRdd
        } match {
          case Success(result) =>
            result
          case Failure(e) =>
            xDSession.sparkContext.union(
              SparkStepHelper.failRDDWithException(rdd1, e),
              SparkStepHelper.failRDDWithException(rdd2, e)
            )
        }
      }
  }

  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[DStream]]
                                    ): (DistributedMonad[DStream], Option[StructType], Option[DistributedMonad[DStream]], Option[StructType]) = {
    require(inputData.size == 2,
      s"The intersection step $name must have two input steps, now have: ${inputData.keys}")

    val (firstStep, firstStream) = inputData.head
    val (secondStep, secondStream) = inputData.drop(1).head
    val transformedData = firstStream.ds.transformWith(secondStream.ds, transformFunc(firstStep, secondStep))
    val firstDiscarded = applyDiscardedData(firstStep, firstStream, None, transformedData, None)._3
    val secondDiscarded = applyDiscardedData(secondStep, secondStream, None, transformedData, None)._3
    val discardedData = if (firstDiscarded.isDefined && secondDiscarded.isDefined) {
      ssc.map(streamingContext => streamingContext.union(Seq(firstDiscarded.get.ds, secondDiscarded.get.ds)))
    } else firstDiscarded.map(_.ds).orElse(secondDiscarded.map(_.ds))

    (transformedData, None, discardedData, None)
  }

}
