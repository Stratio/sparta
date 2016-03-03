/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.serving.api.utils

import org.mockito.Mockito._

class PolicyUtilsTest extends BaseUtilsTest
  with PolicyUtils {

  val utils = spy(this)

  "PolicyUtils.existsByName" should {
    "return true if al least exists one policy with the same name" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(List(getPolicyModel(id = "policy1"), getPolicyModel(id = "policy2"), getPolicyModel(id = "policy3")))
        .when(utils)
        .getPolicies(curatorFramework)

      utils.existsByName(name = "policy1", id = None, curatorFramework = curatorFramework) should be(true)
    }

    "return false if does not exist any policy with the same name" in {
      doReturn(true)
        .when(utils)
        .existsPath
      doReturn(List(getPolicyModel(id = "policy1"), getPolicyModel(id = "policy2"), getPolicyModel(id = "policy3")))
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
}
