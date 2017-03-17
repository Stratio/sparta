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

package com.stratio.sparta.serving.core.marathon

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import com.stratio.tikitakka.common.exceptions.ConfigurationException
import com.stratio.tikitakka.common.util.ConfigComponent
import com.stratio.tikitakka.updown.marathon.MarathonComponent
import SpartaMarathonComponent._

trait SpartaMarathonComponent extends MarathonComponent {

  override lazy val uri = ConfigComponent.getString(SpartaMarathonComponent.uriField).getOrElse {
    throw ConfigurationException("The marathon uri has not been set")
  }

  override lazy val apiVersion = ConfigComponent.getString(versionField, defaultApiVersion)
}

object SpartaMarathonComponent {

  // Property field constants
  val uriField = "sparta.marathon.tikitakka.marathon.uri"
  val versionField = "sparta.marathon.tikitakka.marathon.api.version"

  // Default property constants
  val defaultApiVersion = "v2"

  val upComponentMethod = POST
  val downComponentMethod = DELETE

  def apply(implicit _system: ActorSystem, _materializer: ActorMaterializer): SpartaMarathonComponent =
    new SpartaMarathonComponent {
      implicit val actorMaterializer: ActorMaterializer = _materializer
      implicit val system: ActorSystem = _system
    }
}
