/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, ActorRef, Cancellable}
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.AggregationTimeHelper._
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangeListenerActor.{ForgetExecutionStatusActions, OnExecutionStatusesChangeDo}
import com.stratio.sparta.serving.core.actor.LauncherActor.{Run, Start}
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils.SchedulerUtils
import org.apache.spark.launcher.SparkAppHandle.State
import org.apache.spark.launcher.{SparkAppHandle, SpartaLauncher}
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.util.{Failure, Properties, Success, Try}

class ClusterLauncherActor(executionStatusListenerActor: Option[ActorRef] = None) extends Actor with SchedulerUtils {

  import ClusterLauncherActor._

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val executionService = PostgresDaoFactory.executionPgService

  val executionHandlers = scala.collection.mutable.Map[String, SparkAppHandle]()
  val executionStatuses = scala.collection.mutable.Map[String, WorkflowStatusEnum]()
  val scheduledKillTasks = scala.collection.mutable.Map[String, Cancellable]()
  val scheduledFinishStatusTasks = scala.collection.mutable.Map[String, Cancellable]()

  lazy val SparkClientListenerKey = "-client-listener-stop"
  lazy val SparkClientListenerStatusKey = "-client-listener-status"

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflowExecution) => doStartExecution(workflowExecution)
    case Run(execution: WorkflowExecution) => doRun(execution)
    case ManageListenerStopStatus(handler, state, key, execution) => manageListenerStopState(handler, state, key, execution)
    case KillSparkClientApplication(execution, handler, forceLastStatus) => killSparkClientApplication(execution, handler, forceLastStatus)
    case SetFinishStatus(executionId, forceLastStatus) => setExecutionFinishStatus(executionId, forceLastStatus)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  override def postStop(): Unit = {
    (executionStatuses.keys ++ scheduledKillTasks.keys ++ executionHandlers.keys).foreach { executionId =>
      cleanExecutionActions(executionId)
    }
  }

  def doStartExecution(workflowExecution: WorkflowExecution): Unit = {
    val updateDateResult = if (workflowExecution.genericDataExecution.launchDate.isEmpty)
      executionService.setLaunchDate(workflowExecution, new DateTime())
    else workflowExecution
    doRun(updateDateResult)
  }

  //scalastyle:off
  def doRun(workflowExecution: WorkflowExecution): Unit = {
    Try {
      val workflow = workflowExecution.getWorkflowToExecute
      val submitExecution = workflowExecution.sparkSubmitExecution.get
      val sparkSubmitWithMetrics = submitExecution.submitArguments.map { case (argument, value) =>
        replaceWithEnvVariable(argument) -> replaceWithEnvVariable(value)
      }
      val runtimeConfigurations = SparkSubmitService.getReferenceConfig

      log.info(s"Launching Sparta workflow with options ... \n\t" +
        s"Workflow name: ${workflow.name}\n\t" +
        s"Main Class: $SpartaDriverClass\n\t" +
        s"Driver file: ${submitExecution.driverFile}\n\t" +
        s"Master: ${submitExecution.master}\n\t" +
        s"Spark submit arguments: ${sparkSubmitWithMetrics.mkString(",")}\n\t" +
        s"Spark configurations: ${submitExecution.sparkConfigurations.mkString(",")}\n\t" +
        s"Spark runtime configurations: ${runtimeConfigurations.mkString(",")}\n\t" +
        s"Driver arguments: ${submitExecution.driverArguments}")

      // Notification listener
      val notificationListener = new SparkAppHandle.Listener {
        override def infoChanged(handle: SparkAppHandle): Unit = {
          log.debug(s"The Spark application [${handle.getAppId}] info changed")
        }

        override def stateChanged(handle: SparkAppHandle): Unit = {
          log.info(s"The Spark application [${handle.getAppId}] status changed to [${handle.getState.toString}]")
        }
      }

      // Launcher
      val spartaLauncher = new SpartaLauncher()
        .setAppResource(submitExecution.driverFile)
        .setMainClass(submitExecution.driverClass)
        .setMaster(submitExecution.master)

      //Set Spark Home
      spartaLauncher.setSparkHome(submitExecution.sparkHome)
      //Spark arguments
      sparkSubmitWithMetrics.filter(_._2.nonEmpty)
        .foreach { case (k: String, v: String) => spartaLauncher.addSparkArg(k, v) }
      sparkSubmitWithMetrics.filter(_._2.isEmpty)
        .foreach { case (k: String, v: String) => spartaLauncher.addSparkArg(k) }
      // Spark properties
      (submitExecution.sparkConfigurations ++ runtimeConfigurations).filter(_._2.nonEmpty)
        .foreach { case (key: String, value: String) => spartaLauncher.setConf(key.trim, value.trim) }
      // Driver (Sparta) params
      submitExecution.driverArguments.toSeq.sortWith { case (a, b) => a._1 < b._1 }
        .foreach { case (_, argValue) => spartaLauncher.addAppArgs(argValue) }
      // Redirect options
      spartaLauncher.redirectError()

      // Launch SparkApp
      spartaLauncher.startApplication(notificationListener)
    } match {
      case Failure(exception) =>
        val information = s"An error was encountered while launching the workflow"
        log.error(information, exception)
        val error = WorkflowError(
          information,
          PhaseEnum.Execution,
          exception.toString,
          ExceptionHelper.toPrintableException(exception)
        )
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(state = Failed, statusInfo = Option(information))
        ), error)
      case Success(sparkHandler) =>
        if (workflowExecution.getWorkflowToExecute.settings.global.executionMode == marathon) {
          executionHandlers += (workflowExecution.getExecutionId -> sparkHandler)

          addStatusesChangeListener(workflowExecution.getExecutionId)

          addSparkClientListener(workflowExecution.getExecutionId, sparkHandler)
        }
        val information = "Workflow launched correctly"
        log.info(information)
        val updateStateResult = executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(state = Launched, statusInfo = Option(information))
        ))
        executionService.setStartDate(updateStateResult, new DateTime())
    }
  }

  def addStatusesChangeListener(executionId: String): Unit = {
    executionStatusListenerActor.foreach { listenerActor =>
      listenerActor ! OnExecutionStatusesChangeDo(getClientListenerStatusKey(executionId)) { executionStatusChange =>
        if (executionStatusChange.newExecution.getExecutionId == executionId) {
          executionStatuses += (executionId -> executionStatusChange.newExecution.lastStatus.state)
        }
      }
    }
  }

  def manageListenerStopState(
                               handler: SparkAppHandle,
                               state: WorkflowStatusEnum,
                               key: String,
                               execution: WorkflowExecution
                             ): Unit = {
    val executionId = execution.getExecutionId
    log.info("Stop message received from Zookeeper")

    log.info(s"Stopping spark-client-listener associated to execution id $executionId")
    executionStatusListenerActor.foreach { listenerActor => listenerActor ! ForgetExecutionStatusActions(key) }

    //TODO remove streaming case and send stop when migrate to Spark 2.3.x, this cause a small refactor in scheduleKillTask and scheduleFinishStatusTask. The future upgrade will solve the error log message :"Error reading child process output." and we can see the log messages in streaming workflows when the driver hooks are executed.
    val engine = execution.executionEngine
      .getOrElse(execution.getWorkflowToExecute.executionEngine)
    if (
      !executionHandlers.get(executionId).exists(handler => handler.getState.isFinal) &&
        executionHandlers.get(executionId).exists(handler => handler.getState != State.UNKNOWN) &&
        engine != WorkflowExecutionEngine.Streaming
    ) {
      Try {
        log.info("Stopping execution with handler")

        handler.stop()
      } match {
        case Success(_) =>
          log.info("Workflow correctly stopped with Spark Handler")
        case Failure(e) =>
          log.warn(s"An error was encountered while stopping workflow with Spark Handler, killing it ... " +
            s"with exception: ${e.getLocalizedMessage}")
      }
    }

    scheduleKillTask(execution, handler, Option(state))
  }

  def addSparkClientListener(executionId: String, handler: SparkAppHandle): Unit = {
    executionStatusListenerActor.foreach { listenerActor =>
      log.info(s"Spark Client listener added to execution id: $executionId")
      val key = getClientListenerKey(executionId)

      listenerActor ! OnExecutionStatusesChangeDo(key) { executionStatusChange =>
        if (executionStatusChange.newExecution.getExecutionId == executionId) {
          val state = executionStatusChange.newExecution.lastStatus.state

          if (state == Stopping || state == StoppingByUser || state == Failed) {
            self ! ManageListenerStopStatus(handler, state, key, executionStatusChange.newExecution)
          }
        }
      }
    }
  }

  def scheduleKillTask(
                        execution: WorkflowExecution,
                        handler: SparkAppHandle,
                        forceLastStatus: Option[WorkflowStatusEnum] = None
                      ): Unit = {
    val executionId = execution.getExecutionId
    if (!scheduledKillTasks.contains(executionId) || scheduledKillTasks.get(executionId).exists(_.isCancelled)) {
      val engine = execution.executionEngine.getOrElse(execution.getWorkflowToExecute.executionEngine)
      val shutdownTimeout = {
        Try {
          if (
            executionHandlers.get(executionId).exists(handler => handler.getState == State.UNKNOWN) ||
              engine == WorkflowExecutionEngine.Streaming
          ) {
            GracePeriodMsShutdown
          } else {
            getShutDownTimeoutEngine(execution)
          }
        } match {
          case Success(timeoutMs) =>
            timeoutMs
          case Failure(e) =>
            log.warn(s"Impossible to parse timeout property to milliseconds " +
              s"with exception: ${e.getLocalizedMessage} . Using default value.")
            DefaultShutdownTaskTimeoutMs + GracePeriodMsShutdown
        }
      }

      log.info(s"Scheduling kill task for execution id [$executionId] with milliseconds $shutdownTimeout")
      val killTask = context.system.scheduler.scheduleOnce(
        shutdownTimeout milli,
        self,
        KillSparkClientApplication(execution, handler, forceLastStatus)
      )

      scheduledKillTasks += (executionId -> killTask)
    }
  }

  def killSparkClientApplication(
                                  execution: WorkflowExecution,
                                  handler: SparkAppHandle,
                                  forceLastStatus: Option[WorkflowStatusEnum] = None
                                ): Unit = {
    val executionId = execution.getExecutionId
    val lastState = getLastStatus(executionId, forceLastStatus)

    scheduledKillTasks.get(executionId).foreach(_.cancel())
    scheduledKillTasks -= executionId

    log.info(s"Killing Spark client application for execution id [$executionId] spark appId [${handler.getAppId}]" +
      s" and lastState [$lastState]")
    Try(handler.kill()) match {
      case Success(_) =>
        log.info(s"Workflow killed with Spark Handler")
        scheduleFinishStatusTask(execution, forceLastStatus)
      case Failure(exception) =>
        cleanExecutionActions(executionId)
        val error = s"Problems encountered while killing workflow with Spark Handler"
        log.warn(s"$error with exception: ${exception.getLocalizedMessage}")
        val wError = if (lastState != Failed) {
          Option(WorkflowError(
            error,
            PhaseEnum.Stop,
            exception.toString,
            ExceptionHelper.toPrintableException(exception)
          ))
        } else None

        executionService.updateStatus(ExecutionStatusUpdate(
          executionId,
          ExecutionStatus(
            state = Failed,
            statusInfo = Option(error)
          )), wError)
    }
  }

  def scheduleFinishStatusTask(
                                execution: WorkflowExecution,
                                forceLastStatus: Option[WorkflowStatusEnum] = None
                              ): Unit = {
    synchronized {
      val executionId = execution.getExecutionId
      val shutdownTimeout = {
        Try {
          val engine = execution.executionEngine.getOrElse(execution.getWorkflowToExecute.executionEngine)
          if (
            executionHandlers.get(executionId).exists(handler => handler.getState.isFinal) &&
              engine == WorkflowExecutionEngine.Batch
          ) {
            GracePeriodMsShutdown
          } else if (
            executionHandlers.get(executionId).exists(handler => handler.getState == State.UNKNOWN) ||
              engine == WorkflowExecutionEngine.Streaming
          ) {
            getShutDownTimeoutEngine(execution)
          } else {
            val shutdownTimeoutProp = shutDownMesosTimeout(execution)
            parseValueToMilliSeconds(shutdownTimeoutProp) + GracePeriodMsShutdown
          }
        } match {
          case Success(timeoutMs) =>
            timeoutMs
          case Failure(e) =>
            log.warn(s"Impossible to parse timeout property to milliseconds" +
              s" with exception: ${e.getLocalizedMessage} . Using default value.")
            DefaultShutdownTaskTimeoutMs + GracePeriodMsShutdown
        }
      }

      log.info(s"Scheduling finish status task for execution id [$executionId] with milliseconds $shutdownTimeout")
      val scheduledFinishStatusTask = context.system.scheduler.scheduleOnce(
        shutdownTimeout milli,
        self,
        SetFinishStatus(executionId, forceLastStatus)
      )

      scheduledFinishStatusTasks += (executionId -> scheduledFinishStatusTask)
    }
  }

  def setExecutionFinishStatus(executionId: String, forceLastStatus: Option[WorkflowStatusEnum] = None): Unit = {
    val lastState = getLastStatus(executionId, forceLastStatus)

    cleanExecutionActions(executionId)

    val information = s"Workflow correctly finished with Spark Handler"
    log.info(information)
    executionService.updateStatus(ExecutionStatusUpdate(
      executionId,
      ExecutionStatus(
        state = if (lastState == StoppingByUser) StoppedByUser
        else if (lastState == Stopping) Stopped
        else if (lastState == StoppedByUser || lastState == Stopped) lastState
        else Failed,
        statusInfo = Option(information)
      )))
  }

  def cleanExecutionActions(executionId: String): Unit = {
    val statusKey = getClientListenerStatusKey(executionId)
    val listenerKey = getClientListenerKey(executionId)

    scheduledKillTasks.get(executionId).foreach(_.cancel())
    scheduledFinishStatusTasks.get(executionId).foreach(_.cancel())

    executionStatusListenerActor.foreach { listenerActor =>
      listenerActor ! ForgetExecutionStatusActions(listenerKey)
      listenerActor ! ForgetExecutionStatusActions(statusKey)
    }

    executionStatuses -= executionId
    scheduledKillTasks -= executionId
    scheduledFinishStatusTasks -= executionId
    executionHandlers -= executionId
  }


  def getLastStatus(executionId: String, forceLastStatus: Option[WorkflowStatusEnum] = None): WorkflowStatusEnum = {
    val storedStatus = executionStatuses.getOrElse(executionId, Stopped)

    if (storedStatus == Failed) storedStatus
    else forceLastStatus.getOrElse(storedStatus)
  }

  def getClientListenerKey(executionId: String): String = {
    executionId + SparkClientListenerKey
  }

  def getClientListenerStatusKey(executionId: String): String = {
    executionId + SparkClientListenerStatusKey
  }

  def replaceWithEnvVariable(toReplace: String): String = {
    toReplace
      .replaceAll(
        PrometheusMetricsPortEnv,
        Properties.envOrElse(PrometheusMetricsPortEnv, DefaultMetricsMarathonDriverPort.toString)
      )
      .replaceAll(
        JmxMetricsPortEnv,
        Properties.envOrElse(JmxMetricsPortEnv, DefaultJmxMetricsMarathonDriverPort.toString)
      )
      .replaceAll(
        HostInUseMetricsEnv,
        Properties.envOrElse(HostInUseMetricsEnv, "0.0.0.0")
      )
  }
}

