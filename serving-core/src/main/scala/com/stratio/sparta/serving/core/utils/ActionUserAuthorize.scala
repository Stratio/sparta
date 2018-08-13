/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.stratio.sparta.security.{Action, SpartaSecurityManager}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.dto.LoggedUser

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

trait ActionUserAuthorize extends Actor with SLF4JLogging {

  private val apiTimeout = Try(SpartaConfig.getDetailConfig.get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1

  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  import context.dispatcher

  //scalastyle:off
  type ResourcesAndActions = Map[String, Action]

  /* PUBLIC METHODS */

  override def receive : Receive = {
    case action: Any => manageErrorInActions(receiveApiActions(action))
  }

  def receiveApiActions(action : Any): Unit

  private def manageErrorInActions(actorMessageFunction: => Unit): Unit = {
    Try(actorMessageFunction) match {
      case Success(_) =>
      case Failure(e) => sender ! Left(Try(throw e))
    }
  }

  /**
    * Authorize ONLY Resource and Action (e.g. Workflows -> View)
    */
  def authorizeActions[T](
                                       user: Option[LoggedUser],
                                       resourcesAndActions: ResourcesAndActions,
                                       pipeToActor: Option[ActorRef] = None
                                     )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    authorizeActionsByResourcesIds(user, resourcesAndActions, Seq.empty, pipeToActor)(actionFunction)

  /**
    * Authorize Resource and Action  by ONE resourceId (e.g. Workflows -> View IN /home/test)
    */
  def authorizeActionsByResourceId[T](
                                       user: Option[LoggedUser],
                                       resourcesAndActions: ResourcesAndActions,
                                       resourceId: String,
                                       pipeToActor: Option[ActorRef] = None
                                     )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    authorizeActionsByResourcesIds(user, resourcesAndActions, Seq(resourceId), pipeToActor)(actionFunction)

  /**
    * Authorize Resource and Action by Seq of resourcesId (e.g. Workflows -> View IN (/home/test AND /home/test2 ....) )
    */
  def authorizeActionsByResourcesIds[T](
                                         user: Option[LoggedUser],
                                         resourcesAndActions: ResourcesAndActions,
                                         resourcesId: Seq[String],
                                         pipeToActor: Option[ActorRef] = None
                                       )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit = {
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        val rejectedActions = authorizeResourcesAndActions(userLogged, resourcesAndActions, resourcesId, secManager)
        if (rejectedActions.nonEmpty) {
          // There are rejected actions.
          log.debug(s"Not authorized to execute generic actions: $resourcesAndActions\tRejected: $rejectedActions\tResourcesId: ${resourcesId.mkString(",")}")
          sender ! Right(errorResponseAuthorization(userLogged.id, resourcesAndActions.head._1))
        } else {
          // All actions've been accepted.
          log.debug(s"Authorized to execute generic actions: $resourcesAndActions\tResourcesId: ${resourcesId.mkString(",")}")
          commonPipeToActor(pipeToActor, actionFunction)
        }
      case (Some(_), None) =>
        sender ! Right(errorNoUserFound(resourcesAndActions.values.toSeq))
      case (None, _) =>
        commonPipeToActor(pipeToActor, actionFunction)
    }
  }

