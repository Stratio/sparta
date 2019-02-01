/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparta.security.{Action, SpartaSecurityManager}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.authorization.LoggedUser

trait ActionUserAuthorize extends Actor with SLF4JLogging {

  val apiTimeout = Try(SpartaConfig.getDetailConfig().get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1

  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  //scalastyle:off
  type ResourcesAndActions = Map[String, Action]

  /* PUBLIC METHODS */

  override def receive: Receive = {
    case action: Any => manageErrorInActions(sender, receiveApiActions(action))
  }

  def receiveApiActions(action: Any): Any

  /**
    * Authorize ONLY Resource and Action (e.g. Workflows -> View)
    */
  def authorizeActions[T](
                           user: Option[LoggedUser],
                           resourcesAndActions: ResourcesAndActions,
                           sendTo: Option[ActorRef] = None
                         )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    authorizeActionsByResourcesIds(user, resourcesAndActions, Seq.empty, sendTo)(actionFunction)

  /**
    * Authorize Resource and Action by ONE resourceId (e.g. Workflows -> View IN /home/test)
    */
  def authorizeActionsByResourceId[T](
                                       user: Option[LoggedUser],
                                       resourcesAndActions: ResourcesAndActions,
                                       resourceId: String,
                                       sendTo: Option[ActorRef] = None
                                     )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    authorizeActionsByResourcesIds(user, resourcesAndActions, Seq(resourceId), sendTo)(actionFunction)

  /**
    * Authorize Resource and Action by Seq of resourcesId (e.g. Workflows -> View IN (/home/test AND /home/test2 ....) )
    */
  def authorizeActionsByResourcesIds[T](
                                         user: Option[LoggedUser],
                                         resourcesAndActions: ResourcesAndActions,
                                         resourcesId: Seq[String],
                                         sendTo: Option[ActorRef] = None
                                       )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit = {
    val senderActor = sendTo.getOrElse(sender)

    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        val rejectedActions = authorizeResourcesAndActions(userLogged, resourcesAndActions, resourcesId, secManager)
        if (rejectedActions.nonEmpty) {
          // There are rejected actions.
          log.debug(s"Not authorized to execute generic actions: $resourcesAndActions\tRejected: $rejectedActions\tResourcesId: ${resourcesId.mkString(",")}")
          senderActor ! Right(errorResponseAuthorization(userLogged.id, resourcesAndActions.head._1))
        } else {
          // All actions've been accepted.
          log.debug(s"Authorized to execute generic actions: $resourcesAndActions\tResourcesId: ${resourcesId.mkString(",")}")
          executeActionAndSendResponse(senderActor, actionFunction)
        }
      case (Some(_), None) =>
        senderActor ! Right(errorNoUserFound(resourcesAndActions.values.toSeq))
      case (None, _) =>
        executeActionAndSendResponse(senderActor, actionFunction)
    }
  }

