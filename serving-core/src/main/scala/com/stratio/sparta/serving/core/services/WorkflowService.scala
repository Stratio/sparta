/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder

import scala.collection.JavaConversions
import scala.util._

//scalastyle:off
class WorkflowService(
                       curatorFramework: CuratorFramework,
                       override val serializerSystem: Option[ActorSystem] = None,
                       override val environmentStateActor: Option[ActorRef] = None
                     ) extends SpartaSerializer with SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)
  private val validatorService = new WorkflowValidatorService(Option(curatorFramework))

  import WorkflowService._

  /** METHODS TO MANAGE WORKFLOWS IN ZOOKEEPER **/

  def findById(id: String): Workflow = {
    log.debug(s"Finding workflow by id $id")
    existsById(id).getOrElse(throw new ServerException(s"No workflow with id $id"))
  }

  def find(query: WorkflowQuery): Workflow = {
    log.debug(s"Finding workflow by query $query")
    exists(query.name, query.version.getOrElse(0L), query.group.getOrElse(DefaultGroup.id.get))
      .getOrElse(throw new ServerException(s"No workflow with name ${query.name}"))
  }

  def findList(query: WorkflowsQuery): Seq[Workflow] = {
    log.debug(s"Finding workflows by query $query")
    existsList(query.group.getOrElse(DefaultGroup.id.get), query.tags)
  }

  def findByGroupID(groupId: String): Seq[Workflow] = {
    log.debug(s"Finding workflows by group id $groupId")
    existsList(groupId)
  }

  def findByTemplateId(templateId: String): Seq[Workflow] = {
    log.debug(s"Finding workflows by template id $templateId")
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.filter { workflow =>
          workflow.pipelineGraph.nodes.exists{ node : NodeGraph =>
            node.nodeTemplate.isDefined && node.nodeTemplate.get.id == templateId
          }
        }
      } else Seq.empty[Workflow]
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        Seq.empty[Workflow]
    }
  }

  def findByIdList(workflowIds: Seq[String]): List[Workflow] = {
    log.debug(s"Finding workflows by ids ${workflowIds.mkString(",")}")
    val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)

    JavaConversions.asScalaBuffer(children).toList.flatMap { id =>
      if (workflowIds.contains(id))
        Option(findById(id))
      else None
    }
  }

  def findAll: List[Workflow] = {
    log.debug(s"Finding all workflows")
    if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
      val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)
      JavaConversions.asScalaBuffer(children).toList.map(id => findById(id))
    } else List.empty[Workflow]
  }

  def create(workflow: Workflow, workflowWithEnv: Option[Workflow] = None): Workflow = {
    log.debug(s"Creating workflow with name ${workflow.name}, version ${workflow.version} " +
      s"and group ${workflow.group.name}")

    mandatoryValidationsWorkflow(workflow)

    val workflowWithFields = addCreationDate(addId(addSpartaVersion(workflow)))

    existsById(workflowWithFields.id.get).foreach(searchWorkflow => throw new ServerException(
      s"Workflow with id ${workflowWithFields.id.get} exists." +
        s" The created workflow name is ${searchWorkflow.name}," +
        s" version ${searchWorkflow.version} and group ${searchWorkflow.group.name}"))

    exists(workflowWithFields.name, workflowWithFields.version, workflowWithFields.group.id.get)
      .foreach(searchWorkflow => throw new ServerException(
        s"Workflow with name ${workflowWithFields.name}," +
          s" version ${workflowWithFields.version} and group ${workflowWithFields.group.name} exists." +
          s" The created workflow id is ${searchWorkflow.id.get}"))

    curatorFramework.create.creatingParentsIfNeeded.forPath(
      s"${AppConstant.WorkflowsZkPath}/${workflowWithFields.id.get}", write(workflowWithFields).getBytes)
    statusService.update(WorkflowStatus(
      id = workflowWithFields.id.get,
      status = WorkflowStatusEnum.Created
    ))

    workflowWithFields
  }

  def rename(workflowRename: WorkflowRename): Try[Unit] = {
    log.debug(s"Renaming workflow versions for group: ${workflowRename.groupId} and name: ${workflowRename.oldName}")
    Try {
      val oldWorkflows = workflowVersions(workflowRename.oldName, workflowRename.groupId)
      Try {
        oldWorkflows.foreach { workflow =>
          update(workflow.copy(name = workflowRename.newName))
        }
      } match {
        case Success(_) =>
          log.debug(s"Workflows correctly updated with the new name: ${workflowRename.newName}")
        case Failure(e) =>
          log.error("Error updating workflow names. All workflows will be rolled back", e)
          val detailMsg = Try {
            oldWorkflows.foreach(workflow => update(workflow))
          } match {
            case Success(_) =>
              log.info("Renaming process encountered an error.The previous data was successfully restored")
              None
            case Failure(exception: Exception) =>
              log.error("Unable to restore the old data after failing to rename the workflows." +
                " The data maybe corrupt. Contact with the support.", exception)
              Option(exception.getLocalizedMessage)
          }

          throw new Exception(
            s"Workflows were not updated with the new name: ${workflowRename.newName}. The old values was restored." +
              s"${if (detailMsg.isDefined) s" Restoring error: ${detailMsg.get}" else ""}", e)
      }
    }
  }

  def createVersion(workflowVersion: WorkflowVersion): Workflow = {
    synchronized {
      log.debug(s"Creating workflow version $workflowVersion")
      existsById(workflowVersion.id) match {
        case Some(workflow) =>
          val workflowWithVersionFields = workflow.copy(
            tags = workflowVersion.tags,
            group = workflowVersion.group.getOrElse(workflow.group)
          )
          val workflowWithFields = addCreationDate(incVersion(addId(workflowWithVersionFields, force = true)))

          mandatoryValidationsWorkflow(workflowWithFields)

          curatorFramework.create.creatingParentsIfNeeded.forPath(
            s"${AppConstant.WorkflowsZkPath}/${workflowWithFields.id.get}", write(workflowWithFields).getBytes)
          statusService.update(WorkflowStatus(
            id = workflowWithFields.id.get,
            status = WorkflowStatusEnum.Created
          ))

          workflowWithFields
        case None => throw new ServerException(s"Workflow with id ${workflowVersion.id} does not exist.")
      }
    }
  }

  def createList(workflows: Seq[Workflow], workflowsWithEnv: Seq[Workflow] = Seq.empty): Seq[Workflow] = {
    log.debug(s"Creating workflows from list")
    workflows.map { workflow =>
      val workflowWithEnv = workflowsWithEnv.find(withEnv =>
        withEnv.name == workflow.name && withEnv.group == workflow.group && withEnv.version == workflow.version)
      create(workflow, workflowWithEnv)
    }
  }

  def update(workflow: Workflow, workflowWithEnv: Option[Workflow] = None): Workflow = {
    log.debug(s"Updating workflow with id ${workflow.id.get}")

    mandatoryValidationsWorkflow(workflow)

    val searchWorkflow = existsById(workflow.id.get)
    val searchByName = exists(workflow.name, workflow.version, workflow.group.id.get)

    if (searchWorkflow.isEmpty) {
      throw new ServerException(s"Workflow with id ${workflow.id.get} does not exist")
    } else if (searchWorkflow.isDefined && searchByName.isDefined && searchWorkflow.get.id != searchByName.get.id) {
      throw new ServerException(
        s"Workflow with name ${searchByName.get.name}," +
          s" version ${searchByName.get.version} and group ${searchByName.get.group.name} exists." +
          s" The created workflow has id ${searchByName.get.id.get}")
    } else {
      val workflowId = addUpdateDate(workflow.copy(id = searchWorkflow.get.id))
      curatorFramework.setData().forPath(
        s"${AppConstant.WorkflowsZkPath}/${workflowId.id.get}", write(workflowId).getBytes)
      workflowId
    }
  }

  def updateList(workflows: Seq[Workflow], workflowsWithEnv: Seq[Workflow] = Seq.empty): Seq[Workflow] = {
    log.debug(s"Updating workflows from list")
    workflows.map { workflow =>
      val workflowWithEnv = workflowsWithEnv.find(withEnv =>
        withEnv.name == workflow.name && withEnv.group == workflow.group && withEnv.version == workflow.version)
      update(workflow, workflowWithEnv)
    }
  }

  def delete(id: String): Try[Unit] = {
    log.debug(s"Deleting workflow with id: $id")
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}/$id"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        curatorFramework.delete().forPath(s"${AppConstant.WorkflowsZkPath}/$id")
        statusService.delete(id)
        executionService.delete(id)
      } else throw new ServerException(s"No workflow with id $id")
    }
  }

  def deleteWithAllVersions(workflowDelete: WorkflowDelete): Try[Unit] = {
    log.debug(s"Deleting workflow with name ${workflowDelete.name} belonging to " +
      s"the group ${workflowDelete.groupId} and all its version.")

    Try {
      val workflowsList = workflowVersions(workflowDelete.name, workflowDelete.groupId)
      try {
        workflowsList.foreach { workflow => delete(workflow.id.get) }
        log.debug(s"The workflow and all its versions were successfully deleted")
      } catch {
        case e: Exception =>
          log.error("Error deleting the workflows. The deletion will be rolled back")
          Try(workflowsList.foreach(workflow => create(workflow)))
          throw new RuntimeException("Error deleting workflows", e)
      }
    }
  }

  def deleteList(workflowIds: Seq[String]): Try[Unit] = {
    log.debug(s"Deleting existing workflows from id list: $workflowIds")
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        val workflows = findByIdList(workflowIds)

        try {
          workflows.foreach(workflow => delete(workflow.id.get))
          log.debug(s"Workflows from ids ${workflowIds.mkString(",")} deleted")
        } catch {
          case e: Exception =>
            log.error("Error deleting workflows. The workflows deleted will be rolled back", e)
            Try(workflows.foreach(workflow => create(workflow)))
            throw new RuntimeException("Error deleting workflows", e)
        }
      }
    }
  }

  def deleteAll(): Try[Unit] = {
    log.debug(s"Deleting all existing workflows")
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        val children = curatorFramework.getChildren.forPath(workflowPath)
        val workflows = JavaConversions.asScalaBuffer(children).toList.map(workflow =>
          read[Workflow](new String(curatorFramework.getData.forPath(
            s"${AppConstant.WorkflowsZkPath}/$workflow")))
        )

        try {
          workflows.foreach(workflow => delete(workflow.id.get))
          log.debug(s"All workflows deleted")
        } catch {
          case e: Exception =>
            log.error("Error deleting workflows. The workflows deleted will be rolled back", e)
            Try(workflows.foreach(workflow => create(workflow)))
            throw new RuntimeException("Error deleting workflows", e)
        }
      }
    }
  }

  def resetAllStatuses(): Try[Unit] = {
    log.debug(s"Resetting the execution status for every workflow")
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        val children = curatorFramework.getChildren.forPath(workflowPath)

        JavaConversions.asScalaBuffer(children).toList.foreach(workflow =>
          statusService.update(WorkflowStatus(workflow, WorkflowStatusEnum.Created))
        )
      }
    }
  }

  def stop(id: String): Try[Any] = {
    log.debug(s"Stopping workflow with id $id")
    statusService.update(WorkflowStatus(id, WorkflowStatusEnum.Stopping))
  }

  def reset(id: String): Try[Any] = {
    log.debug(s"Resetting workflow with id $id")
    statusService.update(WorkflowStatus(id, WorkflowStatusEnum.Created))
  }

  def applyEnv(workflow: Workflow): Workflow = {
    val workflowWithoutEnv = write(workflow)
    read[Workflow](workflowWithoutEnv)
  }

  def moveTo(workflowMove: WorkflowMove): List[Workflow] = {
    //Validate groups
    if (CuratorFactoryHolder.existsPath(s"$GroupZkPath/${workflowMove.groupSourceId}") &&
      CuratorFactoryHolder.existsPath(s"$GroupZkPath/${workflowMove.groupTargetId}")) {
      val groupTarget = read[Group](
        new String(curatorFramework.getData.forPath(s"$GroupZkPath/${workflowMove.groupTargetId}")))
      val all = findAll
      //In this path not exists a workflow with that name
      if (all.count(w => w.group.name == groupTarget.name && w.name == workflowMove.workflowName) > 0) {
        throw new RuntimeException(
          s"Workflow with the name ${workflowMove.workflowName} already exist on the target group ${groupTarget.name}")
      } else {
        val workflowsToMove = all.filter(w => w.group.id.get == workflowMove.groupSourceId && w.name == workflowMove
          .workflowName)
        workflowsToMove.map(w =>
          update(w.copy(group = groupTarget))
        )
      }
    } else {
      throw new ServerException(s"No groups found")
    }
  }

  def validateWorkflow(workflow: Workflow): Unit = {
    val basicValidation = validatorService.validateBasicSettings(workflow)
    val validationWithGraph = basicValidation.combineWithAnd(basicValidation, validatorService.validateGraph(workflow))
    val validationWithPlugins = validationWithGraph.combineWithAnd(validationWithGraph, validatorService.validatePlugins(workflow))

    if (!validationWithPlugins.valid)
      throw new ServerException(s"Workflow is not valid. Cause: ${validationWithPlugins.messages.mkString("-")}")
  }

  def validateDebugWorkflow(workflow: Workflow): Unit = {
    val validationResult = validatorService.validateBasicSettings(workflow)

    if (!validationResult.valid)
      throw new ServerException(s"Workflow is not valid. Cause: ${validationResult.messages.mkString("-")}")
  }

  /** PRIVATE METHODS **/

  private[sparta] def mandatoryValidationsWorkflow(workflow: Workflow): Unit = {
    val basicValidation = validatorService.validateBasicSettings(workflow)
    val validationWithGraph = basicValidation.combineWithAnd(basicValidation, validatorService.validateGraph(workflow))

    if (!validationWithGraph.valid)
      throw new ServerException(s"Workflow is not valid. Cause: ${validationWithGraph.messages.mkString("-")}")
  }

  private[sparta] def exists(name: String, version: Long, group: String): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.find { workflow =>
          workflow.name == name && workflow.version == version && workflow.group.id.get == group
        }
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def existsList(groupId: String, tags: Seq[String] = Seq.empty[String]): Seq[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.filter { workflow =>
          workflow.group.id.get == groupId &&
            (
              workflow.tags.isEmpty ||
                tags.isEmpty ||
                (workflow.tags.isDefined && tags.forall(tag => workflow.tags.get.contains(tag)))
              )
        }
      } else Seq.empty[Workflow]
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        Seq.empty[Workflow]
    }

  private[sparta] def existsById(id: String): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(s"${AppConstant.WorkflowsZkPath}/$id")) {
        val workFlow = read[Workflow](
          new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.WorkflowsZkPath}/$id")))
        Option(workFlow.copy(status =
          statusService.findById(id) recoverWith {
            case e =>
              log.error(s"Error finding workflowStatus with id $id", e)
              Failure(e)
          } toOption
        ))
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def incVersion(workflow: Workflow, userVersion: Option[Long] = None): Workflow =
    userVersion match {
      case None =>
        val maxVersion = Try(workflowVersions(workflow.name, workflow.group.id.get).map(_.version).max + 1)
          .getOrElse(0L)
        workflow.copy(version = maxVersion)
      case Some(usrVersion) => workflow.copy(version = usrVersion)
    }

  private[sparta] def workflowVersions(name: String, group: String): Seq[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.filter { workflow =>
          workflow.name == name && workflow.group.id.isDefined && workflow.group.id.get == group
        }
      } else Seq.empty
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        Seq.empty
    }

  private[sparta] def anyLocalWorkflowRunning: Boolean = {
    val wfStarted = statusService.workflowsStarted
    executionService.findAll() match {
      case Success(list) =>
        list.filter(wfExec => wfExec.genericDataExecution.map(we => we.executionMode == local).getOrElse(false)
          && wfStarted.exists(_.id == wfExec.id))
      case Failure(e) =>
        log.error("An error was encountered while finding all the workflow executions", e)
        Seq()
    }
  }.nonEmpty
}

object WorkflowService{

  private[sparta] def addId(workflow: Workflow, force: Boolean = false): Workflow =
    if (workflow.id.notBlank.isEmpty || (workflow.id.notBlank.isDefined && force))
      workflow.copy(id = Some(UUID.randomUUID.toString))
    else workflow


  private[sparta] def addCreationDate(workflow: Workflow): Workflow =
    workflow.creationDate match {
      case None => workflow.copy(creationDate = Some(new DateTime()))
      case Some(_) => workflow
    }

  private[sparta] def addSpartaVersion(workflow: Workflow): Workflow =
    workflow.versionSparta match {
      case None => workflow.copy(versionSparta = Some(AppConstant.version))
      case Some(_) => workflow
    }

  private[sparta] def addUpdateDate(workflow: Workflow): Workflow =
    workflow.copy(lastUpdateDate = Some(new DateTime()))
}