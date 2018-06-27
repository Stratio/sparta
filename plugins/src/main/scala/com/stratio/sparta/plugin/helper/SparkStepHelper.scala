/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.helper

import com.stratio.sparta.plugin.helper.SchemaHelper.{createOrReplaceTemporalView, getSchemaFromSessionOrModelOrRdd}
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper.isCorrectTableName
import com.stratio.sparta.core.models.PropertySchemasInput
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType

import scala.util.{Failure, Success, Try}

object SparkStepHelper {

  def executeSqlFromSteps(
                           xDSession: XDSession,
                           inputData: Map[String, DistributedMonad[RDD]],
                           sql: String,
                           inputsModel: PropertySchemasInput,
                           executeSqlWhenEmpty: Boolean
                         ): (DistributedMonad[RDD], Option[StructType]) = {
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
          val errorsSteps = inputData.map(step => failRDDWithException(step._2.ds, e))
          (xDSession.sparkContext.union(errorsSteps.toSeq), resultSchema)
        } else throw e //broken chain in errors management
    }
  }

  def failRDDWithException(rdd: RDD[Row], exception: Throwable): RDD[Row] =
    rdd.map(_ => Row.fromSeq(throw exception))

}
