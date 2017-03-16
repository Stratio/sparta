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

import com.stratio.sparta.serving.core.config.{SpartaConfigFactory, SpartaConfig}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PolicyStatusUtilsTest extends BaseUtilsTest with PolicyStatusUtils {

  "SparkStreamingContextActor.isAnyPolicyStarted" should {

    "return false if there is no policy Starting/Started mode" in {
      SpartaConfig.initMainConfig(Option(yarnConfig), SpartaConfigFactory(yarnConfig))

      val response = isAnyPolicyStarted
      response should be(false)
    }
  }

  "SparkStreamingContextActor.isContextAvailable" should {
    "return true when execution mode is yarn" in {
      val response = isAvailableToRun(getPolicyModel())
      response should be(true)
    }

    "return false when execution mode is mesos" in {
      SpartaConfig.initMainConfig(Option(mesosConfig), SpartaConfigFactory(mesosConfig))

      val response = isAnyPolicyStarted
      response should be(false)
    }


    "return true when execution mode is local and there is no running policy" in {
      SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))

      val response = isAvailableToRun(getPolicyModel())
      response should be(true)
    }

    "return true when execution mode is standalone and there is no running policy" in {
      SpartaConfig.initMainConfig(Option(standaloneConfig), SpartaConfigFactory(standaloneConfig))

      val response = isAvailableToRun(getPolicyModel())
      response should be(true)
    }
  }
}
