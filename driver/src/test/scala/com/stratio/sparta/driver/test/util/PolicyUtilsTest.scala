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
package com.stratio.sparta.driver.test.util

import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import com.stratio.sparta.driver.util.PolicyUtils
import com.stratio.sparta.serving.core.models.{AggregationPoliciesModel, UserJar}

@RunWith(classOf[JUnitRunner])
class PolicyUtilsTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  val aggModel: AggregationPoliciesModel = mock[AggregationPoliciesModel]

  "PolicyUtils" should "return files" in {
    when(aggModel.userPluginsJars).thenReturn(Seq(UserJar("path1"), UserJar("path2")))

    val files = PolicyUtils.jarsFromPolicy(aggModel)

    files.map(_.getName) shouldBe(Seq("path1", "path2"))
  }

  it should "return empty Seq" in {
    when(aggModel.userPluginsJars).thenReturn(Seq(UserJar("")))

    val files = PolicyUtils.jarsFromPolicy(aggModel)

    files.size shouldBe(0)
  }
}
