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

import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.workflow.{Group, PipelineGraph, Settings, Workflow}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import com.stratio.sparta.serving.core.constants.AppConstant._

@RunWith(classOf[JUnitRunner])
class WorkflowHelperTest extends WordSpec with ShouldMatchers with Matchers with MockitoSugar{
  "A WorkflowHelper" when{
    val wf = Workflow(Option("aaa"), "kafka-to-kafka", "Default description", mock[Settings],
      mock[PipelineGraph], WorkflowExecutionEngine.Streaming, None,None,None, 2L , DefaultGroup)

    "a multilevel group is passed" should{
      "parse it correctly" in {
        val group = "/groupLevel1/groupLevel2/groupLevel3/"
        val expected = "groupLevel1/groupLevel2/groupLevel3"
        WorkflowHelper.retrieveGroup(group) should be (expected)
      }
    }

    "a simple group is passed" should{
      "parse it correctly" in {
        val group = "/groupLevel1/"
        val expected = "groupLevel1"
        WorkflowHelper.retrieveGroup(group) should be (expected)

        val group1 = "groupLevel1"
        val expected1 = "groupLevel1"
        WorkflowHelper.retrieveGroup(group1) should be (expected1)
      }
    }

    "a workflow is passed" should{
      "create correcly a path" in {
        //It is "undefined" instead of sparta-server or any tenant because there is no Environment Variable
        val expected = "sparta/undefined/workflows/home/kafka-to-kafka/kafka-to-kafka-v2"
        WorkflowHelper.getMarathonId(wf) should be (expected)
      }
    }
  }

}
