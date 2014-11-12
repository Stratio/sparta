/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.actor

import akka.actor.{ActorContext, ActorRef}
import com.stratio.sparkta.driver.route.PolicyRoutes

/**
 * Created by ajnavarro on 29/10/14.
 */
class PolicyControllerActor(override val supervisor: ActorRef) extends InstrumentedActor with PolicyRoutes {

  def actorRefFactory: ActorContext = context

  def receive: Receive = runRoute(policyRoutes)

}
