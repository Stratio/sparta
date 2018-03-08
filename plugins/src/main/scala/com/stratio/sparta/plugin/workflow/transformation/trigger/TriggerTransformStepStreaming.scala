/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.trigger

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}

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
  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] = {
    require(sql.nonEmpty, "The input query can not be empty")
    require(validateSql, "The input query is invalid")
    require(inputData.size == 2 || inputData.size == 1,
      s"The trigger $name must have one or two input steps, now have: ${inputData.keys}")
    validateSchemas(inputData)

    if (inputData.size == 1) {
      val (firstStep, firstStream) = inputData.head
      require(isCorrectTableName(firstStep),
        s"The step($firstStep) have wrong name and is not possible to register as temporal table. ${inputData.keys}")

      firstStream.ds.transform { rdd =>
        Try {
          val schema = inputsModel.inputSchemas match {
            case Nil => if (!rdd.isEmpty()) Option(rdd.first().schema) else None
            case x :: Nil => parserInputSchema(x.schema).toOption
          }
          schema.foreach { s =>
            log.debug(s"Registering temporal table with name: $firstStep")
            xDSession.createDataFrame(rdd, s).createOrReplaceTempView(firstStep)
          }
          log.debug(s"Executing query: $sql")
          xDSession.sql(sql)
        } match {
          case Success(sqlResult) => sqlResult.rdd
          case Failure(e) =>
            rdd.map(_ => Row.fromSeq(throw e))
        }
      }
    } else {
      val (firstStep, firstStream) = inputData.head
      val (secondStep, secondStream) = inputData.drop(1).head
      require(isCorrectTableName(firstStep) && isCorrectTableName(secondStep),
        s"The input steps have incorrect names and is not possible to register as temporal table in Spark." +
          s" ${inputData.keys}")

      val transformFunc: (RDD[Row], RDD[Row]) => RDD[Row] = {
        case (rdd1, rdd2) =>
          Try {
            log.debug(s"Registering temporal tables with names: $firstStep, $secondStep")
            val schemas = inputsModel.inputSchemas match {
              case Nil =>
                if (!rdd1.isEmpty() && !rdd2.isEmpty())
                  List((rdd1, rdd1.first().schema, firstStep), (rdd2, rdd2.first().schema, secondStep))
                else List.empty
              case s1 :: s2 :: Nil =>
                if (firstStep == s1.stepName) List(
                  (rdd1, parserInputSchema(s1.schema).get, firstStep),
                  (rdd2, parserInputSchema(s2.schema).get, secondStep)
                )
                else List(
                  (rdd1, parserInputSchema(s2.schema).get, firstStep),
                  (rdd2, parserInputSchema(s1.schema).get, secondStep)
                )
            }

            schemas.foreach { case (rdd, schema, step) =>
              xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(step)
            }
            log.debug(s"Executing query: $sql")
            xDSession.sql(sql)
          } match {
            case Success(sqlResult) => sqlResult.rdd
            case Failure(e) => rdd1.map(_ => Row.fromSeq(throw e))
          }
      }

      firstStream.ds.transformWith(secondStream.ds, transformFunc)
    }
  }
}

