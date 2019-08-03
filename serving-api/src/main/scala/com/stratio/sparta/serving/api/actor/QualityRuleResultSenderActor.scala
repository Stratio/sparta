/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, Cancellable, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.governance.{GovernanceQualityResult, QualityRuleResult}
import com.stratio.sparta.serving.core.utils.HttpRequestUtils
import org.json4s.jackson.Serialization.write

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class QualityRuleResultSenderActor extends Actor with HttpRequestUtils with SpartaSerializer {

  import QualityRuleResultSenderActor._

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val system = context.system
  implicit val actorMaterializer = ActorMaterializer()

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  lazy val qualityRuleResultsService = PostgresDaoFactory.qualityRuleResultPgService
  lazy val governancePushTickTask: Cancellable =
    context.system.scheduler.schedule(1 minutes, governancePushDuration, self, GovernancePushTick)
  lazy val enabled = Try(SpartaConfig.getDetailConfig().get.getString("lineage.enable").toBoolean).getOrElse(false)
  lazy val uri = Try(SpartaConfig.getGovernanceConfig().get.getString("http.uri"))
    .getOrElse("https://governance.labs.stratio.com/dictionary")
  lazy val postEndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.http.post.endpoint"))
    .getOrElse("v1/quality/metrics")
  lazy val governancePushDuration: FiniteDuration = 15 minute
  lazy val noTenant = Some("NONE")
  lazy val current_tenant= AppConstant.EosTenant.orElse(noTenant)
  lazy val rawHeaders = Seq(RawHeader("X-TenantID", current_tenant.getOrElse("NONE")))

  override def preStart(): Unit = {
    if (enabled) {
      mediator ! Subscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
      governancePushTickTask
    }
  }

  override def receive: Receive = {
    case GovernancePushTick =>
      log.debug(s"Received GovernancePushTickand LINEAGE_ENABLED is set to $enabled")
      governancePushRest()
    case workflowExecutionStatusChange: ExecutionStatusChange =>
      if (
        (workflowExecutionStatusChange.executionChange.newExecution.lastStatus.state == StoppedByUser ||
          workflowExecutionStatusChange.executionChange.newExecution.lastStatus.state == Finished) &&
          (workflowExecutionStatusChange.executionChange.originalExecution.lastStatus.state !=
            workflowExecutionStatusChange.executionChange.newExecution.lastStatus.state)
      ) governancePushRest()
  }

  override def postStop(): Unit = {
    if (enabled) {
      mediator ! Unsubscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
      governancePushTickTask.cancel()
    }
    super.postStop()
  }

  def sendResultsToApi(qualityRuleResult: QualityRuleResult): Future[(String, Boolean)] = {

    val qualityRuleGovernance: GovernanceQualityResult =
      GovernanceQualityResult.parseSpartaResult(qualityRuleResult, noTenant)

    val qualityRuleResultJson = write(qualityRuleGovernance)

    val resultPost = doRequest(
      uri = uri,
      resource = postEndpoint,
      method = HttpMethods.POST,
      body = Option(qualityRuleResultJson),
      cookies = Seq.empty,
      headers = rawHeaders
    )

    val result = for {
      (status, response) <- resultPost
      _ <- qualityRuleResultsService.upsert(qualityRuleResult.copy(sentToApi = true, warning = false))
    } yield {
      log.debug(s"Quality rule sent with requestBody $qualityRuleResultJson and receive status: ${status.value} and response: $response")
      response
    }

    result.onComplete { completedAction: Try[String] =>
      completedAction match {
        case Success(response) =>
          log.info(s"Sent results for quality rule ${qualityRuleResult.id.getOrElse("Without id!")} with response: $response")
        case Failure(e) =>
          log.error(s"Error sending data for quality rule ${qualityRuleResult.id.getOrElse("Without id!")} to API with POST method", e)
      }
    }
    result.map(_ => (qualityRuleResult.id.getOrElse("Without id!"), true))
  }

  def governancePushRest(): Unit = {
    if (enabled) {
      val finalResult: Future[List[String]] = for {
        unsentRules <- qualityRuleResultsService.findAllUnsent()
        result <- Future.sequence(
          unsentRules.map(rule => sendResultsToApi(rule))
        )
      } yield {
        result.map(res => res._1)
      }

      finalResult.onSuccess {
        case list => log.debug(s"Correctly sent results for quality rules: ${list.mkString(",")}")
      }
    }
  }

}

object QualityRuleResultSenderActor {

  def props: Props = Props[QualityRuleResultSenderActor]

  case object GovernancePushTick

}
