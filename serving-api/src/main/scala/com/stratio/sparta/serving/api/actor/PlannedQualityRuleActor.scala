/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.serving.api.actor.PlannedQualityRuleActor.RetrievePlannedQualityRulesTick
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.governance.GovernanceQualityRule
import com.stratio.sparta.serving.core.utils.{HttpRequestUtils, PlannedQualityRulesUtils}
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PlannedQualityRuleActor extends Actor
  with HttpRequestUtils
  with SpartaSerializer{

  import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._

  val EpochTime = 0L
  val DefaultPeriodInterval = 60000L

  val retrievePlannedQualityRulesPeriodicity = Try(SpartaConfig.getGovernanceConfig().
    get.getLong("qualityrules.planned.period.interval")).getOrElse(DefaultPeriodInterval)

  val plannedQualityRulePgService = PostgresDaoFactory.plannedQualityRulePgService
  val plannedQualityRulesUtils = new PlannedQualityRulesUtils()

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val system = context.system
  implicit val actorMaterializer = ActorMaterializer()

  val cluster = Cluster(system)

  lazy val uri = Try(SpartaConfig.getGovernanceConfig().get.getString("http.uri"))
    .getOrElse("https://governance.labs.stratio.com/dictionary")
  lazy val getPlannedQREndpoint = Try(SpartaConfig.getGovernanceConfig().get.getString("qualityrules.planned.http.get.endpoint"))
    .getOrElse("v1/quality/quality/searchByModifiedAt?modifiedAtAfter=")
  lazy val noTenant = Some("NONE")
  lazy val current_tenant= AppConstant.EosTenant.orElse(noTenant)
  lazy val rawHeaders = Seq(RawHeader("X-TenantID", current_tenant.getOrElse("NONE")))

  override def preStart(): Unit = {
    context.system.scheduler.schedule(1 minutes, Duration.create(retrievePlannedQualityRulesPeriodicity, MILLISECONDS),
      self, RetrievePlannedQualityRulesTick)
    self ! RetrievePlannedQualityRulesTick
  }

  override def receive: Receive = {
    case RetrievePlannedQualityRulesTick =>
      log.debug(s"Received RetrievePlannedQualityRulesTick")

      val plannedQualityRules: Future[Seq[SpartaQualityRule]] =
        for{
          latestModificationDate <- plannedQualityRulePgService.getLatestModificationDate()
          result <- retrievePlannedQualityRules(parseMillisToDate(latestModificationDate))
        } yield result

      plannedQualityRules.onComplete{
        case Success(value) =>
          for {
            filteredQRs <- value.filter(_.validSpartaQR)
            scheduledQr <- plannedQualityRulesUtils.createOrUpdateTaskPlanning(filteredQRs)
          } {
            plannedQualityRulePgService.createOrUpdate(scheduledQr._2)
          }

        case Failure(ex) =>
          log.error(ex.getLocalizedMessage, ex)
      }
  }

  override def postStop(): Unit = {
    super.postStop()
  }

  private def parseMillisToDate(value: Option[Long]): String = {
    new DateTime(value.getOrElse(EpochTime), DateTimeZone.UTC).toString()
  }

  private def retrievePlannedQualityRules(modTime: String): Future[Seq[SpartaQualityRule]] = {
    import org.json4s.native.Serialization.read

    val plannedRulesFromApi = getPlannedQualityRulesFromApi(modTime)

    val seqUnparsedPlannedQualityRules = plannedRulesFromApi.map(rules =>
      read[Seq[GovernanceQualityRule]](rules))

    val seqPlannedQualityRules = for {
      unparsedPlannedQualityRule <- seqUnparsedPlannedQualityRules
    } yield {
      unparsedPlannedQualityRule.flatMap(_.parse())
    }

    seqPlannedQualityRules
  }

  private def getPlannedQualityRulesFromApi(modTime: String): Future[String] = {
    val query = URLEncoder.encode(modTime, StandardCharsets.UTF_8.toString)

    val resultGet = doRequest(
      uri = uri,
      resource = getPlannedQREndpoint.concat(query),
      body = None,
      cookies = Seq.empty,
      headers = rawHeaders
    )

    resultGet.map{ case(status,rules) =>
      log.debug(s"Quality rule request for modification date $EpochTime received with " +
        s"status ${status.value} and response $rules")
      rules
    }
  }
}

object PlannedQualityRuleActor {

  def props: Props = Props[PlannedQualityRuleActor]

  case object RetrievePlannedQualityRulesTick

}
