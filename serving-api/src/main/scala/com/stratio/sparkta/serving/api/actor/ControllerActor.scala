/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

import akka.actor.{ActorContext, _}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.api.service.http._
import com.stratio.sparkta.serving.core.constants.AkkaConstant
import com.stratio.sparkta.serving.core.exception.ServingCoreException
import com.stratio.sparkta.serving.core.models.{ErrorModel, SparktaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization.write
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext

class ControllerActor(actorsMap: Map[String, ActorRef], curatorFramework : CuratorFramework) extends HttpServiceActor
with SLF4JLogging
with SparktaSerializer {

  override implicit def actorRefFactory: ActorContext = context

  implicit def exceptionHandler(implicit logg: LoggingContext): ExceptionHandler =
    ExceptionHandler {
      case exception: ServingCoreException =>
        requestUri { uri =>
          log.error(exception.getLocalizedMessage)
          complete(StatusCodes.NotFound, write(ErrorModel.toErrorModel(exception.getLocalizedMessage)))
        }
      case exception: Throwable =>
        requestUri { uri =>
          log.error(exception.getLocalizedMessage, exception)
          complete(StatusCodes.InternalServerError, write(
            new ErrorModel(ErrorModel.CodeUnknow, exception.getLocalizedMessage)
          ))
        }
    }

  val serviceRoutes: ServiceRoutes = new ServiceRoutes(actorsMap, context, curatorFramework)

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(getRoutes))

  def getRoutes: Route = webRoutes ~ serviceRoutes.fragmentRoute ~
    serviceRoutes.policyContextRoute ~ serviceRoutes.policyRoute ~
    serviceRoutes.templateRoute ~ serviceRoutes.AppStatusRoute ~ serviceRoutes.policyContextRouteNew

  def webRoutes: Route =
    get {
      pathPrefix("") {
        pathEndOrSingleSlash {
          getFromResource("classes/web/index.html")
        }
      } ~ getFromResourceDirectory("classes/web") ~
        pathPrefix("") {
          pathEndOrSingleSlash {
            getFromResource("web/index.html")
          }
        } ~ getFromResourceDirectory("web")
    }
}

class ServiceRoutes(actorsMap: Map[String, ActorRef], context: ActorContext, curatorFramework : CuratorFramework) {

  val fragmentRoute: Route = new FragmentHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.FragmentActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val templateRoute: Route = new TemplateHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.TemplateActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val policyRoute: Route = new PolicyHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.PolicyActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val policyContextRoute: Route = new PolicyContextHttpService {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.SparkStreamingContextActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val policyContextRouteNew: Route = new PolicyContextHttpServiceWorkflow {
    implicit val actors = actorsMap
    override val supervisor = actorsMap.get(AkkaConstant.SparkStreamingContextActor).get
    override val actorRefFactory: ActorRefFactory = context
  }.routes

  val AppStatusRoute: Route = new AppStatusHttpService {
    override implicit val actors: Map[String, ActorRef] = actorsMap
    override val supervisor: ActorRef = context.self
    override val actorRefFactory: ActorRefFactory = context
    override val curatorInstance = curatorFramework
  }.routes
}