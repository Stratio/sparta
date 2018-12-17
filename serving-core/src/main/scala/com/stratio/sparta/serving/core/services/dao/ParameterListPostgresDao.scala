/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import scala.concurrent.Future
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._
import slick.jdbc.PostgresProfile
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.dao.ParameterListDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterListAndContexts, ParameterListFromWorkflow, ParameterVariable}
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection

class ParameterListPostgresDao extends ParameterListDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  lazy val workflowService = PostgresDaoFactory.workflowPgService

  override def initializeData(): Unit = {
    val customDefaultsFuture = findByName(CustomExampleParameterList)
    val environmentFuture = findByName(EnvironmentParameterListName)

    customDefaultsFuture.onFailure { case _ =>
      log.debug("Initializing custom defaults list")
      for {
        _ <- createFromParameterList(ParameterList(
          id = CustomExampleParameterListId,
          name = CustomExampleParameterList,
          parameters = DefaultCustomExampleParameters
        ))
      } yield {
        log.debug("The custom defaults list initialization has been completed")
      }
    }
    customDefaultsFuture.onSuccess { case paramList =>
      val variablesNames = paramList.parameters.map(_.name)
      val variablesToAdd = DefaultCustomExampleParameters.filter { variable =>
        !variablesNames.contains(variable.name)
      }
      log.debug(s"Variables not present in the custom defaults list: $variablesToAdd")
      update(paramList.copy(
        parameters = (DefaultCustomExampleParametersMap ++ ParameterList.parametersToMap(paramList.parameters)).values.toSeq
      ))
    }
    environmentFuture.onFailure { case _ =>
      log.debug("Initializing environment list")
      for {
        _ <- createFromParameterList(ParameterList(
          id = EnvironmentParameterListId,
          name = EnvironmentParameterListName,
          parameters = DefaultEnvironmentParameters
        ))
      } yield {
        log.debug("The environment list initialization has been completed")
      }
    }
    environmentFuture.onSuccess { case envList =>
      val variablesNames = envList.parameters.map(_.name)
      val variablesToAdd = DefaultEnvironmentParameters.filter { variable =>
        !variablesNames.contains(variable.name)
      }
      log.debug(s"Variables not present in the environment list: $variablesToAdd")
      update(envList.copy(
        parameters = (DefaultEnvironmentParametersMap ++ ParameterList.parametersToMap(envList.parameters)).values.toSeq
      ))
    }
  }

  def findByParentWithContexts(parent: String): Future[ParameterListAndContexts] = {
    for {
      paramList <- findByName(parent)
      contexts <- findByParent(parent)
    } yield {
      ParameterListAndContexts(
        parameterList = paramList,
        contexts = contexts
      )
    }
  }

  def findByParent(parent: String): Future[Seq[ParameterList]] =
    db.run(table.filter(_.parent === Option(parent)).result)

  def findByName(name: String): Future[ParameterList] = findByNameHead(name)

  def findAllParametersList(): Future[List[ParameterList]] = findAll()

  def findById(id: String): Future[ParameterList] = findByIdHead(id)

  def createFromParameterList(parameterList: ParameterList): Future[ParameterList] =
    db.run(filterById(parameterList.name).result).flatMap { parameterLists =>
      if (parameterLists.nonEmpty)
        throw new ServerException(s"Unable to create parameter list ${parameterList.name} because it already exists")
      else createAndReturn(addId(addCreationDate(parameterList)))
    }

  def createFromWorkflow(parameterListFromWorkflow: ParameterListFromWorkflow): Future[ParameterList] = {
    val parametersInWorkflow = WorkflowPostgresDao.getParametersUsed(parameterListFromWorkflow.workflow)
    val parametersVariables = parametersInWorkflow.flatMap { parameter =>
      if (!parameter.contains("env."))
        Option(ParameterVariable(parameter))
      else if (parameterListFromWorkflow.includeEnvironmentVariables)
        Option(ParameterVariable(parameter))
      else None
    }
    val newName = parameterListFromWorkflow.name.getOrElse {
      val dateCreation = new DateTime()
      s"AutoCreatedList_${parameterListFromWorkflow.workflow.name}_" +
        s"${dateCreation.toString("yyyy-MM-dd-HH:mm:ss:SSS")}"
    }
    val newParameterList = ParameterList(
      name = newName,
      description = parameterListFromWorkflow.description,
      tags = parameterListFromWorkflow.tags,
      parameters = parametersVariables
    )

    db.run(filterById(newParameterList.name).result).flatMap { parameterLists =>
      if (parameterLists.nonEmpty)
        throw new ServerException(s"Unable to create parameter list ${newParameterList.name} because it already exists")
      else createAndReturn(addId(addCreationDate(newParameterList)))
    }
  }

  def update(parameterList: ParameterList): Future[Unit] = {
    updateActions(parameterList).flatMap { case (actions, (workflowsToUpdate, oldParameterListName, newParameterListName)) =>
      db.run(txHandler(DBIO.seq(actions: _*).transactionally)).map(_ =>
        if (cacheEnabled)
          workflowService.upsertFromCacheByParameterList(oldParameterListName, newParameterListName, workflowsToUpdate)
      )
    }
  }

  //scalastyle:off
  def updateActions(parameterList: ParameterList) = {
    val newParameterList = addCreationDate(addUpdateDate(parameterList))
    val id = newParameterList.id.getOrElse(
      throw new ServerException(s"No parameter list found by id ${newParameterList.id}"))

    findById(id).flatMap { oldParameterList =>
      val workflowContextActions = if (newParameterList.parent.notBlank.isEmpty) {
        for {
          (workflowsActions, workflowsToUpdate) <- updateWorkflowsWithNewParamListName(oldParameterList.name, newParameterList.name)
          contextsActions <- updateContextsWithParent(oldParameterList, newParameterList)
        } yield (workflowsActions ++ contextsActions, workflowsToUpdate)
      } else Future((Seq.empty, Seq.empty))

      workflowContextActions.map { case (actions, workflowsToUpdate) =>
        (actions :+ upsertAction(newParameterList), (workflowsToUpdate, oldParameterList.name, newParameterList.name))
      }
    }
  }

  def updateList(parameterLists: Seq[ParameterList]): Future[Unit] = {
    val parentListsWithDates = parameterLists.map(element => addCreationDate(addUpdateDate(element)))
    val parentLists = parentListsWithDates.filter(_.parent.isEmpty)
    val childrenLists = parentListsWithDates.filter(_.parent.nonEmpty)
    for {
      parentActions <- Future.sequence(parentLists.map(updateActions))
      pActions = parentActions.flatMap { case (actions, _) => actions }
      workflowActions = parentActions.map { case (_, actions) => actions }
      _ <- db.run(txHandler(DBIO.seq(pActions: _*).transactionally)).map(_ =>
        if (cacheEnabled)
          workflowActions.foreach(action => workflowService.upsertFromCacheByParameterList(action._2, action._3, action._1))
      )
      childrenActions <- Future.sequence {
        childrenLists.map(updateActions)
      }
      cActions = childrenActions.flatMap { case (actions, _) => actions }
      workflowChildrenActions = childrenActions.map { case (_, actions) => actions }
      finalResult <- db.run(txHandler(DBIO.seq(cActions: _*).transactionally)).map(_ =>
        if (cacheEnabled)
          workflowChildrenActions.foreach(action => workflowService.upsertFromCacheByParameterList(action._2, action._3, action._1))
      )
    } yield {
      finalResult
    }
  }

  def deleteById(id: String): Future[Boolean] =
    for {
      parameterList <- findByIdHead(id)
      response <- deleteYield(Seq(parameterList))
    } yield response

  def deleteByName(name: String): Future[Boolean] =
    for {
      parameterList <- findByNameHead(name)
      response <- deleteYield(Seq(parameterList))
    } yield response

  def deleteAllParameterList(): Future[Boolean] =
    for {
      parametersLists <- findAll()
      response <- deleteYield(parametersLists)
    } yield response

  /** PRIVATE METHODS **/

  private[services] def deleteYield(parametersLists: Seq[ParameterList]): Future[Boolean] = {
    val updateDeleteActions = parametersLists.map { parameterList =>
      val workflowContextActions = if (parameterList.parent.notBlank.isEmpty) {
        for {
          (workflowsActions, workflowsToUpdate) <- updateWorkflowsWithNewParamListName(parameterList.name, "")
          contextsActions <- findByParent(parameterList.name).map(contexts =>
            contexts.map(context => deleteByIDAction(context.id.get))
          )
        } yield (workflowsActions ++ contextsActions, workflowsToUpdate)
      } else Future((Seq.empty, Seq.empty))

      workflowContextActions.map { case (actions, workflowsToUpdate) =>
        (actions :+ deleteByIDAction(parameterList.id.get), workflowsToUpdate)
      }
    }

    Future.sequence(updateDeleteActions).flatMap { actionsSequence =>
      val actions = actionsSequence.flatMap(_._1)
      val workflowsToUpdate = actionsSequence.flatMap(_._2)
      for {
        _ <- db.run(txHandler(DBIO.seq(actions: _*).transactionally))
      } yield {
        log.info(s"Parameter lists ${parametersLists.map(_.name).mkString(",")} deleted")
        if (cacheEnabled)
          parametersLists.forall(parameterList => workflowService.upsertFromCacheByParameterList(parameterList.name, "", workflowsToUpdate))
        else
          true
      }
    }
  }

  private[services] def findByIdHead(id: String): Future[ParameterList] =
    for {
      parameterList <- db.run(filterById(id).result)
    } yield {
      if (parameterList.nonEmpty)
        parameterList.head
      else throw new ServerException(s"No parameter list found by id $id")
    }

  private[services] def findByNameHead(name: String): Future[ParameterList] =
    for {
      parameterList <- db.run(table.filter(_.name === name).result)
    } yield {
      if (parameterList.nonEmpty)
        parameterList.head
      else throw new ServerException(s"No parameter list found by name $name")
    }

  private[services] def addId(parameterList: ParameterList): ParameterList =
    if (parameterList.id.notBlank.isEmpty)
      parameterList.copy(id = Some(UUID.randomUUID.toString))
    else parameterList

  private[services] def addCreationDate(parameterList: ParameterList): ParameterList =
    parameterList.creationDate match {
      case None => parameterList.copy(creationDate = Some(new DateTime()))
      case Some(_) => parameterList
    }

  private[services] def addUpdateDate(parameterList: ParameterList): ParameterList =
    parameterList.copy(lastUpdateDate = Some(new DateTime()))

  private[services] def updateContextsWithParent(
                                                  oldParent: ParameterList,
                                                  newParent: ParameterList
                                                ) = {
    findByParent(oldParent.name).map { contextLists =>
      contextLists.flatMap { contextList =>
        val newParameters = newParent.parameters.map { parameter =>
          val oldContextParameterValue = contextList.getParameterValue(parameter.name).notBlank
          val oldParentParameterValue = oldParent.getParameterValue(parameter.name).notBlank
          val newParentParameterValue = newParent.getParameterValue(parameter.name).notBlank
          val newValue = (oldContextParameterValue, oldParentParameterValue, newParentParameterValue) match {
            case (oldContextValue, oldParentValue, _) if oldContextValue == oldParentValue =>
              newParentParameterValue
            case (Some(oldContextValue), Some(oldParentValue), _) if oldContextValue != oldParentValue =>
              oldContextParameterValue
            case (Some(_), None, _) =>
              oldContextParameterValue
            case (None, None, _) =>
              newParentParameterValue
            case _ =>
              newParentParameterValue
          }
          parameter.copy(value = newValue)
        }

        if (newParameters != contextList.parameters) {
          Option(upsertAction(contextList.copy(
            parent = Option(newParent.name),
            parameters = newParameters
          )))
        } else if (Option(newParent.name) != contextList.parent) {
          Option(upsertAction(contextList.copy(
            parent = Option(newParent.name)
          )))
        } else None
      }
    }
  }

  private[services] def updateWorkflowsWithNewParamListName(oldName: String, newName: String) = {
    if (oldName != newName) {
      workflowService.findAll().map { workflows =>
        val workflowsToUpdate = replaceWorkflowsWithNewParamListName(oldName, newName, workflows)
        (workflowsToUpdate.map(workflow => workflowService.upsertAction(workflow)), workflowsToUpdate)
      }
    } else Future((Seq.empty, Seq.empty))
  }

  private[services] def replaceWorkflowsWithNewParamListName(
                                                              oldName: String,
                                                              newName: String,
                                                              workflows: Seq[Workflow]
                                                            ): Seq[Workflow] = {
    workflows.flatMap { workflow =>
      val workflowStr = write(workflow)
      if (workflowStr.contains(s"{{$oldName")) {
        val newNameToReplace = if (newName.nonEmpty) newName + "." else ""
        val newWorkflowStr = workflowStr.replaceAll(s"\\{\\{$oldName.", s"\\{\\{$newNameToReplace")
        val newWorkflow = read[Workflow](newWorkflowStr)
        Option(WorkflowPostgresDao.addParametersUsed(
          WorkflowPostgresDao.addUpdateDate(
            newWorkflow.copy(settings = newWorkflow.settings.copy(
              global = newWorkflow.settings.global.copy(
                parametersLists = newWorkflow.settings.global.parametersLists.flatMap { list =>
                  if (list == oldName && newName.nonEmpty)
                    Option(newName)
                  else if (list == oldName && newName.isEmpty)
                    None
                  else Option(list)
                }
              )
            )))))
      } else None
    }
  }
}