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

import org.apache.curator.framework.CuratorFramework

import scala.reflect.runtime.universe._

import akka.actor.{ActorContext, ActorRef}
import akka.event.slf4j.SLF4JLogging
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.stratio.sparkta.serving.core.exception.ServingCoreException
import com.wordnik.swagger.model.ApiInfo
import org.json4s.jackson.Serialization.write
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext

import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.api.service.http._
import com.stratio.sparkta.serving.core.models.{ErrorModel, SparktaSerializer}

class SwaggerActor(actorsMap: Map[String, ActorRef], curatorFramework : CuratorFramework)
  extends HttpServiceActor with SLF4JLogging with SparktaSerializer {

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

  val serviceRoutes = new ServiceRoutes(actorsMap, context, curatorFramework)

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(getRoutes))

  def getRoutes: Route = swaggerService ~ swaggerUIroutes ~ serviceRoutes.fragmentRoute ~
    serviceRoutes.policyContextRoute ~ serviceRoutes.policyRoute ~ serviceRoutes.templateRoute ~
    serviceRoutes.AppStatusRoute ~ serviceRoutes.policyContextRouteNew

  def swaggerUIroutes: Route =
    get {
      pathPrefix(HttpConstant.SwaggerPath) {
        pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        }
      } ~ getFromResourceDirectory("swagger-ui")
    }

  val swaggerService = new SwaggerHttpService {
    override def apiTypes: Seq[Type] = Seq(
      typeOf[FragmentHttpService],
      typeOf[TemplateHttpService],
      typeOf[PolicyHttpService],
      typeOf[PolicyContextHttpService],
      typeOf[PolicyContextHttpServiceWorkflow])

    override def apiVersion: String = "1.0"

    override def baseUrl: String = "/"

    // let swagger-ui determine the host and port
    override def docsPath: String = "api-docs"

    override def actorRefFactory: ActorContext = context

    override def apiInfo: Option[ApiInfo] = Some(new ApiInfo(
      "SpaRkTA",
      "A real time aggregation engine full spark based.",
      "TOC Url",
      "Sparkta@stratio.com",
      "Apache V2",
      "http://www.apache.org/licenses/LICENSE-2.0"
    ))
  }.routes
}

