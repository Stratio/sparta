/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteBiPredicate
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.write
import slick.jdbc.PostgresProfile
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.dao.WorkflowDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection

//scalastyle:off
class WorkflowPostgresDao extends WorkflowDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  import WorkflowPostgresDao._

  private val validatorService = new WorkflowValidatorService()

  lazy val parameterListDao = PostgresDaoFactory.parameterListPostgresDao
  lazy val templateDao = PostgresDaoFactory.templatePgService
  lazy val debugDao = PostgresDaoFactory.debugWorkflowPgService

  override def initializeData(): Unit = {
    initialCacheLoad()
  }

  def findAllWorkflows(): Future[List[Workflow]] = {
    if (cacheEnabled)
      Try {
        cache.iterator().allAsScala()
      }.getOrElse(super.findAll())
    else
      findAll()
  }

  def findWorkflowById(id: String): Future[Workflow] = findByIdHead(id)

  def findByGroupID(groupId: String): Future[Seq[Workflow]] = {
    if (cacheEnabled)
      predicateList(ignitePredicateByGroup(groupId))(findByGroup(groupId))
    else
      findByGroup(groupId)
  }

  def findByIdList(workflowIds: Seq[String]): Future[Seq[Workflow]] = db.run(table.filter(_.id.inSet(workflowIds.toSet)).result)

  def doQuery(query: WorkflowQuery): Future[Seq[Workflow]] = {
    for {
      groupsQuery <- query.group.fold(Future(Seq.empty[Group])) {
        groupQuery =>
          db.run(groupsTable.filter(group =>
            List(
              Some(groupQuery).map(group.name === _),
              Some(groupQuery).map(group.groupId === _)
            ).collect({ case Some(criteria: Rep[Boolean]) => criteria })
              .reduceLeftOption(_ || _)
              .getOrElse(true: Rep[Boolean])).result)
      }
      workflow <- db.run(
        table.filter(w => List(
          Some(query.name).map(w.name === _),
          query.version.map(w.version === _),
          groupsQuery.headOption.map(w.groupId === _.id)
        ).collect({ case Some(criteria: Rep[Boolean]) => criteria })
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
    val workflowWithFields = addParametersUsed(addCreationDate(addId(addSpartaVersion(workflow.copy(groupId = workflow.group.id.orElse(workflow.groupId))))))
    (for {
      _ <- workflowGroupById(workflowWithFields.groupId.get) //validate if group Exists
      exists <- db.run(table.filter(w => w.id =!= workflowWithFields.id.get && w.name === workflowWithFields.name
        && w.version === workflowWithFields.version && w.groupId === workflowWithFields.groupId.get
      ).result)
    } yield {
      if (exists.nonEmpty)
        throw new ServerException(
          s"Workflow with name ${workflowWithFields.name}," +
            s" version ${workflowWithFields.version} and group ${workflowWithFields.group.name} exists.")
      else createAndReturn(workflowWithFields).cached()
    }).flatMap(identity)
  }

  def updateWorkflow(workflow: Workflow): Future[Workflow] = {
    log.debug(s"Updating workflow with id ${workflow.id.get}")
    mandatoryValidationsWorkflow(workflow)
    (for {
      workflowUpdate <- findByIdHead(workflow.id.get)
    } yield {
      if (workflowUpdate.id.get != workflow.id.get)
        throw new ServerException(
          s"Workflow with name ${workflow.name}," +
            s" version ${workflow.version} and group ${workflow.group.name} exists." +
            s" The created workflow has id ${workflow.id.get}")
      val workflowWithFields = addSpartaVersion(addParametersUsed(addUpdateDate(workflow.copy(groupId = workflow.group.id.orElse(workflow.groupId)))))
      upsert(workflowWithFields)
      workflowWithFields
    }).cached()
  }

  def createVersion(workflowVersion: WorkflowVersion): Future[Workflow] = {
    log.debug(s"Creating workflow version $workflowVersion")
    (for {
      workflowById <- findByID(workflowVersion.id)
      workflowsGroup <- if (workflowById.isEmpty) {
        throw new ServerException(s"Workflow with id ${workflowVersion.id} does not exist.")
      } else {
        val targetWorkflowName = workflowVersion.name.getOrElse(workflowById.get.name)
        val targetWorkflowVersion = workflowVersion.group.flatMap(_.id).orElse(workflowById.flatMap(_.group.id)).getOrElse("N/A")
        findWorkflowsVersion(targetWorkflowName, targetWorkflowVersion)
      }
    } yield {

      workflowById.map { originalWorkflow =>
        val workflowWithVersionFields = originalWorkflow.copy(
          name = workflowVersion.name.getOrElse(originalWorkflow.name),
          tags = workflowVersion.tags,
          group = workflowVersion.group.getOrElse(originalWorkflow.group),
          groupId = workflowVersion.group.getOrElse(originalWorkflow.group).id
        )

        val workflowWithFields = addCreationDate(incVersion(workflowsGroup, addId(workflowWithVersionFields, force = true), workflowVersion.version))
        mandatoryValidationsWorkflow(workflowWithFields)
        createAndReturn(workflowWithFields).cached()
      }.get

    }).flatMap(f => f)
  }

  def rename(workflowRename: WorkflowRename) = Try {
    log.debug(s"Renaming workflow versions for group: ${workflowRename.groupId} and name: ${workflowRename.oldName}")
    (for {
      oldWorkflow <- db.run(table.filter(w => w.name === workflowRename.oldName && w.groupId.isDefined && w.groupId === workflowRename.groupId).result)
      _ <- db.run(DBIO.sequence(oldWorkflow.map(w =>
        table.filter(_.id === w.id.get).map(_.name).update(workflowRename.newName))).transactionally) //Update after filter by id
    } yield {
      db.run(table.filter(w => w.name === workflowRename.newName && w.groupId === workflowRename.groupId).result).cached()
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
        (for {
          _ <- upsertList(workflowsInOrigin.filter(_.name == workflowMove.workflowName).map(w => w.copy(group = destination, groupId = destination.id)))
          getUpserted <- db.run(table.filter(w => w.name === workflowMove.workflowName && w.groupId === destination.id.get).result)
        } yield {
          getUpserted
        }).cached()
      }
    }).flatMap(future => future)
  }

  def deleteWorkflowById(id: String): Future[Boolean] = {
    for {
      workflow <- findByIdHead(id)
      result <- deleteYield(Seq(workflow))
    } yield result
  }

  def deleteWithAllVersions(workflowDelete: WorkflowDelete): Future[Boolean] = {
    log.info(s"Deleting workflow with name ${workflowDelete.name} belonging to " + s"the group ${workflowDelete.groupId} and all its version.")
    for {
      workflows <- db.run(table.filter(w => w.name === workflowDelete.name && w.groupId === workflowDelete.groupId).result)
      result <- deleteYield(workflows)
    } yield result
  }

  def deleteWorkflowList(workflowIds: Seq[String]): Future[Boolean] = {
    log.debug(s"Deleting existing workflows from id list: $workflowIds")
    for {
      workflows <- db.run(table.filter(_.id.inSet(workflowIds)).result)
      result <- deleteYield(workflows)
    } yield result
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

  /** CACHE METHODS */
  def deleteFromCacheByGroupId(groupId: String): Future[Boolean] = {
    for {
      workflows <- db.run(table.filter(_.groupId === groupId).result)
    } yield {
      Try(cache.removeAll(workflows.map(_.id.get).toSet.asJava)) match {
        case Success(_) =>
          log.info(s"Deleting from cache : ${workflows.map(_.id.get).mkString(",")}")
          true
        case Failure(e) => throw new Exception(s"Error deleting cache keys with error: ${ExceptionHelper.toPrintableException(e)}", e)
      }
    }
  }

  def upsertFromCacheByParameterList(oldName: String, newName: String, workflows: Seq[Workflow]): Boolean = {
    val workflowsToUpdate = workflows.map { entity => getSpartaEntityId(entity) -> entity }.toMap
    Try(cache.putAll(workflowsToUpdate.asJava)) match {
      case Success(_) =>
        log.info(s"Updating workflows from cache for parameterList: $newName")
        true
      case Failure(e) =>
        log.error(s"Error deleting cache keys with error: ${ExceptionHelper.toPrintableException(e)}", e)
        false
    }
  }

  def updateFromCacheByTemplate(template: TemplateElement): Future[Boolean] = {
    for {
      workflows <- findAll()
    } yield {
      val workflowsToUpdate = templateDao.replaceWorkflowsWithTemplate(template, workflows)
        .map { entity => getSpartaEntityId(entity) -> entity }.toMap
      Try(cache.putAll(workflowsToUpdate.asJava)) match {
        case Success(_) =>
          log.info(s"Updating workflows from cache for template: ${template.name}")
          true
        case Failure(e) => throw new Exception(s"Error deleting cache keys with error: ${ExceptionHelper.toPrintableException(e)}", e)
      }
    }
  }

  def deleteFromCacheByTemplate(template: TemplateElement): Future[Boolean] = {
    for {
      workflows <- findAll()
    } yield {
      val workflowsToUpdate = templateDao.updateWorkflowsWithRemovedTemplate(template, workflows)
        .map { entity => getSpartaEntityId(entity) -> entity }.toMap
      Try(cache.putAll(workflowsToUpdate.asJava)) match {
        case Success(_) =>
          log.info(s"Updating workflows from cache for template: ${template.name}")
          true
        case Failure(e) => throw new Exception(s"Error deleting cache keys with error: ${ExceptionHelper.toPrintableException(e)}", e)
      }
    }
  }

  def updateFromCacheByGroupId(groupId: String): Future[Boolean] = {
    Future {
      Try {
        db.run(table.filter(_.groupId === groupId).result).cached()
      } match {
        case Success(_) =>
          log.info(s"Updating workflows from cache for group: $groupId")
          true
        case Failure(e) => throw new Exception(s"Error updating cache keys with error: ${ExceptionHelper.toPrintableException(e)}", e)
      }
    }
  }

  /** PRIVATE METHODS **/

  private[sparta] def findByGroup(groupId: String): Future[Seq[Workflow]] = db.run(table.filter(_.groupId === groupId).result)

  private[sparta] def findWorkflowsVersion(name: String, groupId: String): Future[Seq[Workflow]] =
    db.run(table.filter(w => w.name === name && w.groupId.isDefined && w.groupId === groupId).result)

  private[sparta] def filterByIdReal(id: String): Future[Seq[Workflow]] =
    db.run(table.filter(_.id === id).result)

  private[sparta] def findByIdHead(id: String): Future[Workflow] =
    for {
      workflowList <- filterByIdReal(id)
    } yield {
      if (workflowList.nonEmpty)
        workflowList.head
      else throw new ServerException(s"No workflow found by id $id")
    }

  private[sparta] def workflowGroupById(id: String): Future[Group] =
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
    if(workflows.nonEmpty) {
      val newVersion = {
        val workflowVersions = workflows.map(_.version)
        userVersion match {
          case Some(userNewVersion) =>
            if (workflowVersions.contains(userNewVersion))
              workflowVersions.max + 1
            else userNewVersion
          case None =>
            if (workflowVersions.nonEmpty)
              workflowVersions.max + 1
            else 0L
        }
      }

      workflow.copy(version = newVersion)
    } else workflow.copy(version = userVersion.getOrElse(0L))

  private[sparta] def deleteYield(workflowLists: Seq[Workflow]): Future[Boolean] = {
    val ids = workflowLists.flatMap(_.id.toList)
    for {
      _ <- deleteList(ids).removeInCache(ids: _*)
      _ <- Future.sequence(ids.map(debugDao.deleteDebugWorkflowByID))
        .recover { case e: Exception => log.warn("Error deleting debugData", e) }
    } yield {
      log.info(s"Workflows with ids: ${
        ids.mkString(",")
      } deleted")
      true
    }
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
    getParametersUsed(workflow).flatMap { parameter =>
      val parameterSplitted = parameter.split(".")
      if (parameterSplitted.nonEmpty)
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

  /** Ignite predicates */
  private[sparta] def ignitePredicateByGroup(group: String): ScanQuery[String, Workflow] = new ScanQuery[String, Workflow](
    new IgniteBiPredicate[String, Workflow]() {
      override def apply(k: String, value: Workflow) = value.groupId.equals(Option(group))
    }
  )
}