  /**
    * Filter Service Results One by One by authorizationId (e.g. allWorkflows -> foreach(hasPermissionIn gosec) -> result)
    */
  def authorizeActionResultResources[T](
                                         user: Option[LoggedUser],
                                         resourcesAndActions: ResourcesAndActions,
                                         sendTo: Option[ActorRef] = None
                                       )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit = {
    val senderActor = sendTo.getOrElse(sender)

    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        Try(actionFunction) match {
          case Success(actionResult) =>
            actionResult match {
              case action: Future[_] =>
                action.onComplete { completedAction: Try[Any] =>
                  val matchedResponse = extractValueFromTry(completedAction)
                  matchedResponse match {
                    case Success(entityResult: Any) =>
                      senderActor ! authorizeGenericEntityByActions(entityResult, resourcesAndActions, userLogged, secManager)
                    case Failure(e) =>
                      senderActor ! Left(Failure(e))
                  }
                }
              case actionNonFuture: T =>
                senderActor ! authorizeGenericEntityByActions(actionNonFuture, resourcesAndActions, userLogged, secManager)
            }
          case Failure(e) =>
            senderActor ! Left(Failure(e))
        }
      case (Some(_), None) => senderActor ! Right(errorNoUserFound(resourcesAndActions.values.toSeq))
      case (None, _) => executeActionAndSendResponse(senderActor, actionFunction)
    }
  }

  @tailrec
  private def extractValueFromTry(completedAction: Try[Any]): Any = {
    completedAction match {
      case Success(actionMatch) => actionMatch match {
        case actionMatch2: Try[Any] => extractValueFromTry(actionMatch2)
        case _ => completedAction
      }
      case Failure(_) => completedAction
    }
  }

  /* PRIVATE METHODS */

  private def manageErrorInActions(sendTo: ActorRef, actorMessageFunction: => Any): Any = {
    Try(actorMessageFunction) match {
      case Success(result) => result match {
        case _: Unit =>
        case futureResponse: Future[Any] =>
          futureResponse.onFailure { case e =>
            sendTo ! Left(Failure(e))
          }
      }
      case Failure(e) => sendTo ! Left(Failure(e))
    }
  }

  private def authorizeResourcesAndActions(
                                            userLogged: LoggedUser,
                                            resourcesAndActions: ResourcesAndActions,
                                            resourcesId: Seq[String],
                                            secManager: SpartaSecurityManager
                                          ): ResourcesAndActions =
    resourcesAndActions filterNot { case (resource, action) =>
      if (resourcesId.nonEmpty)
        resourcesId.forall(resourceId => secManager.authorize(userLogged.id, (resource, resourceId), action, hierarchy = false))
      else secManager.authorize(userLogged.id, resource, action, hierarchy = false)
    }

  private def executeActionAndSendResponse[T](sendToActor: ActorRef, actionFunction: => T): Unit = {
    Try(actionFunction) match {
      case Success(actionResult) =>
        actionResult match {
          case action: Future[_] =>
            action.onComplete { completedAction: Try[Any] =>
              val finalResponse = extractValueFromTry(completedAction)
              sendToActor ! Left(finalResponse)
            }
          case actionNonFuture: Try[_] =>
            sendToActor ! Left(actionNonFuture)
          case actionNonFuture: T =>
            sendToActor ! Left(Try(actionNonFuture))
        }
      case Failure(e) =>
        sendToActor ! Left(Failure(e))
    }
  }

  private def authorizeGenericEntityByActions[T](
                                                  entity: T,
                                                  resourcesAndActions: ResourcesAndActions,
                                                  userLogged: LoggedUser,
                                                  secManager: SpartaSecurityManager
                                                ): Either[Try[_], UnauthorizedResponse] = {
    entity match {
      case entityResult: EntityAuthorization =>
        authorizeEntityByActions(entityResult, resourcesAndActions, userLogged, secManager)
      case entitiesResult: Seq[EntityAuthorization] =>
        filterEntitiesByActions(entitiesResult, resourcesAndActions, userLogged, secManager)
      case Success(entityResult: EntityAuthorization) =>
        authorizeEntityByActions(entityResult, resourcesAndActions, userLogged, secManager)
      case Success(entitiesResult: Seq[EntityAuthorization]) =>
        filterEntitiesByActions(entitiesResult, resourcesAndActions, userLogged, secManager)
      case Failure(e) =>
        Left(Failure(e))
      case _ =>
        Left(Try(entity))
    }
  }

  private def getRejectedResourceAndActions(
                                             resourcesAndActions: ResourcesAndActions,
                                             idToAuthorize: String,
                                             userLogged: LoggedUser,
                                             secManager: SpartaSecurityManager
                                           ): ResourcesAndActions =
    resourcesAndActions.filterNot { case (resource, action) =>
      secManager.authorize(userLogged.id, (resource, idToAuthorize), action, hierarchy = false)
    }

  private def filterEntitiesByActions(
                                       entityResult: Seq[EntityAuthorization],
                                       resourcesAndActions: ResourcesAndActions,
                                       userLogged: LoggedUser,
                                       secManager: SpartaSecurityManager
                                     ): Either[Try[Seq[EntityAuthorization]], UnauthorizedResponse] =
    Try {
      entityResult.filter { entity =>
        val idToAuthorize = entity.authorizationId
        val rejectedActions = getRejectedResourceAndActions(resourcesAndActions, idToAuthorize, userLogged, secManager)

        if (rejectedActions.nonEmpty)
          log.debug(s"Filtered resource with id ($idToAuthorize) by the authorization service with rejected actions: $rejectedActions")
        rejectedActions.isEmpty
      }
    } match {
      case Success(entitiesFiltered) =>
        Left(Success(entitiesFiltered))
      case Failure(e) =>
        Left(Failure(e))
    }

  private def authorizeEntityByActions(
                                        entityResult: EntityAuthorization,
                                        resourcesAndActions: ResourcesAndActions,
                                        userLogged: LoggedUser,
                                        secManager: SpartaSecurityManager
                                      ): Either[Try[EntityAuthorization], UnauthorizedResponse] = {
    val idToAuthorize = entityResult.authorizationId
    val rejectedActions = getRejectedResourceAndActions(resourcesAndActions, idToAuthorize, userLogged, secManager)

    if (rejectedActions.isEmpty) {
      log.debug(s"Authorized to execute actions: $resourcesAndActions \t ResourceId: $idToAuthorize")
      Left(Try(entityResult))
    } else {
      log.debug(s"Not authorized to execute generic actions: $resourcesAndActions\tRejected: $rejectedActions\tResourceId: $idToAuthorize")
      Right(errorResponseAuthorization(userLogged.id, rejectedActions.head._1))
    }
  }
}