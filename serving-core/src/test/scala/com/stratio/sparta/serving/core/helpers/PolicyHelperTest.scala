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


package com.stratio.sparta.serving.core.helpers

import com.stratio.sparta.serving.core.models.policy.{OutputFieldsModel, PolicyModel, UserJar}
import com.stratio.sparta.serving.core.utils.BaseUtilsTest
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class PolicyHelperTest extends WordSpecLike with Matchers with MockitoSugar {
  val aggModel: PolicyModel = mock[PolicyModel]

  "PolicyHelper" should {
    "return files" in {
      when(aggModel.userPluginsJars).thenReturn(Seq(UserJar("path1"), UserJar("path2")))

      val files = PolicyHelper.jarsFromPolicy(aggModel)

      files.map(_.getName) shouldBe Seq("path1", "path2")
    }

    "return empty Seq" in {
      when(aggModel.userPluginsJars).thenReturn(Seq(UserJar("")))

      val files = PolicyHelper.jarsFromPolicy(aggModel)

      files.size shouldBe 0
    }
  }
}
