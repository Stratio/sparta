/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor._
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.models.DebugResults
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.actor.DebugWorkflowInMemoryApi._
import com.stratio.sparta.serving.core.actor.LauncherActor.Debug
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow
import com.stratio.sparta.serving.core.services.DebugWorkflowService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

class DebugWorkflowActor(
                          val curatorFramework: CuratorFramework,
                          inMemoryDebugWorkflowApi: ActorRef,
                          launcherActor: ActorRef
                        )
                        (implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with Json4sJacksonSupport with FileActorUtils with SpartaSerializer with ActionUserAuthorize {

  import DebugWorkflowActor._

  val ResourceWorkflow = "Workflows"
  val ResourceFiles = "Files"
  val debugService = new DebugWorkflowService(curatorFramework)

  val targetDir = "debug"
  val temporalDir = "/tmp/sparta/debug"
  val apiPath = s"${HttpConstant.DebugWorkflowsPath}/download"

  //scalastyle:off
  override def receive: Receive = {
    case CreateDebugWorkflow(workflow, user) => createDebugWorkflow(workflow, user)
    case DeleteById(id, user) => deleteByID(id, user)
    case DeleteAll(user) => deleteAll(user)
    case Find(id, user) => find(id, user)
    case FindAll(user) => findAll(user)
    case GetResults(id, user) => getResults(id, user)
    case Run(id, user) => run(id, user)
    case UploadFile(files, id, user) => uploadFile(files, id, user)
    case DeleteFile(fileName, user) => deleteFile(fileName, user)
    case DownloadFile(fileName, user) => downloadFile(fileName, user)
  }
  //scalastyle:on

  def createDebugWorkflow(debugWorkflow: DebugWorkflow, user: Option[LoggedUser]): Unit = {
    authorizeActionsByResourceId[ResponseDebugWorkflow](
      user,
      Map(ResourceWorkflow -> Create),
      debugWorkflow.authorizationId
    ) {
      debugService.createDebugWorkflow(debugWorkflow)
    }
  }

  def deleteByID(id: String, user: Option[LoggedUser]): Unit = {
    val authorizationId = debugService.findByID(id).map(_.authorizationId).getOrElse("N/A")
    authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Delete), authorizationId) {
      debugService.deleteDebugWorkflowByID(id)
    }
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    val resourcesId = debugService.findAll.map(_.authorizationId)
    authorizeActionsByResourcesIds(user, Map(ResourceWorkflow -> Delete), resourcesId) {
      debugService.deleteAllDebugWorkflows
    }
  }

  def find(id: String, user: Option[LoggedUser]): Unit =
    authorizeResultByResourceId(user, Map(ResourceWorkflow -> View), Option(inMemoryDebugWorkflowApi)) {
      FindMemoryDebugWorkflow(id)
    }

  def findAll(user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(user, Map(ResourceWorkflow -> View), Option(inMemoryDebugWorkflowApi)) {
      FindAllMemoryDebugWorkflows
    }

  def getResults(id: String, user: Option[LoggedUser]): Unit =
    authorizeResultByResourceId( user, Map(ResourceWorkflow -> View), Option(inMemoryDebugWorkflowApi)) {
      FindMemoryDebugResultsWorkflow(id)
    }

  def run(id: String, user: Option[LoggedUser]): Unit = {
    launcherActor.forward(Debug(id.toString, user))
  }

  def downloadFile(fileName: String, user: Option[LoggedUser]): Unit =
    authorizeActions[SpartaFileResponse](user, Map(ResourceFiles -> Download)) {
      browseFile(fileName)
    }

  def deleteFile(fileName: String, user: Option[LoggedUser]): Unit =
    authorizeActions[Response](user, Map(ResourceFiles -> Delete)) {
      deleteFile(fileName)
    }

  def uploadFile(files: Seq[BodyPart], id: String, user: Option[LoggedUser]): Unit =
    authorizeActions[SpartaFilesResponse](user, Map(ResourceFiles -> View)) {
      uploadFiles(files, useTemporalDirectory = true, Some(id))
    }
}

object DebugWorkflowActor extends SLF4JLogging {

  case class CreateDebugWorkflow(debugWorkflow: DebugWorkflow, user: Option[LoggedUser])

  case class Find(id: String, user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class DeleteById(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class GetResults(id: String, user: Option[LoggedUser])

  case class Run(id: String, user: Option[LoggedUser])

  case class UploadFile(files: Seq[BodyPart], workflowId: String, user: Option[LoggedUser])

  case class DownloadFile(fileName: String, user: Option[LoggedUser])

  case class DeleteFile(fileName: String, user: Option[LoggedUser])


  type ResponseDebugWorkflow = Try[DebugWorkflow]

  type ResponseDebugWorkflows = Try[Seq[DebugWorkflow]]

  type ResponseResult = Try[DebugResults]

  type ResponseRun = Try[DateTime]

  type ResponseAny = Try[Any]

  type Response = Try[Unit]

  type SpartaFilePath = Try[String]

  type SpartaFileResponse = Try[SpartaFile]

  type SpartaFilesResponse = Try[Seq[SpartaFile]]

}
