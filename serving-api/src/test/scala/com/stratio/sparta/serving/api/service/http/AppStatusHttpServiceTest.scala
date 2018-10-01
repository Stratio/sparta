/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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

  override def cleanUp(): Unit = { system.terminate() }

  override implicit val actors: Map[String, ActorRef] = Map()
  override val supervisor: ActorRef = testProbe.ref

  "AppStatusHttpService" should {
    "check the status of the server" in {
      Get(s"/${HttpConstant.AppStatus}") ~> routes() ~> check {
        status should be (StatusCodes.InternalServerError)
      }
    }
  }
}


