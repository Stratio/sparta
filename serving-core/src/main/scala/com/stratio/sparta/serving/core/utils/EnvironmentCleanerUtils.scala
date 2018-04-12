/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.stratio.sparta.serving.core.marathon.OauthTokenUtils.getToken
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap.option2NotBlankOption
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import com.stratio.sparta.serving.core.utils.EnvironmentCleanerUtils._


class EnvironmentCleanerUtils(system: ActorSystem, materializer: ActorMaterializer)
  extends MarathonAPIUtils (system, materializer){

  import oauthUtils._

  def removeFromDCOS : Unit = {
    log.debug("Retrieving groups from MarathonAPI to purge the empty ones")
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


  private def retrieveEmptyGroups: Future[Seq[String]] = {
    val groupsPath = s"v2/groups/sparta/$instanceName/workflows"
    for {
      groups <- doRequest[String](marathonApiUri.get,
        groupsPath,
        HttpMethods.GET,
        cookies = Seq(getToken))
      responseAuth <- responseCheckedAuthorization(groups,
        Option(s"Correctly retrieved all sub-groups of $groupsPath"))
    } yield parseFindingEmpty(responseAuth)
  }.recoverWith{
    case exception: Exception =>
      responseUnExpectedError(exception)
  }

  private def sendDeleteForGroup (group : String) : Unit = {
    val groupPath = s"v2/groups/$group"
    for {
      resultHTTP <- doRequest[String](marathonApiUri.get,
        groupPath,
        HttpMethods.DELETE,
        cookies = Seq(getToken))
      resultAuth <- responseCheckedAuthorization(resultHTTP,
        Option(s"Correctly deleted group with id $group"))
    } yield resultAuth
  }.recoverWith{
    case exception: Exception =>
      responseUnExpectedError(exception)
  }
}

object EnvironmentCleanerUtils{

  import scala.collection.JavaConverters._

  private[core] def parseFindingEmpty(jsonString: String) : Seq[String] = {
    if (jsonString.trim.nonEmpty)
      Try(new ObjectMapper().readTree(jsonString)) match {
        case Success(json) => extractEmpty(json)
        case Failure(_) => Seq.empty
      }
    else Seq.empty
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

  case class CustomNode(nameNode: String, childrenNodes:Seq[String])
}