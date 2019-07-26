/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionEngine, WorkflowExecutionMode}
import com.stratio.sparta.serving.core.models.workflow._
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

    "a workflow execution is passed" should{
      "create correcly a path" in {
        val expected = s"/sparta/$instanceNameHttpService/workflows/home/kafka-to-kafka/kafka-to-kafka-v2/1234-5678"
        val execution = WorkflowExecution(
          id = Option("1234-5678"),
          statuses = Seq(ExecutionStatus()),
          genericDataExecution = GenericDataExecution(wf, WorkflowExecutionMode.local, ExecutionContext())
        )
        WorkflowHelper.getMarathonId(execution) should be (expected)
      }
    }
  }

}
