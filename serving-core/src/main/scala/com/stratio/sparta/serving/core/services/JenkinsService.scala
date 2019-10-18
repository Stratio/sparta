/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpMethods, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.utils.HttpRequestUtils

import scala.concurrent.Future
import scala.util.Try

object JenkinsService{

  lazy val isCiCdEnabled: Boolean =
    Try(SpartaConfig.getCiCdConfig().get.getString(CiCdEnabledConf).toBoolean).getOrElse(false)

  lazy val ReleaseCandidateJobSuffix = "-RC"
  lazy val ReleaseJobSuffix = "-RELEASE"

  case class JenkinsBasicAuthCred(user: String, token: String)

  def jobName(workflowName: String, isRelease: Boolean): String =
    s"$workflowName${if (isRelease) ReleaseJobSuffix else ReleaseCandidateJobSuffix}"
}

case class JenkinsService(actorSystem: ActorSystem) extends HttpRequestUtils {

  implicit val system: ActorSystem = actorSystem
  implicit val exContext = actorSystem.dispatcher
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  import JenkinsService._

  lazy val jenkinsUrl: String =
    Try(SpartaConfig.getCiCdConfig().get.getString(JenkinsUrlConf))
    .getOrElse(throw new RuntimeException("Jenkins URL must be provided"))

  lazy val jenkinsTestTags = Try(SpartaConfig.getCiCdConfig().get.getString(JenkinsTestTagsConf))

  lazy val userCredentials: Option[JenkinsBasicAuthCred] =
    for {
      user <- sys.env.get(JenkinsUserEnvVar)
      apiToken <- sys.env.get(JenkinsApiTokenEnvVar)
    } yield JenkinsBasicAuthCred(user, apiToken)

  def build(workflow: Workflow): Future[String] =
    withJenkinsCredentials{ jenkinsCred =>

      val credentials = BasicHttpCredentials(jenkinsCred.user, jenkinsCred.token)
      doRequestUri(
        uri = buildUri(workflow, isRelease = false),
        method = HttpMethods.POST,
        httpCredentials = Some(credentials)
      ).map{
        case (statusCode, _) if statusCode.isSuccess =>
          log.info(s"Release candidate job launched for workflow with id: ${workflow.id}")
          "OK"
        case (statusCode, strResponse) =>
          val unknownError = s"Error received with statusCode: $statusCode and response: $strResponse"
          log.warn(unknownError)
          if (statusCode == StatusCodes.NotFound){
            if (log.isDebugEnabled) {
              log.debug(s"Job not found using URI: ${buildUri(workflow, isRelease = false)}")
            }
            throw new RuntimeException("Job not found: must be created in Jenkins")
          } else {
            throw new RuntimeException(unknownError)
          }
      }
    }

  // TODO refactor along with build
  def release(workflow: Workflow): Future[String] =
    withJenkinsCredentials{ jenkinsCred =>
      val credentials = BasicHttpCredentials(jenkinsCred.user, jenkinsCred.token)
      doRequestUri(
        uri = buildUri(workflow, isRelease = true),
        method = HttpMethods.POST,
        httpCredentials = Some(credentials)
      ).map{
        case (statusCode, _) if statusCode.isSuccess =>
          log.info(s"Release job launched for workflow with id: ${workflow.id}")
          "OK"
        case (statusCode, strResponse) =>
          val unknownError = s"Error received with statusCode: $statusCode and response: $strResponse"
          log.warn(unknownError)
          if (statusCode == StatusCodes.NotFound){
            throw new RuntimeException("Job not found: must be created in Jenkins")
          } else {
            throw new RuntimeException(unknownError)
          }
      }
    }

  /**
    * Uri format example
    *   Jenkins_url_base/job/nivel1_grupo/job/nivel2_grupo/job/<workflowname>-RC/buildWithParameters?<queryparam>
    *   <queryparam> => workflowName=WN&workflowVersion=WV&workflowGroup=WG
    */
  private def buildUri(workflow: Workflow, isRelease: Boolean): Uri = {
    val jobName = JenkinsService.jobName(workflow.name, isRelease)
    val groupsWithoutHome = workflow.group.name.split("/").filter(_.nonEmpty).tail
    val groupsPath = (AppConstant.tenantIdInstanceNameWithDefault +: groupsWithoutHome).mkString("/job/", "/job/", "/job/")
    val parameters: Map[String, String] = {
      val tokenQP: Option[(String,String)] = sys.env.get("JENKINS_TOKEN_NAME").map("token" -> _)
      val workflowNameQP = "workflowName" -> workflow.name
      val workflowGroupQP = "workflowGroup" -> workflow.group.name
      val workflowVersionQP = "workflowVersion" -> workflow.version.toString
      val testTagsQP = jenkinsTestTags.toOption.map("testTags" -> _)

      val workflowPropsMap: Map[String, String] = Map(workflowNameQP, workflowGroupQP, workflowVersionQP)
      val mapWithTokenQP = tokenQP.map(token => workflowPropsMap + token).getOrElse(workflowPropsMap)
      val mapWithTestTag = testTagsQP.map(testTagTuple => mapWithTokenQP + testTagTuple).getOrElse(mapWithTokenQP)
      mapWithTestTag
    }

    val url = s"$jenkinsUrl$groupsPath$jobName/buildWithParameters"
    val uriWithQuery = Uri(url).withQuery(Query(parameters))
    uriWithQuery
  }


  def withJenkinsCredentials[T](block: JenkinsBasicAuthCred => T): T =
    if (!isCiCdEnabled){
      throw new RuntimeException("CI/CD must be enabled to build a job")
    } else {
      userCredentials.map{ userCred =>
        block(userCred)
      }.getOrElse(throw new RuntimeException("CI/CD credentials not found"))
    }

}
