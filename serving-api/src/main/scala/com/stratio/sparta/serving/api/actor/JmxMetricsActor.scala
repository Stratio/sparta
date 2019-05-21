/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.lang.management.ManagementFactory

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.JmxMetricsActor._
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.WorkflowStatusEnum
import com.stratio.sparta.serving.core.utils.SpartaClusterUtils
import javax.management.ObjectName

import org.joda.time.DateTime

import scala.beans.BeanProperty
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * JmxMetricActor retrieves states of executions and exposes them though Jmx.
 */
class JmxMetricsActor extends Actor
  with SLF4JLogging
  with SpartaClusterUtils {

  implicit val executionContext: ExecutionContext = context.dispatcher
  val executionPgService = PostgresDaoFactory.executionPgService

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  val jmxMetricsTick = context.system.scheduler.schedule(0 seconds, JmxTickDuration, self, JmxMetricsTick)

  val mBeanServer = ManagementFactory.getPlatformMBeanServer

  // Due to they are jmx beans we need to use mutable collections :(
  val jmxMetricMap = scala.collection.mutable.Map.empty[String, JmxMetric]
  val groupedJmxMetricMap = scala.collection.mutable.Map.empty[String, JmxMetric]

  override def preStart(): Unit = mediator ! Subscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)

  override def receive: Receive = {
    case JmxMetricsTick =>
      if(isThisNodeClusterLeader(cluster)) {
        buildJmxMetricStateMapFromDatabase.onComplete {
          case Success(_) =>
            context.become(listening)
          case Failure(ex) =>
            log.error(ex.getLocalizedMessage, ex)
        }
      }
  }

  def listening: Receive = {
    case executionStatusChange: ExecutionStatusChange =>
      val id = executionStatusChange.executionChange.newExecution.id.getOrElse("-1")
      val name = executionStatusChange.executionChange.newExecution.getWorkflowToExecute.name
      val workflowStatusEnum = executionStatusChange.executionChange.newExecution.resumedStatus
        .getOrElse(WorkflowStatusEnum.NotDefined)
      log.debug(s"Changing jmx state for workflow: $name to ${workflowStatusEnum.toString}")

      if(jmxMetricMap.exists(_._1 == id)){
        jmxMetricMap(id).value = workflowStatusEnum.id
      } else {
        val newMetric = new JmxMetric(executionStatusChange.executionChange.newExecution.resumedStatus.get.id)
        val id = executionStatusChange.executionChange.newExecution.id.getOrElse("-1")


        registerMetricBean(
          newMetric,
          ObjectName.getInstance(s"com.stratio.executions:type=WorkflowExecution,key=${id}")
        )
        jmxMetricMap.put(id, newMetric)
      }

      if(groupedJmxMetricMap.exists(_._1 == name)){
        groupedJmxMetricMap(name).value = workflowStatusEnum.id
      } else {
        val newMetric = new JmxMetric(executionStatusChange.executionChange.newExecution.resumedStatus.get.id)
        val groupName = executionStatusChange.executionChange.newExecution.getWorkflowToExecute.group.name
        val version = executionStatusChange.executionChange.newExecution.getWorkflowToExecute.version
        val name = executionStatusChange.executionChange.newExecution.getWorkflowToExecute.name

        registerMetricBean(
          newMetric,
          ObjectName.getInstance(s"com.stratio.executions:type=WorkflowGroupedExecution,key=$groupName/${name}_v$version")
        )
        groupedJmxMetricMap.put(name, newMetric)
      }
    case JmxMetricsTick =>
      if(!isThisNodeClusterLeader(cluster)) context.become(receive)
  }


  def buildJmxMetricStateMapFromDatabase: Future[Unit] =
    executionPgService.findRunningExecutions().map { executions =>
      val workflowStatuses: Seq[WorkflowStatus] = for {
        execution <- executions
        date <- execution.resumedDate
        status <- execution.resumedStatus
      } yield {
        WorkflowStatus(
          execution.id,
          execution.getWorkflowToExecute.name,
          execution.getWorkflowToExecute.version,
          execution.getWorkflowToExecute.group.name,
          date,
          status)
      }

      // Step 1) It creates a map of WorkflowGroupName/WorkflowName_WorkflowVersion as key, as its Workflow status as value.
      // Later it groups by key an takes the first element (the freshest execution that it needs for creating a metric.
      val firstWorkflowStatusesGrouped:Map[String, WorkflowStatus] = workflowStatuses
        .map { case(workflowStatus) => s"${workflowStatus.groupName}/${workflowStatus.name}_v${workflowStatus.version}" -> workflowStatus }
        .groupBy { case(key, _) => key }
        .flatMap { case(_, workflowStatus) =>
          workflowStatus.sortBy { case(_, workflowStatus) => workflowStatus.date.getMillis }
            .reverse
            .headOption
        }

      // Step 2) Now, and thanks to the result obtained in the previous step, it is going to register these metrics.
      firstWorkflowStatusesGrouped.foreach{ case(key, workflowStatus) =>
        val newMetric = new JmxMetric(workflowStatus.workflowStatusEnum.id)

        registerMetricBean(
          newMetric,
          ObjectName.getInstance(s"com.stratio.executions:type=WorkflowGroupedExecution,key=$key")
        )
        groupedJmxMetricMap.put(workflowStatus.name, newMetric)
      }
    }


  override def postStop(): Unit = {
    mediator ! Unsubscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    jmxMetricsTick.cancel()
    super.postStop()
  }

  def registerMetricBean(metric: JmxMetric, objectName: ObjectName): Unit = {
    if(!mBeanServer.isRegistered(objectName))
      mBeanServer.registerMBean(metric, objectName)
  }
}

object JmxMetricsActor {

  def props:Props = Props[JmxMetricsActor]

  case object JmxMetricsTick

  val JmxTickDuration = 15 seconds

  // Take care about the name, because it should be exactly the same that the class that is going to extend it finished with "MBean"
  trait JmxMetricMBean {
    def getValue(): Int
    def setValue(d: Int): Unit
  }

  // Due to it is a Jmx bean we need to have a mutable value :(
  class JmxMetric(@BeanProperty var value: Int) extends JmxMetricMBean

  protected case class WorkflowStatus(id: Option[String],
                                      name: String,
                                      version: Long,
                                      groupName: String,
                                      date: DateTime,
                                      workflowStatusEnum: WorkflowStatusEnum)

}