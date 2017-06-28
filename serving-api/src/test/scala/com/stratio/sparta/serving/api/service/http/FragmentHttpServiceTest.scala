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
import akka.testkit.{TestActor, TestProbe}
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.PolicyActor
import com.stratio.sparta.serving.api.actor.PolicyActor.{DeletePolicy, FindByFragment, ResponsePolicies}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.actor.FragmentActor._
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.workflow.fragment.FragmentElementModel
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class FragmentHttpServiceTest extends WordSpec
  with FragmentHttpService
  with HttpServiceBaseTest {

  val policyTestProbe = TestProbe()
  val fragmentTestProbe = TestProbe()
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))


  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.PolicyActorName -> policyTestProbe.ref,
    AkkaConstant.FragmentActorName -> fragmentTestProbe.ref
  )
  override val supervisor: ActorRef = testProbe.ref

  override def beforeEach(): Unit = {
    SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))
  }

  "FragmentHttpService.findByTypeAndId" should {
    "find a fragment" in {
      startAutopilot(Left(ResponseFragment(Success(getFragmentModel()))))
      Get(s"/${HttpConstant.FragmentPath}/input/id/fragmentId") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByTypeAndId]
        responseAs[FragmentElementModel] should equal((getFragmentModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(ResponseFragment(Failure(new MockException()))))
      Get(s"/${HttpConstant.FragmentPath}/input/id/fragmentId") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByTypeAndId]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.findByTypeAndName" should {
    "find a fragment" in {
      startAutopilot(Left(ResponseFragment(Success(getFragmentModel()))))
      Get(s"/${HttpConstant.FragmentPath}/input/name/fragment") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByTypeAndName]
        responseAs[FragmentElementModel] should equal(getFragmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(ResponseFragment(Failure(new MockException()))))
      Get(s"/${HttpConstant.FragmentPath}/input/name/fragment") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByTypeAndName]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.findAllByType" should {
    "find all fragments" in {
      startAutopilot(Left(ResponseFragments(Success(Seq(getFragmentModel())))))
      Get(s"/${HttpConstant.FragmentPath}/input") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByType]
        responseAs[Seq[FragmentElementModel]] should equal(Seq(getFragmentModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(ResponseFragment(Failure(new MockException()))))
      Get(s"/${HttpConstant.FragmentPath}/input") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByType]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.create" should {
    "return the fragment that was created" in {
      startAutopilot(Left(ResponseFragment(Success(getFragmentModel()))))
      Post(s"/${HttpConstant.FragmentPath}", getFragmentModel) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateFragment]
        responseAs[FragmentElementModel] should equal(getFragmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Response(Failure(new MockException()))))
      Post(s"/${HttpConstant.FragmentPath}", getFragmentModel) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[CreateFragment]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "FragmentHttpService.deleteByTypeAndId" should {
    "return an OK because the fragment was deleted" in {
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindByFragment(input, id, rootUser) =>
              sender ! Left(ResponsePolicies(Success(Seq(getPolicyModel()))))
              TestActor.NoAutoPilot
            case Delete =>
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, policyTestProbe, policyAutoPilot)
      startAutopilot(Left(Response(Success(None))))
      Delete(s"/${HttpConstant.FragmentPath}/input/id/fragmentId") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteByTypeAndId]
        policyTestProbe.expectMsgType[FindByFragment]
        policyTestProbe.expectMsgType[DeletePolicy]
        status should be(StatusCodes.OK)
      }
    }
  }

  "FragmentHttpService.update" should {
    /*"return an OK because the fragment was updated" in {
      val policy = getPolicyModel().copy(fragments = Seq(getFragmentModel()))
      val fragmentAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FragmentActor.Update(input) =>
              sender ! Response(Success(getFragmentModel()))
              TestActor.NoAutoPilot
          }
      })
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case PolicyActor.FindAll() =>
              sender ! ResponsePolicies(Success(Seq(policy)))
              TestActor.NoAutoPilot
            case PolicyActor.Update(policy) =>
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, fragmentTestProbe, fragmentAutoPilot)
      startAutopilot(None, policyTestProbe, policyAutoPilot)
      Put(s"/${HttpConstant.FragmentPath}", getFragmentModel(Some("id"))) ~> routes ~> check {
        fragmentTestProbe.expectMsgType[Update]
        policyTestProbe.expectMsgType[PolicyActor.FindAll]
        policyTestProbe.expectMsgType[PolicyActor.Update]
        status should be(StatusCodes.OK)
      }
    }*/
    /*
    "return a 500 if there was any error" in {
      val fragmentAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FragmentActor.Update(input, rootUser) =>
              sender ! Left(Response(Failure(new MockException())))
              TestActor.NoAutoPilot
          }
      })
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case PolicyActor.FindAll(rootUser) =>
              sender ! Left(ResponsePolicies(Success(Seq(getPolicyModel()))))
              TestActor.NoAutoPilot
            case PolicyActor.Update(policy, rootUser) =>
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, fragmentTestProbe, fragmentAutoPilot)
      startAutopilot(None, policyTestProbe, policyAutoPilot)

      Put(s"/${HttpConstant.FragmentPath}", getFragmentModel(Some("id"))) ~> routes(rootUser) ~> check {
        fragmentTestProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    } */
  }
}
