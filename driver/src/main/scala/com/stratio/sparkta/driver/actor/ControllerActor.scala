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

import akka.actor._
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.stratio.sparkta.driver.constants.AkkaConstant
import com.stratio.sparkta.driver.dto.ErrorDto
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.service.http.{FragmentHttpService, PolicyHttpService}
import com.stratio.sparkta.sdk.JsoneyStringSerializer
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
                      curatorFramework: CuratorFramework) extends HttpServiceActor {

  override  implicit def actorRefFactory: ActorContext = context

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  implicit def exceptionHandler(implicit log: LoggingContext): ExceptionHandler =
    ExceptionHandler {
      case e: Exception =>
        requestUri { uri =>
          log.warning("Request to {} could not be handled normally", uri)
          complete(StatusCodes.NotFound, write(ErrorDto("Error", e.getLocalizedMessage)))
        }
    }

  val policyRoute = new PolicyHttpService {
    val streamingActor = context.actorOf(Props(new StreamingActor(streamingContextService)), "streamingActor")
    override val supervisor = streamingActor
    override implicit def actorRefFactory: ActorRefFactory = context
  }

  val fragmentRoute = new FragmentHttpService {
    val fragmentActor = context.actorOf(Props(new FragmentActor(curatorFramework)), "fragmentActor")
    override val supervisor = fragmentActor
    override implicit def actorRefFactory: ActorRefFactory = context
  }

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(
    policyRoute.routes ~
    fragmentRoute.routes ~
    swaggerService.routes ~
    swaggerUIroutes))

  def swaggerUIroutes: Route =
    get {
      pathPrefix("swagger") {
        pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        }
      } ~ getFromResourceDirectory("swagger-ui")
  }

  val swaggerService = new SwaggerHttpService {
    override def apiTypes: Seq[Type] = Seq(typeOf[PolicyHttpService])
    override def apiVersion: String = "1.0"
    override def baseUrl: String = "/" // let swagger-ui determine the host and port
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
  }
}
