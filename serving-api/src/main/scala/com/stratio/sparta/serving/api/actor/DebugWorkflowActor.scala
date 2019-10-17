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
import com.stratio.sparta.serving.api.constants.HttpConstant._
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.actor.LauncherActor.Debug
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.HdfsFilesService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.joda.time.DateTime
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class DebugWorkflowActor(
                          launcherActor: ActorRef
                        )
  extends Actor with Json4sJacksonSupport with FileActorUtils with SpartaSerializer with ActionUserAuthorize {

  import DebugWorkflowActor._

  val ResourceWorkflow = "Workflows"
  val ResourceFiles = "Files"
  val debugPgService = PostgresDaoFactory.debugWorkflowPgService

  lazy val hdfsFilesService = HdfsFilesService()

  val targetDir = "debug"
  val temporalDir = "/tmp/sparta/debug"
  val apiPath = s"${HttpConstant.DebugWorkflowsPath}/download"

  //scalastyle:off
  def receiveApiActions(action: Any): Unit = action match {
    case CreateDebugWorkflow(workflow, user) => createDebugWorkflow(workflow, user)
    case DeleteById(id, user) => deleteByID(id, user)
    case DeleteAll(user) => deleteAll(user)
    case Find(id, user) => find(id, user)
    case FindAll(user) => findAll(user)
    case GetResults(id, user) => getResults(id, user)
    case Run(id, user) => run(id, user)
    case RunWithWorkflowIdExecutionContext(workflowIdExecutionContext, user) => runWithExecutionContext(workflowIdExecutionContext, user)
    case UploadFile(files, id, user) => uploadFile(files, id, user)
    case DeleteFile(fileName, user) => deleteFile(fileName, user)
    case DownloadFile(fileName, user) => downloadFile(fileName, user)
    case ListFiles(user) => browseFiles(user)
  }

  //scalastyle:on

  def createDebugWorkflow(debugWorkflow: DebugWorkflow, user: Option[LoggedUser]): Unit = {
    authorizeActionsByResourceId(
      user,
      Map(ResourceWorkflow -> Create),
      debugWorkflow.authorizationId
    ) {
      debugPgService.createDebugWorkflow(debugWorkflow)
    }
  }

  def deleteByID(id: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      debugWorkflow <- debugPgService.findDebugWorkflowById(id)
    } yield {
      val authorizationId = debugWorkflow.authorizationId
      authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Delete), authorizationId, senderResponseTo) {
        debugPgService.deleteDebugWorkflowByID(id)
      }
    }
  }

  def deleteAll(user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      allDebugs <- debugPgService.findAll()
    } yield {
      val resourcesId = allDebugs.map(_.authorizationId)
      authorizeActionsByResourcesIds(user, Map(ResourceWorkflow -> Delete), resourcesId, senderResponseTo) {
        debugPgService.deleteAllDebugWorkflows()
      }
    }
  }

  def find(id: String, user: Option[LoggedUser]): Unit = {
    authorizeActions(user, Map(ResourceWorkflow -> View)) {
      debugPgService.findDebugWorkflowById(id)
    }
  }

  def findAll(user: Option[LoggedUser]): Unit = {
    authorizeActions(user, Map(ResourceWorkflow -> View)) {
      debugPgService.findAll()
    }
  }

  def getResults(id: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      debugWorkflow <- debugPgService.findDebugWorkflowById(id)
    } yield {
      val resourcesId = debugWorkflow.authorizationId
      authorizeActionsByResourceId(user, Map(ResourceWorkflow -> View), resourcesId, senderResponseTo) {
        debugPgService.getResultsByID(id)
      }
    }
  }

  def run(id: String, user: Option[LoggedUser]): Unit =
    runWithExecutionContext(WorkflowIdExecutionContext(id, ExecutionContext()), user)

  def runWithExecutionContext(
                               workflowIdExecutionContext: WorkflowIdExecutionContext,
                               user: Option[LoggedUser]
                             ): Unit =
    launcherActor.forward(Debug(workflowIdExecutionContext, user))

  def downloadFile(fileName: String, user: Option[LoggedUser]): Unit =
    authorizeActions[SpartaFileResponse](user, Map(ResourceFiles -> Download)) {
      Try {
        hdfsFilesService.downloadMockDataFile(fileName, temporalDir)
      }.flatMap(localFilePath => browseFile(localFilePath)).orElse(browseFile(fileName))
    }

  def deleteFile(fileName: String, user: Option[LoggedUser]): Unit =
    authorizeActions[Response](user, Map(ResourceFiles -> Delete)) {
      Try(hdfsFilesService.deleteMockData(fileName)).orElse(deleteFile(fileName))
    }

  def uploadFile(files: Seq[BodyPart], id: String, user: Option[LoggedUser]): Unit =
    authorizeActions[SpartaFilesResponse](user, Map(ResourceFiles -> View)) {
      uploadFiles(files, useTemporalDirectory = true, Some(id)) match {
        case Success(spartaFiles) =>
          Try {
            spartaFiles.map { file =>
              val path = hdfsFilesService.uploadMockDataFile(file.path)
              SpartaFile("", "", path)
            }
          }
        case Failure(_) =>
          uploadFiles(files)
      }
    }

  def browseFiles(user: Option[LoggedUser]): Unit =
    authorizeActions[SpartaFilesResponse](user, Map(ResourceFiles -> View)) {
      Try {
        hdfsFilesService.browseMockData.flatMap { fileStatus =>
          if (fileStatus.isFile)
            Option(SpartaFile(
              fileStatus.getPath.getName,
              s"$url/${fileStatus.getPath.getName}",
              fileStatus.getPath.toUri.toString))
          else None
        }
      } match {
        case Success(files) =>
          Try(files)
        case Failure(e: java.io.FileNotFoundException) =>
          Try(Seq.empty[SpartaFile])
        case Failure(e: Exception) =>
          log.warn(s"Error getting files with Hdfs api, getting it from local directory. ${e.getLocalizedMessage}")
          browseDirectory()
      }
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

  case class RunWithWorkflowIdExecutionContext(
                                                workflowIdExecutionContext: WorkflowIdExecutionContext,
                                                user: Option[LoggedUser]
                                              )

  case class UploadFile(files: Seq[BodyPart], workflowId: String, user: Option[LoggedUser])

  case class DownloadFile(fileName: String, user: Option[LoggedUser])

  case class DeleteFile(fileName: String, user: Option[LoggedUser])

  case class ListFiles(user: Option[LoggedUser])

  type ResponseDebugWorkflow = Try[DebugWorkflow]

  type ResponseDebugWorkflows = Try[Seq[DebugWorkflow]]

  type ResponseResult = Try[DebugResults]

  type ResponseRun = Try[DateTime]

  type SpartaFilePath = Try[String]

  type SpartaFileResponse = Try[SpartaFile]

  type SpartaFilesResponse = Try[Seq[SpartaFile]]
}
