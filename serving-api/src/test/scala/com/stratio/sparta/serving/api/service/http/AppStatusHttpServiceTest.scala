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
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import com.stratio.sparta.serving.api.constants.HttpConstant
import org.apache.curator.framework.CuratorFramework
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

@RunWith(classOf[JUnitRunner])
class AppStatusHttpServiceTest extends WordSpec
                              with AppStatusHttpService
                              with HttpServiceBaseTest
with MockFactory {

  override implicit val actors: Map[String, ActorRef] = Map()
  override val supervisor: ActorRef = testProbe.ref
  override val curatorInstance = mock[CuratorFramework]

  "AppStatusHttpService" should {
    "check the status of the server" in {
      Get(s"/${HttpConstant.AppStatus}") ~> routes ~> check {
        status should be (StatusCodes.InternalServerError)
      }
    }
  }
}


