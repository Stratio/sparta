/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration.zookeeper

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.migration.{WorkflowAndromeda, WorkflowCassiopeia}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

class ZkWorkflowService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def createAndromeda(workflow: WorkflowAndromeda, basePath: String): Unit = {
    val workflowLocation = s"$basePath/${workflow.id.get}"
    if (CuratorFactoryHolder.existsPath(workflowLocation))
      curatorFramework.setData().forPath(workflowLocation, write(workflow).getBytes)
    else curatorFramework.create.creatingParentsIfNeeded.forPath(workflowLocation, write(workflow).getBytes)
  }

  def createCassiopea(workflow: WorkflowCassiopeia, basePath: String): Unit = {
    val workflowLocation = s"$basePath/${workflow.id.get}"
    if (CuratorFactoryHolder.existsPath(workflowLocation))
      curatorFramework.setData().forPath(workflowLocation, write(workflow).getBytes)
    else curatorFramework.create.creatingParentsIfNeeded.forPath(workflowLocation, write(workflow).getBytes)
  }

  def deletePath(): Unit = {
    if (CuratorFactoryHolder.existsPath(WorkflowsZkPath))
      curatorFramework.delete().deletingChildrenIfNeeded().forPath(WorkflowsZkPath)
  }

}