  /**
    * Filter PipeActor Results One by One by authorizationId (e.g. allWorkflows -> foreach(hasPermissionIn gosec) -> result)
    */
  def filterResultsWithAuthorization[T](
                                         user: Option[LoggedUser],
                                         resourcesAndActions: ResourcesAndActions,
                                         pipeToActor: Option[ActorRef] = None
                                       )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (pipeToActor.isDefined) {
          val result = for {
            response <- pipeToActor.get ? actionFunction
          } yield {
            response match {
              case Success(entityResult: Seq[EntityAuthorization]) =>
                val idFiltered = entityResult.filter { entity =>
                  val idToAuthorize = entity.authorizationId
                  val rejectedActions = getRejectedResourceAndActions(resourcesAndActions, idToAuthorize, userLogged, secManager)

                  if (rejectedActions.nonEmpty)
                    log.debug(s"Filtered resource with id ($idToAuthorize) by the authorization service with rejected actions: $rejectedActions")
                  rejectedActions.isEmpty
                }
                Left(Try(idFiltered))
              case _ => Left(response)
            }
          }
          pipe(result) to sender
        } else sender ! Left(actionFunction)
      case (Some(_), None) => sender ! Right(errorNoUserFound(resourcesAndActions.values.toSeq))
      case (None, _) => commonPipeToActor(pipeToActor, actionFunction)
    }

  /**
    * Filter Service Results One by One by authorizationId (e.g. allWorkflows -> foreach(hasPermissionIn gosec) -> result)
    */
  def filterServiceResultsWithAuthorization[T](
                                                user: Option[LoggedUser],
                                                resourcesAndActions: ResourcesAndActions
                                              )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        val response = actionFunction
        val result = actionFunction match {
          case Success(entityResult: Seq[EntityAuthorization]) =>
            val idFiltered = entityResult.filter { entity =>
              val idToAuthorize = entity.authorizationId
              val rejectedActions = getRejectedResourceAndActions(resourcesAndActions, idToAuthorize, userLogged, secManager)

              if (rejectedActions.nonEmpty)
                log.debug(s"Filtered resource with id ($idToAuthorize) by the authorization service with rejected actions: $rejectedActions")
              rejectedActions.isEmpty
            }
            Left(Try(idFiltered))
          case _ => Left(response)
        }
        sender ! result
      case (Some(_), None) => sender ! Right(errorNoUserFound(resourcesAndActions.values.toSeq))
      case (None, _) => commonPipeToActor(None, actionFunction)
    }


  /**
     * First = Authorize Resource and Action (e.g. Workflows -> View )
    *  Second = Filter PipeActor Results One by One by authorizationId (e.g. allWorkflows -> foreach(hasPermissionInGosec) -> result)
    */
  def authorizeActionsAndFilterResults[T](
                                           user: Option[LoggedUser],
                                           actionsToAuthorize: ResourcesAndActions,
                                           filterActions: ResourcesAndActions,
                                           pipeToActor: Option[ActorRef] = None
                                         )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit = {
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        val rejectedActions = authorizeResourcesAndActions(userLogged, actionsToAuthorize, Seq.empty, secManager)
        if (rejectedActions.nonEmpty) {
          // There are rejected actions.
          log.debug(s"Not authorized to execute generic actions: $actionsToAuthorize\tRejected: $rejectedActions")
          sender ! Right(errorResponseAuthorization(userLogged.id, actionsToAuthorize.head._1))
        } else filterResultsWithAuthorization(user, filterActions, pipeToActor)(actionFunction)
      case (Some(_), None) =>
        sender ! Right(errorNoUserFound(actionsToAuthorize.values.toSeq ++ filterActions.values.toSeq))
      case (None, _) =>
        commonPipeToActor(pipeToActor, actionFunction)
    }
  }

  /**
    * Authorize PipeActor singleResult by resourceId (e.g. WorkFlow-A -> hasPermissionInGosec -> Workflow-A)
    */
  def authorizeResultByResourceId[T](
                                      user: Option[LoggedUser],
                                      resourcesAndActions: ResourcesAndActions,
                                      pipeToActor: Option[ActorRef] = None
                                    )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (pipeToActor.isDefined) {
          val result = for {
            response <- pipeToActor.get ? actionFunction
          } yield {
            response match {
              case Success(entityResult: EntityAuthorization) =>
                val idToAuthorize = entityResult.authorizationId
                val rejectedActions = getRejectedResourceAndActions(resourcesAndActions, idToAuthorize, userLogged, secManager)

                if (rejectedActions.isEmpty) {
                  log.debug(s"Authorized to execute actions: $resourcesAndActions \t ResourceId: $idToAuthorize")
                  Left(Try(entityResult))
                } else {
                  log.debug(s"Not authorized to execute generic actions: $resourcesAndActions\tRejected: $rejectedActions\tResourceId: $idToAuthorize")
                  Right(errorResponseAuthorization(userLogged.id, rejectedActions.head._1))
                }
              case _ => Left(response)
            }
          }
          pipe(result) to sender
        } else sender ! Left(actionFunction)
      case (Some(_), None) => sender ! Right(errorNoUserFound(resourcesAndActions.values.toSeq))
      case (None, _) => commonPipeToActor(pipeToActor, actionFunction)
    }

  /**
    * Authorize Service singleResult by resourceId (e.g. WorkFlow-A -> hasPermissionInGosec -> Workflow-A)
    */
  def authorizeServiceResultByResourceId[T](
                                             user: Option[LoggedUser],
                                             resourcesAndActions: ResourcesAndActions,
                                             pipeToActor: Option[ActorRef] = None
                                           )(actionFunction: => T)(implicit secManagerOpt: Option[SpartaSecurityManager]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        val response = actionFunction
        val result = response match {
          case Success(entityResult: EntityAuthorization) =>
            val idToAuthorize = entityResult.authorizationId
            val rejectedActions = getRejectedResourceAndActions(resourcesAndActions, idToAuthorize, userLogged, secManager)

            if (rejectedActions.isEmpty) {
              log.debug(s"Authorized to execute actions: $resourcesAndActions \t ResourceId: $idToAuthorize")
              Left(Try(entityResult))
            } else {
              log.debug(s"Not authorized to execute generic actions: $resourcesAndActions\tRejected: $rejectedActions\tResourceId: $idToAuthorize")
              Right(errorResponseAuthorization(userLogged.id, rejectedActions.head._1))
            }
          case _ => Left(response)
        }
        sender ! result
      case (Some(_), None) => sender ! Right(errorNoUserFound(resourcesAndActions.values.toSeq))
      case (None, _) => commonPipeToActor(None, actionFunction)
    }

  /* PRIVATE METHODS */

  private def getRejectedResourceAndActions(
                                             resourcesAndActions: ResourcesAndActions,
                                             idToAuthorize: String,
                                             userLogged: LoggedUser,
                                             secManager: SpartaSecurityManager
                                           ): ResourcesAndActions =
    resourcesAndActions.filterNot { case (resource, action) =>
      secManager.authorize(userLogged.id, (resource, idToAuthorize), action, hierarchy = false)
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


  private def commonPipeToActor[T](pipeToActor: Option[ActorRef] = None, actionFunction: => T): Unit = {
    if (pipeToActor.isDefined) {
      val result = for {
        response <- pipeToActor.get ? actionFunction
      } yield Left(response)

      pipe(result) to sender
    } else sender ! Left(actionFunction)
  }

}