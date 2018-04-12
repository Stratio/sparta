/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.join

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalView, getSchemaFromSessionOrModelOrRdd, validateSchemas}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper.isCorrectTableName
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

class JoinTransformStepStreaming(
                                  name: String,
                                  outputOptions: OutputOptions,
                                  transformationStepsManagement: TransformationStepManagement,
                                  ssc: Option[StreamingContext],
                                  xDSession: XDSession,
                                  properties: Map[String, JSerializable]
                                ) extends JoinTransformStep[DStream](
  name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] = {

    requireValidateSql()
    require(inputData.size == 2,
      s"The join $name must have two input steps, now have: ${inputData.keys}")
    validateSchemas(name, inputsModel, inputData.keys.toSeq)
    require(isCorrectTableName(name),
      s"The step($name) has wrong name and it is not possible to register as temporal table")

    val (firstStep, firstStream) = inputData.head
    val (secondStep, secondStream) = inputData.drop(1).head
    require(isCorrectTableName(firstStep) && isCorrectTableName(secondStep),
      s"The input steps has an incorrect name and it's not possible to register it as a temporal table." +
        s" ${inputData.keys}")

    val transformFunc: (RDD[Row], RDD[Row]) => RDD[Row] = {
      case (rdd1, rdd2) =>
        Try {
          var executeSql = true
          log.debug(s"Registering temporal tables with names: $firstStep, $secondStep")
          val firstSchema = getSchemaFromSessionOrModelOrRdd(xDSession, firstStep, inputsModel, rdd1)
          executeSql = createOrReplaceTemporalView(xDSession, rdd1, firstStep, firstSchema, registerWithEmptySchema = false)
          executeSql = if (executeSql) {
            val secondSchema = getSchemaFromSessionOrModelOrRdd(xDSession, secondStep, inputsModel, rdd2)
            createOrReplaceTemporalView(xDSession, rdd2, secondStep, secondSchema, registerWithEmptySchema = false)
          } else false

          if (executeSql) {
            val df = xDSession.sql(sql)
            df.createOrReplaceTempView(name)
            xDSession.sql(sql).rdd
          } else {
            val rdd = xDSession.sparkContext.union(rdd1.filter(_ => false), rdd2.filter(_ => false))
            xDSession.createDataFrame(rdd, StructType(Nil)).createOrReplaceTempView(name)

            rdd
          }
        } match {
          case Success(sqlResult) =>
            sqlResult
          case Failure(e) =>
            val rdd = xDSession.sparkContext.union(
              rdd1.map(_ => Row.fromSeq(throw e)),
              rdd2.map(_ => Row.fromSeq(throw e))
            )
            xDSession.createDataFrame(rdd, StructType(Nil)).createOrReplaceTempView(name)

            rdd
        }
    }

    firstStream.ds.transformWith(secondStream.ds, transformFunc)
  }
}
