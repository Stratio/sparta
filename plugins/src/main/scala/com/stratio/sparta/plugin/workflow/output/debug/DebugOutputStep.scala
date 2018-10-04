/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.debug

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.ResultStep
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.serving.core.daoTables._
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.json.RowJsonHelper
import org.json4s.DefaultFormats
import slick.lifted.TableQuery

class DebugOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val workflowId = properties.getString(WorkflowIdKey)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    implicit val formats = DefaultFormats
    import slick.jdbc.PostgresProfile.api._

    val stepName = options.getString(DistributedMonad.StepName)
    val rowsData = dataFrame.collect().map(row => RowJsonHelper.toJSON(row, Map.empty))
    val dataSource = JdbcSlickConnection.getDatabase
    val resultStep = ResultStep(
      id = Some(s"$workflowId-$stepName"),
      step = stepName,
      numEvents = rowsData.length,
      schema = Option(dataFrame.schema.json),
      data = Option(rowsData)
    )
    dataSource.run((DebugOutputStep.table += resultStep).transactionally)
  }
}

object DebugOutputStep {

  lazy val table = TableQuery[DebugResultStepTable]
}
