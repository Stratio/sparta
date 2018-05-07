/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.trigger

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

class TriggerTransformStepStreaming(
                                     name: String,
                                     outputOptions: OutputOptions,
                                     transformationStepsManagement: TransformationStepManagement,
                                     ssc: Option[StreamingContext],
                                     xDSession: XDSession,
                                     properties: Map[String, JSerializable]
                                   )
  extends TriggerTransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  //scalastyle:off

  override def transformWithDiscards(
                                      inputData: Map[String, DistributedMonad[DStream]]
                                    ): (DistributedMonad[DStream], Option[StructType], Option[DistributedMonad[DStream]], Option[StructType]) = {
    requireValidateSql()
    require(inputData.size == 2 || inputData.size == 1,
      s"The trigger $name must have one or two input steps, now have: ${inputData.keys}")
    validateSchemas(name, inputsModel, inputData.keys.toSeq)
    require(isCorrectTableName(name),
      s"The step($name) has wrong name and it is not possible to register as temporal table")

    if (inputData.size == 1) {
      val (firstStep, firstStream) = inputData.head
      require(isCorrectTableName(firstStep),
        s"The step($firstStep) have wrong name and is not possible to register as temporal table. ${inputData.keys}")

      val transformedData = firstStream.ds.transform { rdd =>
        Try {
          var executeSql = true
          val schema = getSchemaFromSessionOrModelOrRdd(xDSession, firstStep, inputsModel, rdd)
          executeSql = createOrReplaceTemporalView(xDSession, rdd, firstStep, schema, executeSqlWhenEmpty)

          if (executeSql) {
            log.debug(s"Executing query: $sql")
            val df = xDSession.sql(sql)
            df.createOrReplaceTempView(name)
            df.rdd
          } else {
            val result = rdd.filter(_ => false)
            xDSession.createDataFrame(result, StructType(Nil)).createOrReplaceTempView(name)
            result
          }
        } match {
          case Success(sqlResult) => sqlResult
          case Failure(e) =>
            val result = rdd.map(_ => Row.fromSeq(throw e))
            xDSession.createDataFrame(result, StructType(Nil)).createOrReplaceTempView(name)
            result
        }
      }

      applyHeadDiscardedData(inputData, None, transformedData, None)
    } else {
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
            executeSql = createOrReplaceTemporalView(xDSession, rdd1, firstStep, firstSchema, executeSqlWhenEmpty)
            executeSql = if (executeSql) {
              val secondSchema = getSchemaFromSessionOrModelOrRdd(xDSession, secondStep, inputsModel, rdd2)
              createOrReplaceTemporalView(xDSession, rdd2, secondStep, secondSchema, executeSqlWhenEmpty)
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
                SparkStepHelper.failRDDWithException(rdd1, e),
                SparkStepHelper.failRDDWithException(rdd2, e)
              )
              xDSession.createDataFrame(rdd, StructType(Nil)).createOrReplaceTempView(name)

              rdd
          }
      }

      val transformedData = firstStream.ds.transformWith(secondStream.ds, transformFunc)

      (transformedData, None, None, None)
    }
  }

}

