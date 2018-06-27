/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.debug

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import com.stratio.sparta.sdk.constants.SdkConstants._
import com.stratio.sparta.sdk.models.ResultStep
import org.apache.spark.sql.json.RowJsonHelper
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.util.{Failure, Success, Try}

class DebugOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val dataPath = properties.getString(StepDataKey)
  lazy val errorPath = properties.getString(StepErrorDataKey)
  lazy val workflowId = properties.getString(WorkflowIdKey)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    implicit val formats = DefaultFormats

    val stepName = options.getString(DistributedMonad.StepName)
    val path = s"$dataPath/$stepName-$workflowId"
    val rowsData = dataFrame.collect().map(row => RowJsonHelper.toJSON(row, Map.empty))
    val curatorFramework = DebugOutputStep.getInstance(properties.mapValues(_.toString))
    val resultStep = ResultStep(
      step = stepName,
      numEvents = rowsData.length,
      schema = Option(dataFrame.schema.json),
      data = Option(rowsData)
    )

    if (Option(curatorFramework.checkExists().forPath(path)).isDefined) {
      if (resultStep.data.isDefined && resultStep.data.get.nonEmpty)
        curatorFramework.setData().forPath(path, write(resultStep).getBytes)
    }
    else curatorFramework.create().creatingParentsIfNeeded().forPath(path, write(resultStep).getBytes)
  }
}

object DebugOutputStep extends SLF4JLogging {

  private var curatorFramework: Option[CuratorFramework] = None

  def getInstance(properties: Map[String, String]): CuratorFramework = {
    curatorFramework match {
      case None =>
        val defaultConnectionString = properties.getString(ZKConnection, DefaultZKConnection)
        val connectionTimeout = properties.getInt(ZKConnectionTimeout, DefaultZKConnectionTimeout)
        val sessionTimeout = properties.getInt(ZKSessionTimeout, DefaultZKSessionTimeout)
        val retryAttempts = properties.getInt(ZKRetryAttemps, DefaultZKRetryAttemps)
        val retryInterval = properties.getInt(ZKRetryInterval, DefaultZKRetryInterval)

        Try {
          curatorFramework = Some(CuratorFrameworkFactory.builder()
            .connectString(defaultConnectionString)
            .connectionTimeoutMs(connectionTimeout)
            .sessionTimeoutMs(sessionTimeout)
            .retryPolicy(new ExponentialBackoffRetry(retryInterval, retryAttempts)
          ).build())
          curatorFramework.get.start()
          log.info(s"Curator instance created correctly for Zookeeper cluster $defaultConnectionString")
          curatorFramework.get
        } match {
          case Success(curatorFk) => curatorFk
          case Failure(e) => log.error("Unable to establish a connection with the specified Zookeeper", e); throw e
        }
      case Some(curatorFk) => curatorFk
    }
  }
}
