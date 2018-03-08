/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.trigger

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

class TriggerTransformStepBatch(
                                 name: String,
                                 outputOptions: OutputOptions,
                                 transformationStepsManagement: TransformationStepManagement,
                                 ssc: Option[StreamingContext],
                                 xDSession: XDSession,
                                 properties: Map[String, JSerializable]
                               )
  extends TriggerTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  //scalastyle:off
  override def transform(inputData: Map[String, DistributedMonad[RDD]]): DistributedMonad[RDD] = {
    require(sql.nonEmpty, "The input query can not be empty")
    require(validateSql, "The input query is invalid")
    validateSchemas(inputData)

    Try {
      inputData.foreach { case (stepName, stepData) =>
        require(isCorrectTableName(stepName),
          s"The step($stepName) have wrong name and is not possible to register as temporal table. ${inputData.keys}")

        val schema = inputsModel.inputSchemas.filter(is => is.stepName == stepName) match {
          case Nil => if (!stepData.ds.isEmpty()) Some(stepData.ds.first().schema) else None
          case x :: Nil => parserInputSchema(x.schema).toOption
        }
        schema.foreach { s =>
          log.debug(s"Registering temporal table in Spark with name: $stepName")
          xDSession.createDataFrame(stepData.ds, s).createOrReplaceTempView(stepName)
        }
      }
      log.debug(s"Executing query: $sql")
      xDSession.sql(sql)
    } match {
      case Success(sqlResult) =>
        sqlResult.rdd
      case Failure(e) =>
        if (inputData.nonEmpty)
          inputData.head._2.ds.map(_ => Row.fromSeq(throw e))
        else throw e //broken chain in errors management
    }
  }
}