object ClusterLauncherActor {

  case class ManageListenerStopStatus(
                                       handler: SparkAppHandle,
                                       state: WorkflowStatusEnum,
                                       key: String,
                                       execution: WorkflowExecution
                                     )

  case class SetFinishStatus(executionId: String, forceLastStatus: Option[WorkflowStatusEnum] = None)

  case class KillSparkClientApplication(
                                         execution: WorkflowExecution,
                                         handler: SparkAppHandle,
                                         forceLastStatus: Option[WorkflowStatusEnum] = None
                                       )

  def getShutDownTimeoutEngine(execution: WorkflowExecution): Long = {
    val shutdownTimeoutProp = shutDownMesosTimeout(execution)
    val shutdownTimeoutEngine = {
      execution.executionEngine.getOrElse(execution.getWorkflowToExecute.executionEngine) match {
        case WorkflowExecutionEngine.Batch =>
          shutdownTimeoutProp
        case WorkflowExecutionEngine.Streaming =>
          execution.getWorkflowToExecute.settings.streamingSettings.stopGracefullyTimeout.getOrElse(shutdownTimeoutProp)
      }
    }

    Try(parseValueToMilliSeconds(shutdownTimeoutEngine))
      .toOption
      .orElse(Try(parseValueToMilliSeconds(shutdownTimeoutProp)).toOption)
      .map(time => time + GracePeriodMsShutdown)
      .getOrElse(GracePeriodMsShutdown)
  }

  def shutDownMesosTimeout(execution: WorkflowExecution): String = {
    execution.sparkSubmitExecution.flatMap(exec =>
      exec.sparkConfigurations.get(SubmitMesosShutdownTimeout)
    ).getOrElse(DefaultShutdownTaskTimeout)
  }

}