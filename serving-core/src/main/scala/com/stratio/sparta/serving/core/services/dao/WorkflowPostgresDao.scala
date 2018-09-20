/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import org.joda.time.DateTime
import org.json4s.jackson.Serialization.write
import slick.jdbc.PostgresProfile

import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.dao.WorkflowDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection

//scalastyle:off
class WorkflowPostgresDao extends WorkflowDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.db

  import profile.api._

  import WorkflowPostgresDao._

  private val validatorService = new WorkflowValidatorService()

  def findAllWorkflows(): Future[List[Workflow]] = findAll()

  def findWorkflowById(id: String): Future[Workflow] = findByIdHead(id)

  def findByGroupID(groupId: String): Future[Seq[Workflow]] = db.run(table.filter(_.groupId === groupId).result)

  def findByIdList(workflowIds: Seq[String]): Future[Seq[Workflow]] = db.run(table.filter(_.id.inSet(workflowIds.toSet)).result)

  def findListByWorkflowQuery(query: WorkflowsQuery): Future[Seq[Workflow]] = db.run(table.filter(_.groupId === query.group.get).result)

  def doQuery(query: WorkflowQuery): Future[Seq[Workflow]] = {
    for {
      groupsQuery <-  query.group.fold(Future(Seq.empty[Group])){
        groupQuery =>
          db.run(groupsTable.filter(group =>
            List(
              Some(groupQuery).map(group.name === _),
              Some(groupQuery).map(group.groupId === _)
            ).collect({case Some(criteria: Rep[Boolean])  => criteria})
              .reduceLeftOption(_ || _)
              .getOrElse(true: Rep[Boolean])).result)
      }
      workflow <- db.run(
        table.filter(w => List(
          Some(query.name).map(w.name === _),
          query.version.map(w.version === _),
          groupsQuery.headOption.map(w.groupId === _.id)
        ).collect({case Some(criteria: Rep[Boolean])  => criteria})
          .reduceLeftOption(_ && _)
          .getOrElse(true: Rep[Boolean])).result
      )
    } yield {
      if (workflow.nonEmpty)
        workflow
      else throw new ServerException(s"No workflow found")
    }
  }

  def createWorkflow(workflow: Workflow): Future[Workflow] = {
    log.debug(s"Creating workflow with name ${workflow.name}, version ${workflow.version} " +
      s"and group ${workflow.group.name}")
    mandatoryValidationsWorkflow(workflow)
    val workflowWithFields = addParametersUsed(addCreationDate(addId(addSpartaVersion(workflow.copy(groupId = workflow.group.id)))))
    (for {
      _ <- workflowGroupById(workflow.group.id.get) //validate if group Exists
      exists <- db.run(table.filter(w => w.id =!= workflowWithFields.id.get && w.name === workflowWithFields.name
        && w.version === workflowWithFields.version && w.groupId === workflow.group.id.get
      ).result)
    } yield {
      if (exists.nonEmpty)
        throw new ServerException(
          s"Workflow with name ${workflowWithFields.name}," +
            s" version ${workflowWithFields.version} and group ${workflowWithFields.group.name} exists.")
      else createAndReturn(workflowWithFields)
    }).flatMap(identity)
  }

  def updateWorkflow(workflow: Workflow): Future[Workflow] = {
    log.debug(s"Updating workflow with id ${workflow.id.get}")
    mandatoryValidationsWorkflow(workflow)
    for {
      workflowUpdate <- findByIdHead(workflow.id.get)
    } yield {
      if (workflowUpdate.id.get != workflow.id.get)
        throw new ServerException(
          s"Workflow with name ${workflow.name}," +
            s" version ${workflow.version} and group ${workflow.group.name} exists." +
            s" The created workflow has id ${workflow.id.get}")
      val workflowWithFields = addParametersUsed(addUpdateDate(workflow.copy(id = workflowUpdate.id, groupId = workflowUpdate.groupId)))
      upsert(workflowWithFields)
      workflowWithFields
    }
  }

  def createVersion(workflowVersion: WorkflowVersion): Future[Workflow] ={
    log.debug(s"Creating workflow version $workflowVersion")
    (for {
      workflowById <- findByID(workflowVersion.id)
      workflowsGroup <- if (workflowById.isEmpty)
        throw new ServerException(s"Workflow with id ${
          workflowVersion.id
        } does not exist.")
      else
        findWorkflowsVersion(workflowById.get.name, workflowById.get.group.id.getOrElse("N/A"))
    } yield {
      val workflowWithVersionFields = workflowById.get.copy(
        tags = workflowVersion.tags,
        group = workflowVersion.group.getOrElse(workflowById.get.group),
        groupId = workflowVersion.group.getOrElse(workflowById.get.group).id
      )
      val workflowWithFields = addCreationDate(incVersion(workflowsGroup, addId(workflowWithVersionFields, force = true)))
      mandatoryValidationsWorkflow(workflowWithFields)
      createAndReturn(workflowWithFields)
    }).flatMap(f => f)
  }

  def rename(workflowRename: WorkflowRename) = Try {
    log.debug(s"Renaming workflow versions for group: ${workflowRename.groupId} and name: ${workflowRename.oldName}")
    (for {
      oldWorkflow <- db.run(table.filter(w => w.name === workflowRename.oldName && w.groupId.isDefined && w.groupId === workflowRename.groupId).result)
      updated <- db.run(DBIO.sequence(oldWorkflow.map(w =>
        table.filter(_.id === w.id.get).update(w.copy(name = workflowRename.newName)))).transactionally) //Update after filter by id
    } yield {
      db.run(table.filter(w => w.name === workflowRename.newName && w.groupId === workflowRename.groupId).result)
    }).flatMap(f => f)
  }

  def moveTo(workflowMove: WorkflowMove): Future[Seq[Workflow]] = {
    (for {
      origin <- workflowGroupById(workflowMove.groupSourceId)
      destination <- workflowGroupById(workflowMove.groupTargetId)
      workflowsInOrigin <- findByGroupID(origin.id.get)
      workflowsInDestination <- findByGroupID(destination.id.get)
    } yield {
      if (workflowsInDestination.count(_.name == workflowMove.workflowName) > 0)
        throw new RuntimeException(
          s"Workflow with the name ${workflowMove.workflowName} already exist on the target group ${destination.name}")
      else {
        for {
          _ <- upsertList(workflowsInOrigin.filter(_.name == workflowMove.workflowName).map(w => w.copy(group = destination, groupId = destination.id)))
          getUpserted <- db.run(table.filter(w => w.name === workflowMove.workflowName && w.groupId === destination.id.get).result)
        } yield {
          getUpserted
        }
      }
    }).flatMap(future => future)
  }

  def deleteWorkflowById(id: String): Future[Boolean] = {
    for {
      workflow <- findByIdHead(id)
    } yield deleteYield(Seq(workflow))
  }

  def deleteWithAllVersions(workflowDelete: WorkflowDelete): Future[Boolean] = {
    log.info(s"Deleting workflow with name ${workflowDelete.name} belonging to " + s"the group ${workflowDelete.groupId} and all its version.")
    for {
      workflows <- db.run(table.filter(w => w.name === workflowDelete.name && w.groupId === workflowDelete.groupId).result)
    } yield deleteYield(workflows)
  }

  def deleteWorkflowList(workflowIds: Seq[String]): Future[Boolean] = {
    log.debug(s"Deleting existing workflows from id list: $workflowIds")
    for {
      workflows <- db.run(table.filter(_.id.inSet(workflowIds)).result)
    } yield deleteYield(workflows)
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

  private[dao] def findWorkflowsVersion(name: String, groupId: String): Future[Seq[Workflow]] =
    db.run(table.filter(w => w.name === name && w.groupId.isDefined && w.groupId === groupId).result)

  private[services] def filterByIdReal(id: String): Future[Seq[Workflow]] =
    db.run(table.filter(_.id === id).result)

  private[services] def findByIdHead(id: String): Future[Workflow] =
    for {
      workflowList <- filterByIdReal(id)
    } yield {
      if (workflowList.nonEmpty)
        workflowList.head
      else throw new ServerException(s"No workflow found by id $id")
    }

  private[services] def workflowGroupById(id: String): Future[Group] =
    for {
      group <- db.run(groupsTable.filter(_.groupId === id).result)
    } yield {
      if (group.nonEmpty)
        group.head
      else throw new ServerException(s"No group found by id $id")
    }

  private[sparta] def mandatoryValidationsWorkflow(workflow: Workflow): Unit = {
    val basicValidation = validatorService.validateBasicSettings(workflow)
    val validationWithGraph = basicValidation.combineWithAnd(basicValidation, validatorService.validateGraph(workflow))

    if (!validationWithGraph.valid)
      throw new ServerException(s"Workflow is not valid. Cause: ${
        validationWithGraph.messages.mkString("-")
      }")
  }

  private[sparta] def incVersion(workflows: Seq[Workflow], workflow: Workflow, userVersion: Option[Long] = None): Workflow =
    userVersion match {
      case None => {
        val maxVersion = Try(workflows.map(_.version).max + 1).getOrElse(0L)
        workflow.copy(version = maxVersion)
      }
      case Some(usrVersion) => workflow.copy(version = usrVersion)
    }

  private[services] def deleteYield(workflowLists: Seq[Workflow]): Boolean = {
    val ids = workflowLists.flatMap(_.id.toList)
    Try(deleteList(ids)) match {
      case Success(_) =>
        log.info(s"Workflow with ids=${ids.mkString(",")} deleted")
      case Failure(e) =>
        throw e
    }
    true
  }
}

object WorkflowPostgresDao extends SpartaSerializer {

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

  private[sparta] def addParametersUsed(workflow: Workflow): Workflow =
    workflow.copy(
      settings = workflow.settings.copy(
        global = workflow.settings.global.copy(parametersUsed = getParametersUsed(workflow))
      )
    )

  private[sparta] def getGroupsParametersUsed(workflow: Workflow): Seq[(String, String)] = {
    getParametersUsed(workflow).flatMap{ parameter =>
      val parameterSplitted = parameter.split(".")
      if(parameterSplitted.nonEmpty)
        Option((parameterSplitted.head, parameter))
      else None
    }
  }

  private[sparta] def getParametersUsed(workflow: Workflow): Seq[String] = {
    val workflowStr = write(workflow)
    val parametersTwoBracketsFound = parametersTwoBracketsPattern.findAllIn(workflowStr).toArray.toSeq

    parametersTwoBracketsFound.map { parameter =>
      parameter.replaceAll("\\{\\{", "").replaceAll("\\}\\}", "")
    }.distinct.sortBy(parameter => parameter)
  }
}
