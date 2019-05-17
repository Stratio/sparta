/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import com.stratio.sparta.core.helpers.AggregationTimeHelper
import com.stratio.sparta.serving.core.dao.{CustomColumnTypes, ScheduledWorkflowTaskDao}
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, HeaderAuthUser, LoggedUser}
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskState
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskState.ScheduledTaskState
import com.stratio.sparta.serving.core.models.orchestrator._
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import slick.jdbc.PostgresProfile

import scala.concurrent.Future

class ScheduledWorkflowTaskPostgresDao extends ScheduledWorkflowTaskDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  val MinimumPeriodTime = 500

  import profile.api._
  import CustomColumnTypes._

  def findAllScheduledTasks(): Future[List[ScheduledWorkflowTask]] =
    findAll()

  def findAllScheduledTasksDto(): Future[List[ScheduledWorkflowTaskDto]] =
    db.run(tableScheduledWorkflowTaskDto.sortBy(_.initDate.desc).result).map(_.toList)

  def filterScheduledWorkflowTaskByActive(active: Boolean): Future[Seq[ScheduledWorkflowTask]] =
    filterByActive(active)

  def filterScheduledWorkflowTaskByActiveAndState(
                                                   active: Boolean,
                                                   state: ScheduledTaskState
                                                 ): Future[Seq[ScheduledWorkflowTask]] =
    filterByActiveAndState(active, state)

  def findScheduledWorkflowTaskById(id: String): Future[ScheduledWorkflowTask] =
    findByIdHead(id)

  def createScheduledWorkflowTask(
                                   scheduledWorkflowTaskInsert: ScheduledWorkflowTaskInsert,
                                   user: Option[LoggedUser]
                                 ): Future[ScheduledWorkflowTask] = {
    val scheduledWorkflowTask = ScheduledWorkflowTask(
      id = UUID.randomUUID().toString,
      taskType = scheduledWorkflowTaskInsert.taskType,
      actionType = scheduledWorkflowTaskInsert.actionType,
      entityId = scheduledWorkflowTaskInsert.entityId,
      executionContext = scheduledWorkflowTaskInsert.executionContext,
      active = scheduledWorkflowTaskInsert.active,
      state = ScheduledTaskState.NOT_EXECUTED,
      duration = scheduledWorkflowTaskInsert.duration,
      initDate = scheduledWorkflowTaskInsert.initDate,
      loggedUser = user.map(usr => HeaderAuthUser(usr.id, usr.gid))
    )

    validateScheduledPeriod(scheduledWorkflowTask)

    createAndReturn(scheduledWorkflowTask)
  }

  def updateScheduledWorkflowTask(scheduledWorkflowTask: ScheduledWorkflowTask): Future[ScheduledWorkflowTask] = {
    validateScheduledPeriod(scheduledWorkflowTask)
    upsert(scheduledWorkflowTask).map(_ => scheduledWorkflowTask)
  }

  def setStateScheduledWorkflowTask(id: String, state: ScheduledTaskState): Future[ScheduledWorkflowTask] = {
    for {
      task <- findByIdHead(id)
      upsertResult <- {
        val upsertTask = task.copy(state = state)
        upsert(upsertTask).map(_ => upsertTask)
      }
    } yield upsertResult
  }

  def deleteById(id: String): Future[Boolean] = {
    for {
      task <- findByIdHead(id)
      result <- deleteYield(Seq(task))
    } yield result
  }

  def deleteAllWorkflowTasks(): Future[Boolean] =
    for {
      tasks <- findAll()
      result <- deleteYield(tasks)
    } yield result

  /** PRIVATE METHODS **/

  private[services] def filterByActiveAndState(active: Boolean, state: ScheduledTaskState): Future[Seq[ScheduledWorkflowTask]] = {
    db.run(table.filter(task => task.active === active && task.state === state).result)
  }

  private[services] def filterByActive(active: Boolean): Future[Seq[ScheduledWorkflowTask]] = {
    db.run(table.filter(task => task.active === active).result)
  }

  private[services] def findByIdHead(id: String): Future[ScheduledWorkflowTask] =
    for {
      scheduledTask <- db.run(filterById(id).result)
    } yield {
      if (scheduledTask.nonEmpty)
        scheduledTask.head
      else throw new ServerException(s"No scheduled task found by id $id")
    }

  private[services] def deleteYield(tasks: Seq[ScheduledWorkflowTask]): Future[Boolean] = {
    val deleteActions = tasks.map { currentTask =>
      log.debug(s"Deleting task ${currentTask.id}")
      Future(Seq(deleteByIDAction(currentTask.id)))
    }

    Future.sequence(deleteActions).flatMap { actionsSequence =>
      val actions = actionsSequence.flatten
      for {
        _ <- db.run(txHandler(DBIO.seq(actions: _*).transactionally))
      } yield {
        log.info(s"Tasks ${tasks.map(_.id).mkString(",")} deleted")
        true
      }
    }
  }

  private def validateScheduledPeriod(scheduledWorkflowTask: ScheduledWorkflowTask): Unit = {
    scheduledWorkflowTask.duration.foreach { durationTime =>
      val period = AggregationTimeHelper.parseValueToMilliSeconds(durationTime)

      if (period < MinimumPeriodTime)
        throw new Exception(s"The configured period ($period) is less than the minimun period time ($MinimumPeriodTime) in scheduled tasks")
    }
  }
}
