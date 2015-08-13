/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.api.actor

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.StreamingActor._
import com.stratio.sparkta.serving.api.actor.SupervisorContextActor._
import com.stratio.sparkta.serving.core.models.StreamingContextStatusEnum._
import com.stratio.sparkta.serving.core.models._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class StreamingActor(streamingContextService: StreamingContextService,
                     jobServerRef: Option[ActorRef],
                     jobServerConfig: Option[Config],
                     supervisorContextRef: ActorRef) extends InstrumentedActor {

  implicit val timeout: Timeout = Timeout(10.seconds)

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: DriverException => Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  override def receive: PartialFunction[Any, Unit] = {
    case CreateContext(policy) => doCreateContext(policy)
    case GetContextStatus(contextName) => doGetContextStatus(contextName)
    case DeleteContext(contextName) => doDeleteContext(contextName)
    case GetAllContextStatus => doGetAllContextStatus
    case ResponseCreateContext(response) => doResponseCreateContext(response)
  }

  /**
   * Tries to create a spark streaming context with a given configuration.
   * @param policy that contains the configuration to run.
   */
  private def doCreateContext(policy: AggregationPoliciesModel): Unit = {

    val streamingContextActor = getStreamingContextActor(policy)

    supervisorContextRef ! SupAddContextStatus(policy.name,
      new StatusContextActor(streamingContextActor, policy.name, Initializing, None))
    streamingContextActor ! InitSparktaContext
  }

  private def getStreamingContextActor(policy: AggregationPoliciesModel): ActorRef = {
    if (jobServerRef.isDefined && jobServerConfig.isDefined) {
      context.actorOf(
        Props(new ClusterContextActor(policy, streamingContextService, jobServerRef.get, jobServerConfig.get)),
        "context-actor-".concat(policy.name))
    } else {
      context.actorOf(
        Props(new StandAloneContextActor(policy, streamingContextService)),
        "context-actor-".concat(policy.name))
    }
  }

  private def doResponseCreateContext(response: StatusContextActor): Unit = {
    supervisorContextRef ! SupAddContextStatus(response.policyName, response)
    response.status match {
      case Initialized =>
        log.info(s"StreamingContext initialized with name: ${response.policyName} \n " +
          s"description: ${response.description.getOrElse("")}")
      case ConfigurationError => {
        log.warn(s"Configuration error! StreamingContext with name: ${response.policyName} \n " +
          s"description: ${response.description.getOrElse("")}")
        response.actor ! PoisonPill
      }
      case Error => {
        log.error(s"Error initializing StreamingContext with name: ${response.policyName} \n " +
          s"description: ${response.description.getOrElse("")}")
        response.actor ! PoisonPill
      }
    }
  }

  /**
   * If a context with a specific contextName exists, it will retrieve information about it.
   * @param contextName of the context to obtain information.
   */
  private def doGetContextStatus(contextName: String): Unit = {
    (supervisorContextRef ? SupGetContextStatus(contextName)).mapTo[SupResponse_ContextStatus].foreach {
      case SupResponse_ContextStatus(Some(contextActorStatus)) =>
        sender ! new StreamingContextStatus(contextName, contextActorStatus.status, contextActorStatus.description)
      case SupResponse_ContextStatus(None) =>
        throw new DriverException("Context with name " + contextName + " does not exists.")
    }
  }

  /**
   * If a context with a specific contextName exists, it will try to delete it.
   * @param contextName of the context to delete.
   */
  private def doDeleteContext(contextName: String): Unit = {
    (supervisorContextRef ? SupDeleteContextStatus(contextName)).mapTo[SupResponse_ContextStatus]
      .foreach(resContextSt =>
      resContextSt.contextStatus match {
        case Some(contextActorStatus) => {
          if (jobServerRef.isDefined) deleteClusterContext(contextName, contextActorStatus.actor)
          else deleteStandAloneContext(contextName, contextActorStatus.actor)
        }
        case None =>
          throw new DriverException("Context with name " + contextName + " does not exists.")
      }
      )
  }

  private def deleteStandAloneContext(contextName: String, contextActorStatus: ActorRef): Unit = {
    contextActorStatus ! PoisonPill
    sender ! new StreamingContextStatus(contextName, Removed, None)
  }

  private def deleteClusterContext(contextName: String, contextActorStatus: ActorRef): Unit = {
//    (jobServerRef.get ? JsDeleteContext(contextName)).mapTo[JsResponseDeleteContext].foreach {
//      case JsResponseDeleteContext(Failure(exception)) =>
//        sender ! new StreamingContextStatus(contextName, Error, Some(exception.getMessage))
//      case JsResponseDeleteContext(Success(response)) => {
//        contextActorStatus ! PoisonPill
//        sender ! new StreamingContextStatus(contextName, Removed, None)
//      }
//    }
  }

  /**
   * Retrieves information of all running contexts.
   */
  private def doGetAllContextStatus: Unit = {
    (supervisorContextRef ? SupGetAllContextStatus).mapTo[SupResponse_AllContextStatus].foreach {
      case SupResponse_AllContextStatus(contextStatuses) =>
        sender ! contextStatuses.map(cas => new StreamingContextStatus(cas._1, cas._2.status, cas._2.description)).toSeq
    }
  }

  /**
   * Stop all context actors.
   */
  override def postStop(): Unit = {
    (supervisorContextRef ? SupGetAllContextStatus).mapTo[SupResponse_AllContextStatus].foreach {
      case SupResponse_AllContextStatus(contextStatuses) => contextStatuses.values.foreach(_.actor ! PoisonPill)
        super.postStop()
    }
  }
}

object StreamingActor {

  case class CreateContext(policy: AggregationPoliciesModel)

  case class GetContextStatus(contextName: String)

  case object GetAllContextStatus

  case class StopContext(contextName: String)

  case class DeleteContext(contextName: String)

  case object InitSparktaContext

  case class InitSparktaContextError(e: Exception)

  case object StopSparktaContext

  case class ResponseCreateContext(contextStatus: StatusContextActor)

}