/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.actor.ScheduledWorkflowTaskActor._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskState
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskState.ScheduledTaskState
import com.stratio.sparta.serving.core.models.orchestrator._
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

import scala.util.Try

class ScheduledWorkflowTaskActor()
  extends Actor with ActionUserAuthorize with SpartaSerializer {

  private val scheduledWorkflowTaskPgService = PostgresDaoFactory.scheduledWorkflowTaskPgService
  private val Resource = "Workflows"

  //scalastyle:off
  def receiveApiActions(action: Any): Any = action match {
    case CreateScheduledWorkflowTask(request, user) => createScheduledWorkflowTask(request, user)
    case UpdateScheduledWorkflowTask(request, user) => updateScheduledWorkflowTask(request, user)
    case FindAllScheduledWorkflowTasks(user) => findAllScheduledWorkflowTasks(user)
    case FindAllScheduledWorkflowTasksDto(user) => findAllScheduledWorkflowTasksDto(user)
    case FindScheduledWorkflowTaskByID(id, user) => findScheduledWorkflowTaskByID(id, user)
    case FindScheduledWorkflowTaskByActive(active, user) => findScheduledWorkflowTaskByActive(active, user)
    case FindScheduledWorkflowTaskByActiveAndState(active, state, user) => findScheduledWorkflowTaskByActiveAndState(active, state, user)
    case DeleteAllScheduledWorkflowTasks(user) => deleteAllScheduledWorkflowTasks(user)
    case DeleteScheduledWorkflowTaskByID(id, user) => deleteScheduledWorkflowTaskByID(id, user)
    case _ => log.info("Unrecognized message in ScheduledWorkflowTask Actor")
  }

  def createScheduledWorkflowTask(request: ScheduledWorkflowTaskInsert, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.createScheduledWorkflowTask(request, user)
    }

  def updateScheduledWorkflowTask(request: ScheduledWorkflowTask, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.updateScheduledWorkflowTask(request)
    }

  def findScheduledWorkflowTaskByActive(active: Boolean, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.filterScheduledWorkflowTaskByActive(active)
    }

  def findScheduledWorkflowTaskByActiveAndState(active: Boolean, state: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.filterScheduledWorkflowTaskByActiveAndState(
        active,
        ScheduledTaskState.withName(state.toUpperCase)
      )
    }

  def findScheduledWorkflowTaskByID(id: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.findScheduledWorkflowTaskById(id)
    }

  def findAllScheduledWorkflowTasks(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.findAllScheduledTasks()
    }

  def findAllScheduledWorkflowTasksDto(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.findAllScheduledTasksDto()
    }

  def deleteAllScheduledWorkflowTasks(user: Option[LoggedUser]): Unit = {
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.deleteAllWorkflowTasks()
    }
  }

  def deleteScheduledWorkflowTaskByID(id: String, user: Option[LoggedUser]): Unit = {
    authorizeActions(user, Map(Resource -> Status)) {
      scheduledWorkflowTaskPgService.deleteById(id)
    }
  }
}

object ScheduledWorkflowTaskActor {

  case class UpdateScheduledWorkflowTask(request: ScheduledWorkflowTask, user: Option[LoggedUser])

  case class CreateScheduledWorkflowTask(request: ScheduledWorkflowTaskInsert, user: Option[LoggedUser])

  case class DeleteAllScheduledWorkflowTasks(user: Option[LoggedUser])

  case class DeleteScheduledWorkflowTaskByID(id: String, user: Option[LoggedUser])

  case class FindAllScheduledWorkflowTasks(user: Option[LoggedUser])

  case class FindAllScheduledWorkflowTasksDto(user: Option[LoggedUser])

  case class FindScheduledWorkflowTaskByID(id: String, user: Option[LoggedUser])

  case class FindScheduledWorkflowTaskByActive(active: Boolean, user: Option[LoggedUser])

  case class FindScheduledWorkflowTaskByActiveAndState(active: Boolean, state: String, user: Option[LoggedUser])

  type ResponseScheduledWorkflowTask = Try[ScheduledWorkflowTask]

  type ResponseScheduledWorkflowTasks = Try[Seq[ScheduledWorkflowTask]]

  type ResponseScheduledWorkflowTasksDto = Try[Seq[ScheduledWorkflowTaskDto]]

}


