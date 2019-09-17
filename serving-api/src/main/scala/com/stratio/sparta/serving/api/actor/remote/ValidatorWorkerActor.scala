/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor.remote

import akka.actor.{ActorRef, Props}
import com.stratio.sparta.serving.api.actor.WorkflowValidatorActor
import com.stratio.sparta.serving.api.actor.WorkflowValidatorActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import org.json4s.native.Serialization.read
import ValidatorWorkerActor._
import com.stratio.sparta.serving.core.models.RocketModes

import scala.concurrent.Future
import scala.util.Try

class ValidatorWorkerActor extends WorkerActor {

  val dispatcherActorName = AkkaConstant.ValidatorDispatcherActorName
  val rocketMode = RocketModes.Validator
  val workerTopic = ValidatorWorkerTopic

  lazy val workflowValidatorActor = context.actorOf(WorkflowValidatorActor.props)

  override def workerPreStart(): Unit = {
    SparkContextFactory.getOrCreateStandAloneXDSession(None)
  }

  override def executeJob(job: String, jobSender: ActorRef): Future[Unit] = Future {
    Try {
      val validateWorkflowSteps = read[ValidateWorkflowStepsJob](job)
      log.debug(s"Sending ValidateWorkflowStepsJob in worker with sender ${jobSender.path}")
      workflowValidatorActor.tell(validateWorkflowSteps, jobSender)
    }.recover { case _ =>
      val validateWorkflowWithoutExContext = read[ValidateWorkflowWithoutExContextJob](job)
      log.debug(s"Sending ValidateWorkflowWithoutExContextJob in worker with sender ${jobSender.path}")
      workflowValidatorActor.tell(validateWorkflowWithoutExContext, jobSender)
    }.recover { case _ =>
      val validateWorkflowIdWithExContext = read[ValidateWorkflowIdWithExContextJob](job)
      log.debug(s"Sending ValidateWorkflowIdWithExContextJob in worker with sender ${jobSender.path}")
      workflowValidatorActor.tell(validateWorkflowIdWithExContext, jobSender)
    }.recover { case _ =>
      log.warn(s"Invalid job message in Validator worker actor: $job")
    }
  }
}

object ValidatorWorkerActor {

  val ValidatorWorkerTopic = "validator-worker"

  def props: Props = Props[ValidatorWorkerActor]

}