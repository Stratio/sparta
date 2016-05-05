/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.serving.api.actor

import com.stratio.sparkta.serving.api.service.http.{PolicyContextHttpService, TemplateHttpService}

import scala.reflect.runtime.universe._

import akka.actor.{ActorContext, ActorRef}
import akka.event.slf4j.SLF4JLogging
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo
import org.apache.curator.framework.CuratorFramework
import spray.routing._

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.service.handler.CustomExceptionHandler._
import com.stratio.sparta.serving.api.service.http._
import com.stratio.sparta.serving.core.models.SpartaSerializer

class SwaggerActor(actorsMap: Map[String, ActorRef], curatorFramework: CuratorFramework) extends HttpServiceActor
  with SLF4JLogging
  with SpartaSerializer {

  override implicit def actorRefFactory: ActorContext = context

  val serviceRoutes = new ServiceRoutes(actorsMap, context, curatorFramework)

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(getRoutes))

  def getRoutes: Route = swaggerService ~ swaggerUIroutes ~ serviceRoutes.fragmentRoute ~
    serviceRoutes.policyContextRoute ~ serviceRoutes.policyRoute ~ serviceRoutes.templateRoute ~
    serviceRoutes.AppStatusRoute

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
      typeOf[PolicyContextHttpService])

    override def apiVersion: String = "1.0"

    override def baseUrl: String = "/"

    // let swagger-ui determine the host and port
    override def docsPath: String = "api-docs"

    override def actorRefFactory: ActorContext = context

    override def apiInfo: Option[ApiInfo] = Some(new ApiInfo(
      "SpaRTA",
      "A real time aggregation engine full spark based.",
      "TOC Url",
      "Sparta@stratio.com",
      "Apache V2",
      "http://www.apache.org/licenses/LICENSE-2.0"
    ))
  }.routes
}

