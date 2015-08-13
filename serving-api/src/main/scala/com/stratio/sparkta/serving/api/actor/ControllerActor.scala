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

import akka.actor.{ActorContext, _}
import akka.event.slf4j.SLF4JLogging
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.stratio.sparkta.driver.constants.AkkaConstant
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.api.service.http._
import com.stratio.sparkta.serving.core.models.{ErrorModel, StreamingContextStatusEnum}
import com.wordnik.swagger.model.ApiInfo
import org.apache.curator.framework.CuratorFramework
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext

import scala.reflect.runtime.universe._

class ControllerActor(streamingContextService: StreamingContextService,
                      curatorFramework: CuratorFramework,
                      actorsMap: Map[String, ActorRef]) extends HttpServiceActor with SLF4JLogging {

  override implicit def actorRefFactory: ActorContext = context

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  implicit def exceptionHandler(implicit logg: LoggingContext): ExceptionHandler =
    ExceptionHandler {
      case exception: Exception =>
        requestUri { uri =>
          log.error(exception.getLocalizedMessage, exception)
          complete(StatusCodes.NotFound, write(ErrorModel("Error", exception.getLocalizedMessage)))
        }
    }

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(serviceRoutes ~ swaggerService ~ swaggerUIroutes))

  val serviceRoutes: Route =
    new JobServerHttpService {
      implicit val actors = actorsMap
      override val supervisor =
        if (actorsMap.contains(AkkaConstant.JobServerActor)) actorsMap.get(AkkaConstant.JobServerActor).get
        else context.self

      override implicit def actorRefFactory: ActorRefFactory = context
    }.routes ~
      new FragmentHttpService {
        implicit val actors = actorsMap
        override val supervisor =
          if (actorsMap.contains(AkkaConstant.FragmentActor)) actorsMap.get(AkkaConstant.FragmentActor).get
          else context.self

        override implicit def actorRefFactory: ActorRefFactory = context
      }.routes ~
      new TemplateHttpService {
        implicit val actors = actorsMap
        override val supervisor =
          if (actorsMap.contains(AkkaConstant.TemplateActor)) actorsMap.get(AkkaConstant.TemplateActor).get
          else context.self

        override implicit def actorRefFactory: ActorRefFactory = context
      }.routes ~
      new PolicyHttpService {
        implicit val actors = actorsMap
        override val supervisor =
          if (actorsMap.contains(AkkaConstant.PolicyActor)) actorsMap.get(AkkaConstant.PolicyActor).get
          else context.self

        override implicit def actorRefFactory: ActorRefFactory = context
      }.routes ~
      new PolicyContextHttpService {
        implicit val actors = actorsMap
        override val supervisor =
          if (actorsMap.contains(AkkaConstant.StreamingActor)) actorsMap.get(AkkaConstant.StreamingActor).get
          else context.self

        override implicit def actorRefFactory: ActorRefFactory = context
      }.routes

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
      typeOf[JobServerHttpService],
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
      "SpaRkTA",
      "A real time aggregation engine full spark based.",
      "TOC Url",
      "Sparkta@stratio.com",
      "Apache V2",
      "http://www.apache.org/licenses/LICENSE-2.0"
    ))
  }.routes
}
