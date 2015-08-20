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

package com.stratio.sparkta.serving.api.service.http

import akka.actor.{ActorRefFactory, ActorContext, ActorRef}
import com.stratio.sparkta.driver.constants.AkkaConstant
import spray.routing._

class ServiceRoutes (actorsMap: Map[String, ActorRef], context : ActorContext) extends RouteConcatenation {

  val serviceRoutes: Route =
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

