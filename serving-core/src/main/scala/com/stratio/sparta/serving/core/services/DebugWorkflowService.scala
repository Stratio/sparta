/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.workflow.step.DebugResults
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant.DebugWorkflowZkPath
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.services.WorkflowService._
import org.json4s.jackson.Serialization._
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization.read

import scala.util.Try

class DebugWorkflowService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def writeDebugWorkflowInZk(debugWorkflow: DebugWorkflow) : DebugWorkflow = {
    val updatedOriginalWorkflow = debugWorkflow.copy( workflowOriginal =
      addCreationDate(addId(debugWorkflow.workflowOriginal)))
    curatorFramework.create.creatingParentsIfNeeded.forPath(
      s"${AppConstant.DebugWorkflowZkPath}/${updatedOriginalWorkflow.workflowOriginal.id.get}",
      write(updatedOriginalWorkflow).getBytes)
    updatedOriginalWorkflow
  }

  def updateDebugWorkflowInZk(debugWorkflow: DebugWorkflow, oldDebugWorkflow: DebugWorkflow) : DebugWorkflow = {
    val updatedWorkflowId = debugWorkflow.copy(workflowOriginal = addUpdateDate(debugWorkflow.workflowOriginal
      .copy(id = oldDebugWorkflow.workflowOriginal.id)))
    curatorFramework.setData().forPath(
      s"${AppConstant.DebugWorkflowZkPath}/${debugWorkflow.workflowOriginal.id.get}", write(updatedWorkflowId).getBytes)
    updatedWorkflowId
  }

  def createDebugWorkflow(debugWorkflow: DebugWorkflow): Try[DebugWorkflow] =
    Try {
      val storedWorkflow = debugWorkflow.workflowOriginal.id match {
        case Some(id) =>
          findByID(id).toOption
            .fold(writeDebugWorkflowInZk(createDebugWorkflowFromOriginal(debugWorkflow))){ existingWk =>
            updateDebugWorkflowInZk(createDebugWorkflowFromOriginal(debugWorkflow), existingWk)
          }
        case _ => writeDebugWorkflowInZk(createDebugWorkflowFromOriginal(debugWorkflow))
      }
      storedWorkflow
    }

  def findByID(id: String): Try[DebugWorkflow] =
    Try {
      val debugWorkflowLocation = s"$DebugWorkflowZkPath/$id"
      if (CuratorFactoryHolder.existsPath(debugWorkflowLocation)) {
        read[DebugWorkflow](new String(curatorFramework.getData.forPath(debugWorkflowLocation)))
      } else throw new ServerException(s"No debug workflow found for workflow id: $id")
    }

  def getResultsByID(id: String): Try[DebugResults] =
    Try {
      val debugWorkflowLocation = s"$DebugWorkflowZkPath/$id"
      if (CuratorFactoryHolder.existsPath(debugWorkflowLocation)) {
        read[DebugWorkflow](new String(curatorFramework.getData.forPath(debugWorkflowLocation))).result.get
      } else throw new ServerException(s"No debug results found for workflow id: $id")
    }

  // @TODO[fl]: to create correctly the debug by substituting Inputs/Outputs
  def createDebugWorkflowFromOriginal(debugWorkflow : DebugWorkflow) : DebugWorkflow = debugWorkflow

}
