/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon.service

import akka.http.scaladsl.model.StatusCode
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.utils.Utils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.marathon.factory.MarathonApplicationFactory
import com.stratio.sparta.serving.core.marathon.{MarathonApplication, MarathonHealthCheck}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.MarathonOauthTokenUtils._
import com.typesafe.config.Config

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

class MarathonService(marathonUpAndDownComponent: MarathonUpAndDownComponent) extends SpartaSerializer {

  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get
  lazy val marathonApiRetryAttempts: Int = Try(marathonConfig.getInt("api.retry.attempts")).toOption.getOrElse(DefaultRetryAttempts)
  lazy val marathonApiRetrySleep: Int = Try(marathonConfig.getInt("api.retry.sleep")).toOption.getOrElse(DefaultRetrySleep)
  lazy val marathonApiTimeout: Int = Try(marathonConfig.getInt("api.timeout")).toOption.getOrElse(DefaultMaxTimeOutInMarathonRequests)

  def launch(marathonApplication: MarathonApplication): Unit =
    Utils.retry(marathonApiRetryAttempts, marathonApiRetrySleep) {
      val launchRequest = marathonUpAndDownComponent.upApplication(marathonApplication, Try(getToken).toOption)
      Await.result(launchRequest, marathonApiTimeout millis) match {
        case response: (StatusCode, String) =>
          log.info(s"Workflow App ${marathonApplication.id} launched to marathon api with status code ${response._1} and response ${response._2}")
          val statusResponse = response._1.intValue()
          if(statusResponse >= 400 && statusResponse < 600)
            throw new Exception(
              s"Invalid status response submitting marathon application with status code $statusResponse and response ${response._2}")
          else log.info(s"Workflow App ${marathonApplication.id} launched with successful response")
        case _ =>
          log.info(s"Workflow App ${marathonApplication.id} launched but the response is not serializable")
      }
    }

  def kill(containerId: String): Unit =
    Utils.retry(marathonApiRetryAttempts, marathonApiRetrySleep) {
      val killRequest = marathonUpAndDownComponent.killDeploymentsAndDownApplication(containerId, Try(getToken).toOption)
      Await.result(killRequest, marathonApiTimeout millis) match {
        case responses: Seq[(StatusCode, String)] =>
          val statusFailed = responses.forall { case (statusResponse,  _) =>
            (statusResponse.intValue() >= 400) && (statusResponse.intValue() < 600)
          }
          if(statusFailed)
            throw new Exception(s"Invalid status response killing marathon application with responses: ${responses.mkString(",")}")
          else {
            log.info(s"Workflow App $containerId correctly killed with responses: ${responses.mkString(",")}")
          }
        case _ =>
          log.info(s"Workflow App $containerId killed but the response is not serializable")
      }
    }
}

object MarathonService {

  protected[core] def getHealthChecks(workflowModel: Workflow): Option[Seq[MarathonHealthCheck]] = {
    val workflowHealthcheckSettings =
      HealthChecks.fromObject(workflowModel.settings.global.marathonDeploymentSettings)

    Option(Seq(MarathonHealthCheck(
      protocol = "HTTP",
      path = Option(MarathonApplicationFactory.SparkUIHealthCheckPath),
      portIndex = Option(MarathonApplicationFactory.SparkUIHealthCheckPortIndex),
      gracePeriodSeconds = workflowHealthcheckSettings.gracePeriodSeconds,
      intervalSeconds = workflowHealthcheckSettings.intervalSeconds,
      timeoutSeconds = workflowHealthcheckSettings.timeoutSeconds,
      maxConsecutiveFailures = workflowHealthcheckSettings.maxConsecutiveFailures,
      ignoreHttp1xx = Option(false)
    )))
  }


  protected[core] def calculateMaxTimeout(healthChecks: Option[Seq[MarathonHealthCheck]]): Int =
    healthChecks.fold(AppConstant.DefaultAwaitWorkflowChangeStatusSeconds) {
      healthCk => {
        val applicationHck = healthCk.head
        import applicationHck._
        gracePeriodSeconds + (intervalSeconds + timeoutSeconds) * maxConsecutiveFailures
      }
    }

  case class HealthChecks(gracePeriodSeconds: Int = DefaultGracePeriodSeconds,
                          intervalSeconds: Int = DefaultIntervalSeconds,
                          timeoutSeconds: Int = DefaultTimeoutSeconds,
                          maxConsecutiveFailures: Int = DefaultMaxConsecutiveFailures)

  object HealthChecks {

    private def toIntOrElse(integerString: Option[JsoneyString], defaultValue: Int): Int =
      Try(integerString.map(_.toString.toInt).get).toOption.getOrElse(defaultValue)

    protected[core] def fromObject(settings: Option[MarathonDeploymentSettings]): HealthChecks =
      settings match {
        case None => HealthChecks()
        case Some(marathonSettings) =>
          HealthChecks(
            gracePeriodSeconds = toIntOrElse(marathonSettings.gracePeriodSeconds, DefaultGracePeriodSeconds),
            intervalSeconds = toIntOrElse(marathonSettings.intervalSeconds, DefaultIntervalSeconds),
            timeoutSeconds = toIntOrElse(marathonSettings.timeoutSeconds, DefaultTimeoutSeconds),
            maxConsecutiveFailures = toIntOrElse(marathonSettings.maxConsecutiveFailures, DefaultMaxConsecutiveFailures)
          )
      }
  }

}