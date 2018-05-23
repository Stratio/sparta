/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.join

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper.validateSchemas
import com.stratio.sparta.plugin.helper.SparkStepHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper.isCorrectTableName
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

class JoinTransformStepBatch(
                              name: String,
                              outputOptions: OutputOptions,
                              transformationStepsManagement: TransformationStepManagement,
                              ssc: Option[StreamingContext],
                              xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            ) extends JoinTransformStep[RDD](
  name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transformWithSchema(
                                    inputData: Map[String, DistributedMonad[RDD]]
                                  ): (DistributedMonad[RDD], Option[StructType], Option[StructType]) = {
    requireValidateSql()
    require(inputData.size == 2,
      s"The join $name must have two input steps, now have: ${inputData.keys}")
    validateSchemas(name, inputsModel, inputData.keys.toSeq)
    require(isCorrectTableName(name),
      s"The step($name) has wrong name and it is not possible to register as temporal table")

    val result = SparkStepHelper.executeSqlFromSteps(xDSession, inputData, sql, inputsModel, executeSqlWhenEmpty = false)

    (result._1, result._2, None)
  }
}
