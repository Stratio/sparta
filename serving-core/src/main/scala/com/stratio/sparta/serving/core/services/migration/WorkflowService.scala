/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util._

//scalastyle:off
class WorkflowService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  /** METHODS TO MANAGE WORKFLOWS IN ZOOKEEPER **/

  def findById(id: String): Workflow = {
    log.debug(s"Finding workflow by id $id")
    existsById(id).getOrElse(throw new ServerException(s"No workflow with id $id"))
  }

  def findAll: List[Workflow] = {
    log.debug(s"Finding all workflows")
    if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
      val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)
      JavaConversions.asScalaBuffer(children).toList.map(id => findById(id))
    } else List.empty[Workflow]
  }

  /** PRIVATE METHODS **/

  private[sparta] def existsById(id: String): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(s"${AppConstant.WorkflowsZkPath}/$id")) {
        val workflowStr = new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.WorkflowsZkPath}/$id"))
        Option(read[Workflow](workflowStr))
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

}