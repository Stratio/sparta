/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.authorization.GosecUserConstants
import com.stratio.sparta.serving.core.models.info.AppInfo
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AppInfoHttpServiceTest extends WordSpec
  with AppInfoHttpService
  with HttpServiceBaseTest {

  val dummyUser = Some(GosecUserConstants.AnonymousUser)

  override def cleanUp(): Unit = { system.terminate() }

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
