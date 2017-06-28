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

package com.stratio.sparta.serving.core.utils

import com.stratio.sparta.serving.core.models.workflow.WorkflowModel
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PolicyUtilsTest extends BaseUtilsTest with PolicyUtils {

  val utils = spy(this)
  val basePath = "/samplePath"
  val aggModel: WorkflowModel = mock[WorkflowModel]

  "PolicyUtils.policyWithId" should {
    "return a policy with random UUID when there is no set id yet" in {
      val policy: WorkflowModel = utils.policyWithId(getWorkflowModel(None))
      policy.id shouldNot be(None)
    }

    "return a policy with the same id when there is set id" in {
      val policy: WorkflowModel = utils.policyWithId(getWorkflowModel(name = "TEST"))
      policy.id should be(Some("id"))
      policy.name should be("test")
    }
  }

  "PolicyUtils.populatePolicyWithRandomUUID" should {
    "return a policy copy with random UUID" in {
      utils.populatePolicyWithRandomUUID(getWorkflowModel(id = None)).id shouldNot be(None)
    }
  }

  "PolicyUtils.existsByNameId" should {
    "return an existing policy with \"existingId\" from zookeeper" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(Seq(
        getWorkflowModel(id = Some("existingID")),
        getWorkflowModel(id = Some("id#2")),
        getWorkflowModel(id = Some("id#3"))))
        .when(utils)
        .findAllPolicies(withFragments = false)
      utils.existsPolicyByNameId(name = "myName", id = Some("existingID")).get should be(
        getWorkflowModel(id = Some("existingID")))
    }

    "return an existing policy with not defined id but existing name from zookeeper" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(Seq(
        getWorkflowModel(id = Some("id#1"), name = "myname"),
        getWorkflowModel(id = Some("id#2")),
        getWorkflowModel(id = Some("id#3"))))
        .when(utils)
        .findAllPolicies(withFragments = false)

      val actualPolicy: WorkflowModel = utils.existsPolicyByNameId(name = "MYNAME", id = None).get

      actualPolicy.name should be("myname")
      actualPolicy.id.get should be("id#1")
    }

    "return none when there is no policy with neither id or name" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(Seq(
        getWorkflowModel(id = Some("id#1"), name = "myname"),
        getWorkflowModel(id = Some("id#2")),
        getWorkflowModel(id = Some("id#3"))))
        .when(utils)
        .findAllPolicies(withFragments = true)

      utils.existsPolicyByNameId(name = "noName", id = None) should be(None)
    }

    "return none when there is some error or exception" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doThrow(new RuntimeException)
        .when(utils)
        .findAllPolicies(withFragments = true)

      utils.existsPolicyByNameId(name = "noName", id = None) should be(None)
    }

    "return none when path not does not exists" in {
      doReturn(false)
        .when(utils)
        .existsPath

      utils.existsPolicyByNameId(name = "noName", id = None) should be(None)
    }
  }
}
