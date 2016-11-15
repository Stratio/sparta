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

import com.stratio.sparta.serving.core.models.{AggregationPoliciesModel, OutputFieldsModel, UserJar}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PolicyUtilsTest extends PolicyBaseUtilsTest
  with PolicyUtils {

  val utils = spy(this)
  val basePath = "/samplePath"
  val aggModel: AggregationPoliciesModel = mock[AggregationPoliciesModel]

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

  "PolicyUtils.existsByName" should {
    "return true if al least exists one policy with the same name" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(List(
        getPolicyModel(name = "policy1"),
        getPolicyModel(name = "policy2"),
        getPolicyModel(name = "policy3")))
        .when(utils)
        .getPolicies(curatorFramework)

      utils.existsByName(name = "policy1", id = None, curatorFramework = curatorFramework) should be(true)
    }

    "return false if does not exist any policy with the same name" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(List(
        getPolicyModel(name = "policy1"),
        getPolicyModel(name = "policy2"),
        getPolicyModel(name = "policy3")))
        .when(utils)
        .getPolicies(curatorFramework)

      utils.existsByName(name = "policy0", id = None, curatorFramework = curatorFramework) should be(false)
    }


    "return false if does not exist the path" in {
      doReturn(false)
        .when(utils)
        .existsPath

      utils.existsByName(name = "policy0", id = None, curatorFramework = curatorFramework) should be(false)
    }

    "return false if any exception is thrown" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doThrow(new RuntimeException)
        .when(utils)
        .getPolicies(curatorFramework)

      utils.existsByName(name = "policy0", id = None, curatorFramework = curatorFramework) should be(false)
    }
  }

  "PolicyUtils.savePolicyInZk" should {
    "update policy when this exists" in {
      doReturn(getPolicyModel())
        .when(utils)
        .populatePolicy(getPolicyModel(), curatorFramework)
      doNothing()
        .when(utils)
        .updatePolicy(getPolicyModel(), curatorFramework)

      utils.savePolicyInZk(policy = getPolicyModel(), curatorFramework)

      verify(utils).updatePolicy(getPolicyModel(), curatorFramework)
    }

    "write policy when this does not exist" in {
      doThrow(new RuntimeException)
        .when(utils)
        .populatePolicy(getPolicyModel(), curatorFramework)
      doNothing()
        .when(utils)
        .writePolicy(getPolicyModel(), curatorFramework)

      utils.savePolicyInZk(policy = getPolicyModel(), curatorFramework)

      verify(utils).writePolicy(getPolicyModel(), curatorFramework)
    }
  }

  "PolicyUtils.policyWithId" should {
    "return a policy with random UUID when there is no set id yet" in {
      val policy: AggregationPoliciesModel = utils.policyWithId(getPolicyModel(None))
      policy.version should be(Some(1))
      policy.id shouldNot be(None)
    }

    "return a policy with the same id when there is set id" in {
      val policy: AggregationPoliciesModel = utils.policyWithId(getPolicyModel(name = "TEST"))
      policy.version should be(Some(1))
      policy.id should be(Some("id"))
      policy.name should be("test")
    }
  }

  "PolicyUtils.populatePolicyWithRandomUUID" should {
    "return a policy copy with random UUID" in {
      utils.populatePolicyWithRandomUUID(getPolicyModel(id = None)).id shouldNot be(None)
    }
  }

  "PolicyUtils.deleteCheckpointPath" should {
    "delete path from HDFS when using not local mode" in {
      doReturn(false)
        .when(utils)
        .isLocalMode

      utils.deleteCheckpointPath(getPolicyModel())

      verify(utils, times(1)).deleteFromHDFS(getPolicyModel())
    }

    "delete path from local when using local mode" in {
      doReturn(true)
        .when(utils)
        .isLocalMode

      doReturn(false)
        .when(utils)
        .isHadoopEnvironmentDefined

      utils.deleteCheckpointPath(getPolicyModel())

      verify(utils, times(1)).deleteFromLocal(getPolicyModel())
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
        .getPolicies(curatorFramework)
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
        .getPolicies(curatorFramework)

      val actualPolicy: AggregationPoliciesModel = utils.existsByNameId(name = "MYNAME", id = None,
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
        .getPolicies(curatorFramework)

      utils.existsByNameId(name = "noName", id = None, curatorFramework) should be(None)
    }

    "return none when there is some error or exception" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doThrow(new RuntimeException)
        .when(utils)
        .getPolicies(curatorFramework)

      utils.existsByNameId(name = "noName", id = None, curatorFramework) should be(None)
    }

    "return none when path not does not exists" in {
      doReturn(false)
        .when(utils)
        .existsPath

      utils.existsByNameId(name = "noName", id = None, curatorFramework) should be(None)
    }

    "PolicyUtils.setVersion" should {
      "return lastVersion+1 when cubes are different and lastPolicy has version" in {
        val lastPolicy = getPolicyModel().copy(version = Some(1))
        val newPolicy = getPolicyModel().copy(cubes = Seq(
          populateCube(
            "cube1",
            OutputFieldsModel("out1"),
            OutputFieldsModel("out2"),
            getDimensionModel,
            getOperators),
          populateCube(
            "cube2",
            OutputFieldsModel("out1"),
            OutputFieldsModel("out2"),
            getDimensionModel,
            getOperators)
        ))
        utils.setVersion(lastPolicy, newPolicy) should be(Some(2))
      }

      "return 1 when cubes are different and lastPolicy has no version" in {
        val lastPolicy = getPolicyModel()
        val newPolicy = getPolicyModel().copy(cubes = Seq(
          populateCube(
            "cube1",
            OutputFieldsModel("out1"),
            OutputFieldsModel("out2"),
            getDimensionModel,
            getOperators),
          populateCube(
            "cube2",
            OutputFieldsModel("out1"),
            OutputFieldsModel("out2"),
            getDimensionModel,
            getOperators)
        ))
        utils.setVersion(lastPolicy, newPolicy) should be(Some(1))
      }

      "return lastPolicy version when cubes equal " in {
        val lastPolicy = getPolicyModel().copy(version = Some(1))
        val newPolicy = getPolicyModel()
        utils.setVersion(lastPolicy, newPolicy) should be(Some(1))
      }
    }
  }
}
