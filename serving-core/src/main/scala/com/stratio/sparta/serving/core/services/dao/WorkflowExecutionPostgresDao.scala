/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.constants.SparkConstant.{SubmitAppNameConf, SubmitUiProxyPrefix}
import com.stratio.sparta.serving.core.constants.{AppConstant, MarathonConstant}
import com.stratio.sparta.serving.core.dao.WorkflowExecutionDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.WorkflowHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionEngine, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow.DtoModelImplicits._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.SparkSubmitService.ExecutionIdKey
import com.stratio.sparta.serving.core.utils.{JdbcSlickConnection, NginxUtils}
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteBiPredicate
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.write
import slick.jdbc.PostgresProfile
import slick.lifted.CanBeQueryCondition

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Properties, Success, Try}

//scalastyle:off
class WorkflowExecutionPostgresDao extends WorkflowExecutionDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import com.stratio.sparta.serving.core.dao.CustomColumnTypes._
  import profile.api._

  override def initializeData(): Unit = {
    initialCacheLoad()
  }

  def findAllExecutions(): Future[List[WorkflowExecution]] = {
    if (cacheEnabled)
      Try {
        cache.iterator().allAsScala()
      }.getOrElse(super.findAll())
    else
      findAll()
  }

  def findExecutionsByStatus(statuses: Seq[WorkflowStatusEnum]): Future[Seq[WorkflowExecution]] =
    db.run(table.filter(_.resumedStatus.inSet(statuses)).result)

  def findExecutionsByIds(ids: Seq[String]): Future[Seq[WorkflowExecution]] =
    db.run(table.filter(_.id.inSet(ids)).result)

  case class SlickFilter[TType, Y, C[_]](val query: Query[TType, Y, C]) {

    def dinamicFilter[FilterValue, Function: CanBeQueryCondition](optionFilter: Option[FilterValue])(f: FilterValue => TType => Function) = {
      optionFilter.map(v => SlickFilter(query.withFilter(f(v)))).getOrElse(this)
    }
  }

  def findExecutionsByQuery(workflowExecutionQuery: WorkflowExecutionQuery): Future[(Seq[WorkflowExecution], Int)] = {

    val statusFilter = workflowExecutionQuery.status.map(_.toLowerCase) match {
      case Some("running") => Some(Set(WorkflowStatusEnum.Launched, WorkflowStatusEnum.Starting, WorkflowStatusEnum.Started, WorkflowStatusEnum.Uploaded))
      case Some("stopped") => Some(Set(WorkflowStatusEnum.Stopped, WorkflowStatusEnum.StoppedByUser, WorkflowStatusEnum.Stopping, WorkflowStatusEnum.StoppingByUser, WorkflowStatusEnum.Finished, WorkflowStatusEnum.NotDefined,
        WorkflowStatusEnum.Created, WorkflowStatusEnum.NotStarted, WorkflowStatusEnum.Killed))
      case Some("failed") => Some(Set(WorkflowStatusEnum.Failed))
      case None => None
    }

    val execEngineFilter = workflowExecutionQuery.executionEngine.map(_.toLowerCase) match {
      case Some(execType) if execType == WorkflowExecutionEngine.Streaming.toString.toLowerCase => Some(WorkflowExecutionEngine.Streaming)
      case Some(execType) if execType == WorkflowExecutionEngine.Batch.toString.toLowerCase => Some(WorkflowExecutionEngine.Batch)
      case None => None
    }

    val dateFilter = workflowExecutionQuery.date match {
      case Some(millis) => Some {
        val current = System.currentTimeMillis()
        val minusDate = current - millis
        (new DateTime(minusDate), new DateTime(current))
      }
      case None => None
    }

    val query = SlickFilter(table)
      .dinamicFilter(workflowExecutionQuery.archived)(archived => t => t.archived === archived)
      .dinamicFilter(statusFilter)(status => t => t.resumedStatus.inSet(status))
      .dinamicFilter(execEngineFilter)(engine => t => t.executionEngine === engine)
      .dinamicFilter(workflowExecutionQuery.searchText)(search => t => t.searchText like s"%$search%")
      .dinamicFilter(dateFilter)(date => t => t.resumedDate >= date._1 && t.resumedDate <= date._2)
      .query.sortBy(_.resumedDate.desc)

    for {
      result <- db.run(query.drop(workflowExecutionQuery.page * workflowExecutionQuery.offset).take(workflowExecutionQuery.offset).result)
      totalCount <- db.run(query.size.result)
    } yield {
      (result, totalCount)
    }
  }

  def createDashboardView(): Future[DashboardView] = getOptimizedDashboardView

  def executionsByDate(executionsByDateQuery: WorkflowExecutionsByDateQuery): Future[WorkflowExecutionsByDate] = {
    val initialQuery = table.filter(data => data.resumedDate.isDefined && data.executionEngine.isDefined)
    val startDateQuery = executionsByDateQuery.startDate match {
      case Some(startDate) => initialQuery.filter(_.resumedDate >= startDate)
      case None => initialQuery
    }
    val endDateQuery = executionsByDateQuery.endDate match {
      case Some(endDate) => startDateQuery.filter(_.resumedDate <= endDate)
      case None => startDateQuery
    }
    val dateTruncationUDF = SimpleFunction.binary[String, Option[DateTime], DateTime]("date_trunc")
    val aggregatedQuery = endDateQuery
      .groupBy { data =>
        (data.executionEngine, dateTruncationUDF(executionsByDateQuery.dateGranularity.toString, data.resumedDate))
      }.map { case ((executionEngine, date), group) =>
      (group.length, executionEngine, date)
    }

    for {
      result <- db.run(aggregatedQuery.result)
    } yield {
      WorkflowExecutionsByDate(result.map { case (total, executionEngine, date) =>
        ExecutionByDate(total, date, executionEngine)
      })
    }
  }

  def findExecutionById(id: String): Future[WorkflowExecution] = findByIdHead(id)

  def createExecution(execution: WorkflowExecution): Future[WorkflowExecution] = {
    log.debug("Adding extra data to execution")
    val executionWithExtraData = addResumedInformation(
      addLastUpdateDate(
        addCreatedStatus(
          addMarathonData(
            addSparkSubmitData(
              addWorkflowId(
                addExecutionId(execution)))))))

    createAndReturn(executionWithExtraData).cached()
  }

  def updateExecution(execution: WorkflowExecution): Future[WorkflowExecution] =
    (for {
      _ <- findByIdHead(execution.id.get)
    } yield {
      upsert(addResumedInformation(execution))
      execution
    }).cached()

  def updateCacheExecutionStatus(execution: WorkflowExecution): Future[WorkflowExecution] = {
    if (cacheEnabled)
      findByIdHead(execution.id.get).cached()
    else Future(execution)
  }

  def updateExecutionSparkURI(execution: WorkflowExecution): Future[WorkflowExecution] = {
    Await.result(
      db.run(
        table.filter(_.id === execution.getExecutionId)
          .map(oldExecution => oldExecution.marathonExecution)
          .update(execution.marathonExecution)
          .transactionally),
      AppConstant.DefaultApiTimeout seconds
    )
    Future(execution).cached()
  }

  def updateStatus(
                    executionStatus: ExecutionStatusUpdate,
                    error: WorkflowError
                  ): WorkflowExecution = updateStatus(executionStatus, Option(error))

  def updateStatus(
                    executionStatus: ExecutionStatusUpdate
                  ): WorkflowExecution = updateStatus(executionStatus, None)

  def updateStatus(
                    executionStatus: ExecutionStatusUpdate,
                    error: Option[WorkflowError]
                  ): WorkflowExecution = {
    val updateFuture = findExecutionById(executionStatus.id).flatMap { actualExecutionStatus =>
      val actualStatus = actualExecutionStatus.lastStatus
      val inputExecutionStatus = executionStatus.status
      val newState = {
        if (inputExecutionStatus.state == WorkflowStatusEnum.NotDefined)
          actualStatus.state
        else inputExecutionStatus.state
      }
      val newStatusInfo = {
        if (inputExecutionStatus.statusInfo.isEmpty && newState != actualStatus.state)
          Option(s"Execution state changed to $newState")
        else if (inputExecutionStatus.statusInfo.isEmpty && newState == actualStatus.state)
          actualStatus.statusInfo
        else inputExecutionStatus.statusInfo
      }
      val newLastUpdateDate = {
        if (newState != actualStatus.state)
          getNewUpdateDate
        else actualStatus.lastUpdateDate
      }
      val newStatus = inputExecutionStatus.copy(
        state = newState,
        statusInfo = newStatusInfo,
        lastUpdateDate = newLastUpdateDate
      )
      val newStatuses = if (newStatus.state == actualStatus.state) {
        newStatus +: actualExecutionStatus.statuses.drop(1)
      } else newStatus +: actualExecutionStatus.statuses
      val newExecutionWithStatus = addResumedInformation(
        actualExecutionStatus.copy(
          statuses = newStatuses,
          genericDataExecution = actualExecutionStatus.genericDataExecution.copy(lastError = error.orElse(actualExecutionStatus.genericDataExecution.lastError))
        ))
      val newExInformation = new StringBuilder
      if (actualStatus.state != newStatus.state)
        newExInformation.append(s"\tStatus -> ${actualStatus.state} to ${newStatus.state}")
      if (actualStatus.statusInfo != newStatus.statusInfo)
        newExInformation.append(s"\tInfo -> ${newStatus.statusInfo.getOrElse("No status information registered")}")
      if (newExInformation.nonEmpty)
        log.info(s"Updating execution ${actualExecutionStatus.getExecutionId}: $newExInformation")

      val upsertResult = upsert(newExecutionWithStatus).map(_ => (actualExecutionStatus, newExecutionWithStatus))
      upsertResult.map(_._2).cached()
      upsertResult
    }

    val updateWithZKFuture = updateFuture.map { case (actualExecutionStatus, newExecutionWithStatus) =>
      if(actualExecutionStatus.lastStatus.state != newExecutionWithStatus.lastStatus.state)
        writeExecutionStatusInZk(WorkflowExecutionStatusChange(actualExecutionStatus, newExecutionWithStatus))
      newExecutionWithStatus
    }

    Try {
      Await.result(updateWithZKFuture, AppConstant.DefaultApiTimeout seconds)
    }.recoverWith {
      case NonFatal(e) =>
        Failure(
          ServerException.create(s"Impossible to update execution with id ${executionStatus.id} and status ${executionStatus.status} after max timeout", e)
        )
    }.get
  }

  def stopExecution(id: String): WorkflowExecution = {
    log.debug(s"Stopping workflow execution with id $id")
    updateStatus(ExecutionStatusUpdate(id, ExecutionStatus(WorkflowStatusEnum.StoppingByUser)))
  }

  def setArchived(executions: Seq[WorkflowExecution], archived: Boolean): Future[Seq[WorkflowExecution]] = {
    val executionsUpdated = executions.map(execution => execution.copy(archived = Option(archived)))

    Await.result(db.run(
      table.filter(_.id.inSet(executionsUpdated.map(_.getExecutionId)))
        .map(execution => execution.archived)
        .update(Option(archived))
        .transactionally
    ), AppConstant.DefaultApiTimeout seconds)

    if (archived) {
      if (cacheEnabled) cache.removeAll(executionsUpdated.map(_.getExecutionId).toSet.asJava)
      Future(executionsUpdated)
    } else Future(executionsUpdated).cached()
  }

  def deleteAllExecutions(): Future[Boolean] =
    for {
      executions <- findAllExecutions()
      canDelete = executions.forall { execution =>
        val statuses = execution.statuses.map(_.state)
        statuses.contains(Failed) || statuses.contains(Stopped)
      }
      result <- if (canDelete) {
        deleteYield(executions)
      } else throw new ServerException("Impossible to delete the executions, its statuses must be stopped or failed to perform this action")
    } yield result

  def deleteExecutions(ids: Seq[String]): Future[Boolean] =
    for {
      executions <- findExecutionsByIds(ids)
      canDelete = {
        val statuses = executions.flatMap(_.statuses.map(_.state))
        statuses.contains(Failed) || statuses.contains(Stopped) || statuses.contains(StoppedByUser)
      }
      result <- if (canDelete) {
        deleteYield(executions)
      } else throw new ServerException("Impossible to delete the execution, its status must be stopped or failed to perform this action")
    } yield result

  def setLaunchDate(execution: WorkflowExecution, date: DateTime): WorkflowExecution = {
    val executionUpdated = execution.copy(
      genericDataExecution = execution.genericDataExecution.copy(launchDate = Option(date))
    )
    Await.result(db.run(
      table.filter(_.id === executionUpdated.getExecutionId)
        .map(execution => execution.genericDataExecution)
        .update(executionUpdated.genericDataExecution)
        .transactionally
    ), AppConstant.DefaultApiTimeout seconds)
    Future(executionUpdated).cached()
    executionUpdated
  }

  def setStartDate(execution: WorkflowExecution, date: DateTime): WorkflowExecution = {
    val executionUpdated = execution.copy(
      genericDataExecution = execution.genericDataExecution.copy(startDate = Option(date))
    )
    Await.result(db.run(
      table.filter(_.id === executionUpdated.getExecutionId)
        .map(execution => execution.genericDataExecution)
        .update(executionUpdated.genericDataExecution)
        .transactionally
    ), AppConstant.DefaultApiTimeout seconds)
    Future(executionUpdated).cached()
    executionUpdated
  }

  def setEndDate(execution: WorkflowExecution, date: DateTime): WorkflowExecution = {
    val executionUpdated = execution.copy(
      genericDataExecution = execution.genericDataExecution.copy(endDate = Option(date))
    )
    Await.result(db.run(
      table.filter(_.id === executionUpdated.getExecutionId)
        .map(execution => execution.genericDataExecution)
        .update(executionUpdated.genericDataExecution)
        .transactionally
    ), AppConstant.DefaultApiTimeout seconds)
    Future(executionUpdated).cached()
    executionUpdated
  }

  def setLastError(execution: WorkflowExecution, error: WorkflowError): WorkflowExecution = {
    val executionUpdated = execution.copy(
      genericDataExecution = execution.genericDataExecution.copy(lastError = Option(error))
    )
    Await.result(db.run(
      table.filter(_.id === executionUpdated.getExecutionId)
        .map(execution => execution.genericDataExecution)
        .update(executionUpdated.genericDataExecution)
        .transactionally
    ), AppConstant.DefaultApiTimeout seconds)
    Future(executionUpdated).cached()
    executionUpdated
  }

  def clearLastError(executionId: String): Future[WorkflowExecution] =
    (for {
      execution <- findByIdHead(executionId)
      executionUpdated = execution.copy(
        genericDataExecution = execution.genericDataExecution.copy(lastError = None)
      )
      _ <- db.run(
        table.filter(_.id === executionId)
          .map(execution => execution.genericDataExecution)
          .update(executionUpdated.genericDataExecution)
          .transactionally
      )
    } yield executionUpdated).cached()

  def anyLocalWorkflowRunning: Future[Boolean] =
    for {
      executions <- findAll()
    } yield {
      val executionsRunning = executions.filter(execution =>
        execution.genericDataExecution.executionMode == local && execution.lastStatus.state == Started)
      executionsRunning.nonEmpty
    }

  def getWorkflowsRunning: Future[Map[String, com.stratio.sparta.serving.core.models.workflow.ExecutionContext]] = {
    val runningStates = Seq(Created, NotStarted, Launched, Starting, Started, Uploaded)
    findExecutionsByStatus(runningStates).map { executions =>
      executions.map(execution =>
        execution.getWorkflowToExecute.id.get -> execution.genericDataExecution.executionContext
      ).toMap
    }
  }

  def otherWorkflowInstanceRunning(workflowId: String, executionContext: ExecutionContext): Future[Boolean] = {
    for{
      workflowsRunning <- getWorkflowsRunning
    } yield {
      workflowsRunning.exists{ case (id, exContext) =>
        id == workflowId &&
          executionContext.paramsLists.forall(exContext.paramsLists.contains(_)) &&
          executionContext.extraParams.forall(exContext.extraParams.contains(_))
      }
    }
  }

  /** Ignite predicates */
  private def ignitePredicateByArchived(archived: Option[Boolean]): ScanQuery[String, WorkflowExecution] = new ScanQuery[String, WorkflowExecution](
    new IgniteBiPredicate[String, WorkflowExecution]() {
      override def apply(k: String, value: WorkflowExecution) = value.archived.equals(archived)
    }
  )

  /** PRIVATE METHODS */

  private[services] def deleteYield(executions: Seq[WorkflowExecution]): Future[Boolean] = {
    val ids = executions.flatMap(_.id.toList)
    for {
      _ <- deleteList(ids).removeInCache(ids: _*)
    } yield {
      log.info(s"Executions with ids: ${ids.mkString(",")} deleted")
      true
    }
  }

  private def writeExecutionStatusInZk(workflowExecutionStatusChange: WorkflowExecutionStatusChange): Unit = {
    synchronized {
      import AppConstant._
      import workflowExecutionStatusChange._

      Try {
        if (CuratorFactoryHolder.existsPath(ExecutionsStatusChangesZkPath))
          CuratorFactoryHolder.getInstance().setData()
            .forPath(ExecutionsStatusChangesZkPath, write(workflowExecutionStatusChange).getBytes)
        else CuratorFactoryHolder.getInstance().create().creatingParentsIfNeeded()
          .forPath(ExecutionsStatusChangesZkPath, write(workflowExecutionStatusChange).getBytes)
      } match {
        case Success(_) =>
          log.info(s"Execution change notified in Zookeeper for execution ${newExecution.getExecutionId}" +
            s" with last status ${originalExecution.lastStatus.state}" +
            s" from (${originalExecution.statuses.map(_.state).mkString(",")}) and new status" +
            s" ${newExecution.lastStatus.state} from (${newExecution.statuses.map(_.state).mkString(",")})")
        case Failure(e) =>
          log.error(s"Error notifying execution change in Zookeeper for execution ${newExecution.getExecutionId}" +
            s" with last status ${originalExecution.lastStatus.state} and new status ${newExecution.lastStatus.state}", e)
          CuratorFactoryHolder.resetInstance()
          throw e
      }
    }
  }

  private[services] def findByIdHead(id: String): Future[WorkflowExecution] =
    for {
      workflowExecList <- filterByIdReal(id)
    } yield {
      if (workflowExecList.nonEmpty)
        workflowExecList.head
      else throw new ServerException(s"No Workflow Execution found with id $id")
    }

  private[services] def filterByArchived(archived: Boolean) =
    db.run(table.filter(_.archived === Option(archived)).result)

  private[services] def filterByIdReal(id: String): Future[Seq[WorkflowExecution]] =
    db.run(table.filter(_.id === id).result)

  private[services] def addId(execution: WorkflowExecution, newId: String): WorkflowExecution = {
    execution.copy(id = Option(newId))
  }

  private[services] def addWorkflowId(execution: WorkflowExecution): WorkflowExecution = {
    val newGenericDataExecution = execution.genericDataExecution.copy(
      workflow = execution.genericDataExecution.workflow.copy(executionId = execution.id)
    )

    execution.copy(genericDataExecution = newGenericDataExecution)
  }

  private[services] def addMarathonData(execution: WorkflowExecution): WorkflowExecution = {
    if (execution.genericDataExecution.executionMode == marathon) {
      val marathonId = WorkflowHelper.getMarathonId(execution)
      val sparkUri = NginxUtils.buildSparkUI(WorkflowHelper.getExecutionDeploymentId(execution))

      execution.copy(
        marathonExecution = execution.marathonExecution match {
          case Some(mExecution) =>
            Option(mExecution.copy(
              marathonId = marathonId,
              sparkURI = sparkUri
            ))
          case None =>
            Option(MarathonExecution(
              marathonId = marathonId,
              sparkURI = sparkUri
            ))
        }
      )
    } else execution
  }

  private[services] def addSparkSubmitData(execution: WorkflowExecution): WorkflowExecution = {
    val newSparkSubmitExecution = execution.sparkSubmitExecution.map { submitEx =>
      val sparkConfWithNginx = addNginxPrefixConf(execution, submitEx.sparkConfigurations)
      val sparkConfWithName = addAppNameConf(
        sparkConfWithNginx,
        execution.getExecutionId,
        execution.getWorkflowToExecute.name
      )

      submitEx.copy(
        driverArguments = submitEx.driverArguments + (ExecutionIdKey -> execution.getExecutionId),
        sparkConfigurations = sparkConfWithName
      )
    }

    execution.copy(sparkSubmitExecution = newSparkSubmitExecution)
  }

  private[services] def addAppNameConf(
                                        sparkConfs: Map[String, String],
                                        executionId: String,
                                        workflowName: String
                                      ): Map[String, String] =
    if (!sparkConfs.contains(SubmitAppNameConf)) {
      sparkConfs ++ Map(SubmitAppNameConf -> s"$workflowName-$executionId")
    } else sparkConfs

  private[services] def addNginxPrefixConf(
                                            workflowExecution: WorkflowExecution,
                                            sparkConfs: Map[String, String]
                                          ): Map[String, String] = {
    if (WorkflowHelper.isMarathonLBConfigured) {
      val proxyLocation = WorkflowHelper.getProxyLocation(WorkflowHelper.getExecutionDeploymentId(workflowExecution))
      sparkConfs + (SubmitUiProxyPrefix -> proxyLocation)
    } else sparkConfs
  }

  private[services] def addExecutionId(execution: WorkflowExecution): WorkflowExecution =
    execution.id match {
      case None =>
        addId(execution, UUID.randomUUID.toString)
      case Some(id) =>
        addId(execution, id)
    }

  private[services] def addCreatedStatus(execution: WorkflowExecution): WorkflowExecution =
    execution.copy(statuses = Seq(ExecutionStatus(
      state = Created,
      statusInfo = Option("Workflow execution created correctly")
    )))

  private[services] def addLastUpdateDate(execution: WorkflowExecution): WorkflowExecution = {
    val createDate = getNewUpdateDate
    if (execution.statuses.isEmpty) {
      execution.copy(statuses = Seq(ExecutionStatus(
        state = Created,
        statusInfo = Option("Workflow execution created correctly"),
        lastUpdateDate = createDate
      )))
    } else {
      val newStatuses = execution.statuses.map { executionStatus =>
        executionStatus.copy(
          lastUpdateDate = createDate
        )
      }
      execution.copy(statuses = newStatuses)
    }
  }

  private[services] def addResumedInformation(execution: WorkflowExecution): WorkflowExecution = {
    import execution.genericDataExecution._
    val genericSettingsDate = launchDate.orElse(startDate).orElse(endDate).getOrElse(new DateTime())
    val newResumedDate = execution.statuses.headOption match {
      case Some(status) => status.lastUpdateDate.getOrElse(genericSettingsDate)
      case None => genericSettingsDate
    }
    execution.copy(
      resumedDate = Try(newResumedDate).toOption,
      resumedStatus = Try(execution.lastStatus.state).toOption,
      executionEngine = Try(execution.getWorkflowToExecute.executionEngine).toOption,
      searchText = Try(execution.getWorkflowToExecute.name).toOption
    )
  }

  private[services] def getNewUpdateDate: Option[DateTime] = Option(new DateTime())

  private[services] def getOptimizedDashboardView: Future[DashboardView] = {

    val archivedExecutions = db.run(table.filter(_.archived === true).length.result)

    val nonArchivedExecutionsStatuses = db.run(table.filter(_.archived === false).map(_.resumedStatus)
      .groupBy(status => status).map { case (status, aggStatus) =>
      (status, aggStatus.length)
    }.result)

    val nonArchivedSummary = nonArchivedExecutionsStatuses.map { aggegatedStatuses =>
      aggegatedStatuses.foldLeft(ExecutionsSummary(0, 0, 0, 0)) {
        (summary, aggStatus: (Option[WorkflowStatusEnum], Int)) =>
          aggStatus._1 match {
            case Some(status) if Seq(Launched, Starting, Started, Uploaded).contains(status) => summary.copy(running = summary.running + aggStatus._2)
            case Some(status) if Seq(Stopping, StoppingByUser, Stopped, StoppedByUser, Finished, NotDefined, Created, NotStarted, Killed).contains(status) => summary.copy(stopped = summary.stopped + aggStatus._2)
            case Some(status) if Seq(Failed).contains(status) => summary.copy(failed = summary.failed + aggStatus._2)
            case None => summary
          }
      }
    }

    val lastExecutions = db.run(table.filter(_.archived === false).sortBy(_.resumedDate.desc).take(10).result).map { executions =>
      executions.map { execution =>
        val executionDto: WorkflowExecutionDto = execution
        executionDto
      }
    }

    for {
      archivedEx <- archivedExecutions
      nonArchivedSum <- nonArchivedSummary
      lastEx <- lastExecutions
    } yield {
      val summary = nonArchivedSum.copy(archived = archivedEx)
      DashboardView(lastEx, summary)
    }
  }
}