/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration.zookeeper

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.migration.EnvironmentAndromeda
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.util.Try

class ZkEnvironmentService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def find(): Option[EnvironmentAndromeda] = {
    Try {
      if (CuratorFactoryHolder.existsPath(EnvironmentZkPath))
        read[EnvironmentAndromeda](new String(curatorFramework.getData.forPath(EnvironmentZkPath)))
      else throw new ServerException(s"No environment found")
    }.toOption
  }

  def create(environmentAndromeda: EnvironmentAndromeda): Unit = {
    log.debug("Creating global parameters")
    val envSorted = environmentAndromeda.copy(variables = environmentAndromeda.variables.sortBy(_.name))
    if (CuratorFactoryHolder.existsPath(EnvironmentOldZkPath))
      curatorFramework.setData().forPath(
        EnvironmentOldZkPath,
        write(envSorted).getBytes
      )
    else curatorFramework.create.creatingParentsIfNeeded.forPath(
      EnvironmentOldZkPath,
      write(envSorted).getBytes
    )
  }

  def deletePath(): Unit = {
    if (CuratorFactoryHolder.existsPath(EnvironmentZkPath))
      curatorFramework.delete().deletingChildrenIfNeeded().forPath(EnvironmentZkPath)
  }

}
