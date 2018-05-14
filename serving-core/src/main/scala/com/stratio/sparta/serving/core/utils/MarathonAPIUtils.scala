/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.HttpMethods
import akka.io.Tcp.Message
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.jayway.jsonpath.{Configuration, JsonPath, ReadContext}
import com.stratio.sparta.serving.core.marathon.OauthTokenUtils.{expireToken, getToken}
import com.stratio.tikitakka.common.util.HttpRequestUtils
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap.option2NotBlankOption
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.utils.MarathonApiError._
import net.minidev.json.JSONArray
import org.slf4j.Logger

import scala.util.{Failure, Properties, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._

class MarathonAPIUtils(system: ActorSystem, materializer: ActorMaterializer) extends SLF4JLogging {
  outer =>

  private[core] lazy val oauthUtils = new HttpRequestUtils {
    override implicit val system: ActorSystem = outer.system
    override implicit val actorMaterializer: ActorMaterializer = outer.materializer
  }

  import oauthUtils._
  import MarathonAPIUtils._

  implicit val loggerMarathonUtils: Logger = outer.log

  private[core] val UnauthorizedKey = "<title>Unauthorized</title>"

  private[core] val errorServerErrors= Seq("<title>500 Internal Server Error</title>",
    "<title>502 Bad Gateway</title>")

  private[core] val instanceName = AppConstant.instanceName.fold("sparta-server") { x => x }

  private[core] val marathonApiUri = Properties.envOrNone("MARATHON_TIKI_TAKKA_MARATHON_URI").notBlank

  def removeEmptyFoldersFromDCOS : Unit = {
    outer.log.debug("Retrieving groups from MarathonAPI to purge the empty ones")
    val groupsToDelete = for {
      groupsToDeleteFuture <- retrieveEmptyGroups
      groupsAfterCheck <- Future(groupsToDeleteFuture)
    } yield groupsAfterCheck
    groupsToDelete.onSuccess{ case groups =>
      if(groups.isEmpty)
        log.debug("No eligible groups for deletion were found")
      else
        groups.foreach(sendDeleteForGroup)
    }
  }

  def checkDiscrepancy(activeWorkflowsInZK: Map[String,String]): Future[(Map[String,String], Seq[String])] = {
    for {
      runningWorkflows <- retrieveApps()
      listRunningWorkflows <- Future(extractWorkflowStatus(runningWorkflows))
      res <- Future {
        listRunningWorkflows match {
          case Some(workflows) =>
            (activeWorkflowsInZK.filterNot(mapItem =>
              workflows.contains(mapItem._1)),
              workflows.filterNot(workflowName =>
                activeWorkflowsInZK.contains(workflowName)))
          case None =>
            (activeWorkflowsInZK, Seq.empty[String])
        }
      }
    } yield res
  }


  private[core] def responseUnauthorized(): Future[Nothing] = {
    expireToken()
    val problem = Unauthorized
    log.error(problem.getMessage)
    Future.failed(problem)
  }

  private[core] def responseUnExpectedError(exception: Exception): Future[Nothing] = {
    val problem = UnExpectedError(exception)
    log.error(problem.getMessage)
    Future.failed(problem)
  }


  private[core] def responseCheckedAuthorization(response: String, successfulLog: Option[String]): Future[String] = {
    if (response.contains(UnauthorizedKey))
      responseUnauthorized()
    else if(errorServerErrors.exists(response.contains(_))) responseUnExpectedError(ServerError(response))
    else {
      successfulLog.foreach(log.debug(_))
      Future(response)
    }
  }

  def retrieveApps(): Future[String] = {
    val appsList = s"v2/apps?id=sparta/$instanceName/workflows"
    for {
      responseMarathon <- doRequest[String](marathonApiUri.get,
        appsList,
        HttpMethods.GET,
        cookies = Seq(getToken))
      responseAuth <- responseCheckedAuthorization(responseMarathon,
        Option(s"Correctly retrieved all apps inside $appsList"))
    } yield responseAuth
  }.recoverWith {
    case exception: Exception =>
      responseUnExpectedError(exception)
  }

  private[core] def retrieveEmptyGroups: Future[Seq[String]] = {
    val groupsPath = s"v2/groups/sparta/$instanceName/workflows"
    for {
      groups <- doRequest[String](marathonApiUri.get,
        groupsPath,
        HttpMethods.GET,
        cookies = Seq(getToken))
      responseAuth <- responseCheckedAuthorization(groups,
        Option(s"Correctly retrieved all sub-groups of $groupsPath"))
    } yield parseFindingEmpty(responseAuth)
  }.recoverWith {
    case exception: Exception =>
      responseUnExpectedError(exception)
  }

  private[core] def sendDeleteForGroup(group: String): Unit = {
    val groupPath = s"v2/groups/$group"
    for {
      resultHTTP <- doRequest[String](marathonApiUri.get,
        groupPath,
        HttpMethods.DELETE,
        cookies = Seq(getToken))
      resultAuth <- responseCheckedAuthorization(resultHTTP,
        Option(s"Correctly deleted group with id $group"))
    } yield resultAuth
  }.recoverWith {
    case exception: Exception =>
      responseUnExpectedError(exception)
  }

  private[utils] def retrieveIPandPorts: Future[Seq[AppParameters]] = {
    val GroupPath = s"v2/groups/sparta/$instanceName/workflows"

    import oauthUtils._

    for {
      group <- doRequest[String](marathonApiUri.get, GroupPath, cookies = Seq(getToken))
      seqApps = {
        if (!group.contains(UnauthorizedKey)) {
          extractAppsId(group) match {
            case Some(appsId) =>
              loggerMarathonUtils.debug(s"Applications IDs list retrieved from Marathon: ${appsId.mkString(",")}")
              Future.sequence {
                appsId.map { appId =>
                  val appResponse = doRequest[String](marathonApiUri.get, s"v2/apps/$appId", cookies = Seq(getToken))
                  appResponse.flatMap { response =>
                    if (response.contains(UnauthorizedKey))
                      responseUnauthorized()
                    else {
                      loggerMarathonUtils.debug(s"Extracted info for appID $appId: $response")
                      Future(response)
                    }
                  }
                }
              }
            case None => Future(Seq.empty[String])
          }
        } else responseUnauthorized()
      }
      appsStrings <- seqApps
    } yield {
      loggerMarathonUtils.debug(s"Marathon API responses from AppsIds: $appsStrings")
      appsStrings.flatMap(extractAppParameters)
    }
  } recoverWith {
    case exception: Exception =>
      responseUnExpectedError(exception)
  }


  private[core] def extractAppIDs(stringJson: String): Seq[String] = {
    log.debug(s"Marathon API responses from groups: $stringJson")
    if (stringJson.trim.nonEmpty)
      Try(new ObjectMapper().readTree(stringJson)) match {
        case Success(json) => extractID(json)
        case Failure(_) => Seq.empty
      }
    else Seq.empty
  }

  private[utils] def extractID(jsonNode: JsonNode): List[String] = {
    //Find apps and related ids in this node and all its subtrees
    val apps = jsonNode.findValues("apps")
    if (apps.isEmpty) List.empty
    else {
      apps.asScala.toList.flatMap(app =>
        if (app.elements().asScala.toList.nonEmpty)
          Try(app.findValues("id").asScala.toList) match {
            case Success(list) if list.nonEmpty =>
              list.flatMap(node => Try(node.asText).toOption.notBlank)
            case Failure(e) =>
              log.warn(s"Impossible to extract App in JsonNode: ${jsonNode.toString} .Error: ${e.getLocalizedMessage}")
              None
          }
        else None
      )
    }
  }

  private[core] def extractAppsId(json: String): Option[Seq[String]] =
    extractAppIDs(json) match {
      case seq if seq.nonEmpty => Some(seq)
      case _ => None
    }


  private[utils] def extractAppParameters(json: String): Option[AppParameters] = {
    val queryId = "$.app.id"
    val queryIpAddress = "$.app.tasks.[0].ipAddresses.[0].ipAddress"
    val queryPort = "$.app.tasks.[0].ports.[0]"

    Try {
      val extractor = new JsonPathExtractor(json, false)
      import extractor.query
      val id = query(queryId).asInstanceOf[String]
      val ip = query(queryIpAddress).asInstanceOf[String]
      val port = query(queryPort).asInstanceOf[Int]
      AppParameters(id, ip, port)
    } match {
      case Success(apps) =>
        Option(apps)
      case Failure(e) =>
        log.warn(s"Invalid App extraction, the Marathon API responses: $json .Error: ${e.getLocalizedMessage}")
        None
    }
  }

  private[core] def extractWorkflowStatus(json: String): Option[Seq[String]] = {
    val queryPath = "$.apps[*].id"

    if (json.trim.nonEmpty) {
      Try {
        val extractor = new JsonPathExtractor(json, false)
        import extractor.query
        val res = query(queryPath).asInstanceOf[JSONArray]
        val test = res.iterator().asScala.map(_.toString).toSeq
        test
      } match {
        case Success(apps) =>
          Option(apps)
        case Failure(e) =>
          log.warn(s"Invalid Apps extraction, the Marathon API responses: $json ." +
            s"Error: ${e.getLocalizedMessage}")
          None
      }
    }
    else None
  }
}

object MarathonAPIUtils {
  //Here there are all the methods for handling JSON responses wrt MarathonAPI

  case class CustomNode(nameNode: String, childrenNodes:Seq[String])

  case class AppParameters(appId: String, addressIP: String, port: Int)

  private[core] class JsonPathExtractor(jsonDoc: String, isLeafToNull: Boolean) {

    val conf = {
      if (isLeafToNull)
        Configuration.defaultConfiguration().addOptions(com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL)
      else Configuration.defaultConfiguration()
    }
    private val ctx: ReadContext = JsonPath.using(conf).parse(jsonDoc)

    def query(query: String): Any = ctx.read(query)
  }

  private def extractEmpty(node: JsonNode): Seq[String] = {
    //Our rootGroup is ../../workflows [{ id : ../../workflows, groups : [{id : ../../workflows/home}]
    val rootGroup = node.withArray("groups").elements().asScala.toList
    val listOfGroupsToCheck = scala.collection.mutable.ListBuffer[JsonNode]()
    val listOfEmptyGroups = scala.collection.mutable.ListBuffer[String]()
    val listOfGroups = scala.collection.mutable.ListBuffer[CustomNode]()
    listOfGroupsToCheck.++=(rootGroup)

    // Breadth-first traversal: iteration more efficient than recursion when there are deeply nested JSONs
    while(listOfGroupsToCheck.nonEmpty){
      val currentJnode = listOfGroupsToCheck.head
      val nestedGroups = currentJnode.findValue("groups").elements().asScala.toSeq
      val childrenNames = nestedGroups.flatMap(node => Try(node.get("id").asText).toOption)
      val currentApps = currentJnode.findValue("apps").asScala
      val currentID = Try(currentJnode.get("id").asText).toOption.notBlank
      currentID.fold(){id => if (nestedGroups.isEmpty && currentApps.isEmpty) listOfEmptyGroups += id}

      listOfGroupsToCheck -= currentJnode
      listOfGroupsToCheck ++= nestedGroups

      // We prepend in order to have a LIFO: nodes closer to the root will be checked later
      listOfGroups.+=:(CustomNode(currentID.get, childrenNames))
    }

    // Mark for deletion only the inner nodes that have only empty children
    listOfGroups.toList.foreach{ group =>
      if (group.childrenNodes.nonEmpty && group.childrenNodes.forall(child => listOfEmptyGroups.contains(child)))
        listOfEmptyGroups += group.nameNode
    }

    listOfEmptyGroups
  }

  private[core] def parseFindingEmpty(jsonString: String) : Seq[String] = {
    if (jsonString.trim.nonEmpty)
      Try(new ObjectMapper().readTree(jsonString)) match {
        case Success(json) => extractEmpty(json)
        case Failure(_) => Seq.empty
      }
    else Seq.empty
  }

}

abstract class MarathonApiError protected(description: String) extends RuntimeException {
  override def getMessage: String = s"MarathonAPI problem: $description"
}

object MarathonApiError{

  object Unauthorized extends MarathonApiError("Unauthorized in Marathon API")

  case class ServerError(message: String) extends MarathonApiError(s"MarathonAPI responded with $message")

  case class UnExpectedError(ex: Exception) extends MarathonApiError(s"Unexpected error with message: ${ex.toString}")

}