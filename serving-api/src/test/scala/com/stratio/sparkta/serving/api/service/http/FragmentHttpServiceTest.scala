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

import scala.util.{Failure, Success}

import akka.actor.ActorRef
import akka.testkit.{TestActor, TestProbe}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import com.stratio.sparkta.sdk.exception.MockException
import com.stratio.sparkta.serving.api.actor.PolicyActor.{FindByFragment, ResponsePolicies}
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.actor.FragmentActor._
import com.stratio.sparkta.serving.core.constants.AkkaConstant
import com.stratio.sparkta.serving.core.models.FragmentElementModel

@RunWith(classOf[JUnitRunner])
class FragmentHttpServiceTest extends WordSpec
with FragmentHttpService
with HttpServiceBaseTest {

  val policyTestProbe = TestProbe()

  override implicit val actors: Map[String, ActorRef] = Map(AkkaConstant.PolicyActor -> policyTestProbe.ref)
  override val supervisor: ActorRef = testProbe.ref

  "FragmentHttpService.findByTypeAndId" should {
    "find a fragment" in {
      startAutopilot(ResponseFragment(Success(getFragmentModel())))
      Get(s"/${HttpConstant.FragmentPath}/input/id") ~> routes ~> check {
        testProbe.expectMsgType[FindByTypeAndId]
        responseAs[FragmentElementModel] should equal(getFragmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(ResponseFragment(Failure(new MockException())))
      Get(s"/${HttpConstant.FragmentPath}/input/id") ~> routes ~> check {
        testProbe.expectMsgType[FindByTypeAndId]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.findByTypeAndName" should {
    "find a fragment" in {
      startAutopilot(ResponseFragment(Success(getFragmentModel())))
      Get(s"/${HttpConstant.FragmentPath}/input/name/fragment") ~> routes ~> check {
        testProbe.expectMsgType[FindByTypeAndName]
        responseAs[FragmentElementModel] should equal(getFragmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(ResponseFragment(Failure(new MockException())))
      Get(s"/${HttpConstant.FragmentPath}/input/name/fragment") ~> routes ~> check {
        testProbe.expectMsgType[FindByTypeAndName]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.findAllByType" should {
    "find all fragments" in {
      startAutopilot(ResponseFragments(Success(Seq(getFragmentModel()))))
      Get(s"/${HttpConstant.FragmentPath}/input") ~> routes ~> check {
        testProbe.expectMsgType[FindByType]
        responseAs[Seq[FragmentElementModel]] should equal(Seq(getFragmentModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(ResponseFragment(Failure(new MockException())))
      Get(s"/${HttpConstant.FragmentPath}/input") ~> routes ~> check {
        testProbe.expectMsgType[FindByType]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.create" should {
    "return the fragment that was created" in {
      startAutopilot(ResponseFragment(Success(getFragmentModel())))
      Post(s"/${HttpConstant.FragmentPath}", getFragmentModel) ~> routes ~> check {
        testProbe.expectMsgType[Create]
        responseAs[FragmentElementModel] should equal(getFragmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Response(Failure(new MockException())))
      Post(s"/${HttpConstant.FragmentPath}", getFragmentModel) ~> routes ~> check {
        testProbe.expectMsgType[Create]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.update" should {
    "return an OK because the fragment was updated" in {
      startAutopilot(Response(Success(getFragmentModel())))
      Put(s"/${HttpConstant.FragmentPath}", getFragmentModel) ~> routes ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Response(Failure(new MockException())))
      Put(s"/${HttpConstant.FragmentPath}", getFragmentModel) ~> routes ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.deleteByTypeAndId" should {
    "return an OK because the fragment was deleted" in {
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindByFragment(input, id) =>
              sender ! ResponsePolicies(Success(Seq(getPolicyModel())))
              TestActor.NoAutoPilot
            case Delete => TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, policyTestProbe, policyAutoPilot)
      startAutopilot(Response(Success(None)))
      Delete(s"/${HttpConstant.FragmentPath}/input/id") ~> routes ~> check {
        testProbe.expectMsgType[DeleteByTypeAndId]
        policyTestProbe.expectMsgType[FindByFragment]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error when a fragment is deleted" in {
      startAutopilot(Response(Failure(new MockException())))
      Delete(s"/${HttpConstant.FragmentPath}/input/id") ~> routes ~> check {
        testProbe.expectMsgType[DeleteByTypeAndId]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
