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

import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatusModel
import org.junit.runner.RunWith
import org.mockito.Mockito.{doReturn, spy}
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class PolicyStatusUtilsTest extends BaseUtilsTest with PolicyStatusUtils {

  val utils = spy(this)

  "SparkStreamingContextActor.isAnyPolicyStarted" should {

    "return false if there is no policy Starting/Started mode" in {
      doReturn(Try(Seq(WorkflowStatusModel("id", PolicyStatusEnum.Stopped))))
        .when(utils)
        .findAllStatuses()
      val response =  utils.isAnyPolicyStarted
      response should be(false)
    }
  }

  "SparkStreamingContextActor.isContextAvailable" should {
    "return true when execution mode is marathon" in {
      doReturn(Try(Seq(WorkflowStatusModel("id", PolicyStatusEnum.Stopped))))
        .when(utils)
        .findAllStatuses()

      val response = isAvailableToRun(getWorkflowModel(Some("id"), "testPolicy", "marathon"))
      response should be(true)
    }

    "return true when execution mode is marathon and started" in {
      doReturn(Try(Seq(WorkflowStatusModel("id", PolicyStatusEnum.Started))))
        .when(utils)
        .findAllStatuses()

      val response = isAvailableToRun(getWorkflowModel(Some("id"), "testPolicy", "marathon"))
      response should be(true)
    }

    "return false when execution mode is mesos" in {
      doReturn(Try(Seq(WorkflowStatusModel("id", PolicyStatusEnum.Stopped))))
        .when(utils)
        .findAllStatuses()
      val response = isAnyPolicyStarted
      response should be(false)
    }

    "return false when execution mode is local and started" in {
      doReturn(Try(Seq(WorkflowStatusModel("id", PolicyStatusEnum.Started))))
        .when(utils)
        .findAllStatuses()

      val response = isAvailableToRun(getWorkflowModel())
      response should be(true)
    }

    "return true when execution mode is local and there is no running policy" in {
      doReturn(Try(Seq(WorkflowStatusModel("id", PolicyStatusEnum.Stopped))))
        .when(utils)
        .findAllStatuses()

      val response = isAvailableToRun(getWorkflowModel())
      response should be(true)
    }
  }
}
