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
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.dto.LoggedUserConstant
import com.stratio.sparta.serving.core.models.info.AppInfo
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AppInfoHttpServiceTest extends WordSpec
  with AppInfoHttpService
  with HttpServiceBaseTest {

  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map.empty[String, ActorRef]

  override val supervisor: ActorRef = testProbe.ref

  protected def retrieveAppInfo: AppInfo =
    AppInfo(pomVersion = "x.x.x",
      profileId = "production",
      buildTimestamp = "",
      devContact = "sparta@stratio.com",
      supportContact = "support@stratio.com",
      description = "",
      license = "license")

  "AppInfoHttpService.Get" should {
    "retrieve a AppInfo item" in {
      Get(s"/${HttpConstant.AppInfoPath}") ~> routes(dummyUser) ~> check {
        responseAs[AppInfo] should equal(InfoHelper.getAppInfo)
      }
    }
  }

}
