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

package com.stratio.sparta.serving.api.utils

import com.stratio.sparta.serving.core.config.{MockConfigFactory, SpartaConfig}
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.PolicyStatusModel
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class SparkLauncherUtilsTest extends BaseUtilsTest
  with LauncherActorUtils
  with BeforeAndAfter {

  val policyModel = getPolicyModel()
  val spyActor = spy(this)
  val policyStatusModel = PolicyStatusModel(id = "id1", status = PolicyStatusEnum.Started)

  before {
    reset(spyActor)
  }

  "SparkStreamingContextActor.getStreamingContextActor" should {
    "return ClusterLauncherActor" in {

      SpartaConfig.initMainConfig(Option(mesosConfig), new MockConfigFactory(mesosConfig))

      spyActor.getLauncherActor(getPolicyModel(name = "clusterPolicy"),
        statusActorRef,
        streamingContextService,
        statusActor.context)

      verify(spyActor, times(1)).getClusterLauncher(getPolicyModel(name = "clusterPolicy"), statusActorRef,
        statusActor.context, "sparkStreamingContextActor-clusterPolicy")
    }
  }
}
