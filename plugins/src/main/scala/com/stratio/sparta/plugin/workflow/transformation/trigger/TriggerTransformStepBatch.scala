/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.trigger

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
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

  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType]) = {
    requireValidateSql()

    var resultSchema: Option[StructType] = None
    Try {
      var executeSql = true
      inputData.foreach { case (stepName, stepData) =>
        if (executeSql) {
          require(isCorrectTableName(stepName),
            s"The step ($stepName) has an incorrect name and it's not possible to register it as a temporal table")

          val schema = getSchemaFromSessionOrModelOrRdd(xDSession, stepName, inputsModel, stepData.ds)
          executeSql = createOrReplaceTemporalView(xDSession, stepData.ds, stepName, schema, executeSqlWhenEmpty)
        }
      }
      if (executeSql) {
        log.debug(s"Executing query: $sql")
        val df = xDSession.sql(sql)
        resultSchema = Option(df.schema)
        df.rdd
      } else {
        resultSchema = Option(StructType(Nil))
        xDSession.sparkContext.union(inputData.map(step => step._2.ds.filter(_ => false)).toSeq)
      }
    } match {
      case Success(sqlResult) =>
        (sqlResult, resultSchema)
      case Failure(e) =>
        if (inputData.nonEmpty) {
          val errorsSteps = inputData.map(step => step._2.ds.map(_ => Row.fromSeq(throw e)))
          (xDSession.sparkContext.union(errorsSteps.toSeq), resultSchema)
        } else throw e //broken chain in errors management
    }
  }
}

