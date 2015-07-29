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

import akka.actor._
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.driver.models.{ErrorModel, StreamingContextStatusEnum}
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.constants.AkkaConstant
import com.stratio.sparkta.serving.api.service.http._
import org.apache.curator.framework.CuratorFramework
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext

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

  def receive: Receive = runRoute(handleExceptions(exceptionHandler)(serviceRoutes))

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
}
