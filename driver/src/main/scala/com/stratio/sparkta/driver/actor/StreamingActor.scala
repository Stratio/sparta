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

package com.stratio.sparkta.driver.actor

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.actor.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.dto.{AggregationPoliciesDto, PolicyElementDto, StreamingContextStatusDto}
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.service.StreamingContextService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class CreateContext(policy: AggregationPoliciesDto)

case class GetContextStatus(contextName: String)

case class GetAllContextStatus()

case class StopContext(contextName: String)

case class DeleteContext(contextName: String)

case class CreateFragment(fragment: PolicyElementDto)

case class ContextActorStatus(actor: ActorRef, status: StreamingContextStatusEnum.Status, description: Option[String])

class StreamingActor(streamingContextService: StreamingContextService) extends InstrumentedActor {

  private var contextActors: Map[String, ContextActorStatus] = Map()

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
    case GetAllContextStatus => doGetAllContextStatus()
  }

  /**
   * Tries to create a spark streaming context with a given configuration.
   * @param policy that contains the configuration to run.
   */
  private def doCreateContext(policy: AggregationPoliciesDto): Unit = {
    val streamingContextActor = context.actorOf(
      Props(new StreamingContextActor(policy, streamingContextService)), "context-actor-".concat(policy.name))
    contextActors += (policy.name -> new ContextActorStatus(streamingContextActor, Initializing, None))

    (streamingContextActor ? Init)(Timeout(10 minutes)).onComplete {
      case Success(Initialized) =>
        log.info("Context initialized with name: " + policy.name)
        contextActors.get(policy.name) match {
          case Some(contextActorStatus) =>
            contextActors += (policy.name -> new ContextActorStatus(contextActorStatus.actor, Initialized, None))
        }
      case Success(InitError(e: DriverException)) =>
        log.warn("Configuration error! in context with name: " + policy.name)
        contextActors.get(policy.name) match {
          case Some(contextActorStatus) =>
            contextActors += (policy.name ->
              new ContextActorStatus(contextActorStatus.actor,ConfigurationError, Option(e.getMessage)))
            contextActorStatus.actor ! PoisonPill
        }
      case Success(InitError(e)) =>
        log.error("Error initializing StreamingContext with name: " + policy.name, e)
        contextActors.get(policy.name) match {
          case Some(contextActorStatus) =>
            contextActors += (policy.name ->
              new ContextActorStatus(contextActorStatus.actor, Error, Option(e.getMessage)))
            contextActorStatus.actor ! PoisonPill
        }
      case Failure(e: Exception) =>
        log.error("Akka error initializing StreamingContext with name: " + policy.name, e)
        contextActors.get(policy.name) match {
          case Some(contextActorStatus) =>
            contextActors += (policy.name ->
              new ContextActorStatus(contextActorStatus.actor, Error, Option(e.getMessage)))
            contextActorStatus.actor ! PoisonPill
        }
      case x =>
        log.warn("Unexpected message received by streamingContextActor: " + x)
    }
  }

  /**
   * If a context with a specific contextName exists, it will retrieve information about it.
   * @param contextName of the context to obtain information.
   */
  private def doGetContextStatus(contextName: String): Unit = {
    contextActors.get(contextName) match {
      case Some(contextActorStatus) =>
        sender ! new StreamingContextStatusDto(contextName, contextActorStatus.status, contextActorStatus.description)
      case None =>
        throw new DriverException("Context with name " + contextName + " does not exists.")
    }
  }

  /**
   * If a context with a specific contextName exists, it will try to delete it.
   * @param contextName of the context to delete.
   */
  private def doDeleteContext(contextName: String): Unit = {
    contextActors.get(contextName) match {
      case Some(contextActorStatus) =>
        contextActorStatus.actor ! PoisonPill
        contextActors -= contextName
        sender ! new StreamingContextStatusDto(contextName, Removed, None)
      case None =>
        throw new DriverException("Context with name " + contextName + " does not exists.")
    }
  }

  /**
   * Retrieves information of all running contexts.
   */
  private def doGetAllContextStatus(): Unit = {
    sender ! contextActors.map(cas =>
      new StreamingContextStatusDto(cas._1, cas._2.status, cas._2.description)).toSeq
  }

  override def postStop(): Unit = {
    contextActors.values.foreach(_.actor ! PoisonPill)
    super.postStop()
  }
}