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

import com.stratio.sparta.serving.core.models.policy.{OutputFieldsModel, PolicyModel, UserJar}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PolicyUtilsTest extends BaseUtilsTest
  with PolicyUtils {

  val utils = spy(this)
  val basePath = "/samplePath"
  val aggModel: PolicyModel = mock[PolicyModel]

  "PolicyUtils" should {
    "return files" in {
      when(aggModel.userPluginsJars).thenReturn(Seq(UserJar("path1"), UserJar("path2")))

      val files = jarsFromPolicy(aggModel)

      files.map(_.getName) shouldBe(Seq("path1", "path2"))
    }

    "return empty Seq" in {
      when(aggModel.userPluginsJars).thenReturn(Seq(UserJar("")))

      val files = jarsFromPolicy(aggModel)

      files.size shouldBe(0)
    }
  }

  "PolicyUtils.savePolicyInZk" should {

    "write policy when this does not exist" in {
      doReturn(getPolicyModel())
        .when(utils)
        .writePolicy(getPolicyModel(), curatorFramework)

      utils.savePolicyInZk(policy = getPolicyModel(), curatorFramework)

      verify(utils).writePolicy(getPolicyModel(), curatorFramework)
    }
  }

  "PolicyUtils.policyWithId" should {
    "return a policy with random UUID when there is no set id yet" in {
      val policy: PolicyModel = utils.policyWithId(getPolicyModel(None))
      policy.id shouldNot be(None)
    }

    "return a policy with the same id when there is set id" in {
      val policy: PolicyModel = utils.policyWithId(getPolicyModel(name = "TEST"))
      policy.id should be(Some("id"))
      policy.name should be("test")
    }
  }

  "PolicyUtils.populatePolicyWithRandomUUID" should {
    "return a policy copy with random UUID" in {
      utils.populatePolicyWithRandomUUID(getPolicyModel(id = None)).id shouldNot be(None)
    }
  }

  "PolicyUtils.existsByNameId" should {
    "return an existing policy with \"existingId\" from zookeper" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(Seq(
        getPolicyModel(id = Some("existingID")),
        getPolicyModel(id = Some("id#2")),
        getPolicyModel(id = Some("id#3"))))
        .when(utils)
        .getPolicies(curatorFramework, withFragments = false)
      utils.existsByNameId(name = "myName", id = Some("existingID"), curatorFramework).get should be(
        getPolicyModel(id = Some("existingID")))
    }

    "return an existing policy with not defined id but existing name from zookeper" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(Seq(
        getPolicyModel(id = Some("id#1"), name = "myname"),
        getPolicyModel(id = Some("id#2")),
        getPolicyModel(id = Some("id#3"))))
        .when(utils)
        .getPolicies(curatorFramework, withFragments = false)

      val actualPolicy: PolicyModel = utils.existsByNameId(name = "MYNAME", id = None,
        curatorFramework).get

      actualPolicy.name should be("myname")
      actualPolicy.id.get should be("id#1")
    }

    "return none when there is no policy with neither id or name" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(Seq(
        getPolicyModel(id = Some("id#1"), name = "myname"),
        getPolicyModel(id = Some("id#2")),
        getPolicyModel(id = Some("id#3"))))
        .when(utils)
        .getPolicies(curatorFramework, withFragments = true)

      utils.existsByNameId(name = "noName", id = None, curatorFramework) should be(None)
    }

    "return none when there is some error or exception" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doThrow(new RuntimeException)
        .when(utils)
        .getPolicies(curatorFramework, withFragments = true)

      utils.existsByNameId(name = "noName", id = None, curatorFramework) should be(None)
    }

    "return none when path not does not exists" in {
      doReturn(false)
        .when(utils)
        .existsPath

      utils.existsByNameId(name = "noName", id = None, curatorFramework) should be(None)
    }
  }
}
