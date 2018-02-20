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

package com.stratio.sparta.dg.agent.commons

import com.stratio.governance.commons.agent.model.metadata.MetadataPath
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType
import com.stratio.sparta.dg.agent.model.SpartaWorkflowStatusMetadata
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WorkflowStatusUtilsTest extends WordSpec with Matchers {
  val nodes = Seq(
    NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
    NodeGraph("b", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )
  val validPipeGraph = PipelineGraph(nodes , edges)
  val emptyPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
  val settingsModel = Settings(
    GlobalSettings("local", Seq.empty, Seq.empty, true ,Some(JsoneyString("constraint1:constraint2"))),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(
      JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false,
      sparkMesosSecurity = false, None, SubmitArguments(), SparkConf(SparkResourcesConf()))
  )

  val pipeline = PipelineGraph(nodes , edges)
  val testWorkflow = Workflow(Option("qwerty12345"), "kafka-test",
    settings = settingsModel,
    pipelineGraph = pipeline,
    group = Group(Option("987654"), "/home/test/subgroup"),
    lastUpdateDate = Option(new DateTime(1519051473L))
  )


  "WorkflowStatusUtils.statusMetadataLineage" should {
    "return None" when {
      "we receive a transient state event" in {
        val statusEvent: WorkflowStatusStream = WorkflowStatusStream(
          WorkflowStatus("qwerty12345", WorkflowStatusEnum.Starting),
          Some(testWorkflow),
          None
        )
        LineageUtils.statusMetadataLineage(statusEvent) should be (None)
      }

      "the event has no workflow info associated to the status" in {
        val statusEvent: WorkflowStatusStream = WorkflowStatusStream(
          WorkflowStatus("qwerty12345", WorkflowStatusEnum.Finished),
          None,
          None
        )
        LineageUtils.statusMetadataLineage(statusEvent) should be (None)
      }
    }

    "return a List[SpartaWorkflowStatusMetadata]" in {
      val statusEvent: WorkflowStatusStream = WorkflowStatusStream(
        WorkflowStatus("qwerty12345", WorkflowStatusEnum.Finished,
          lastUpdateDate = Option(new DateTime(1519051473L))),
        Some(testWorkflow),
        None
      )
      val expected = SpartaWorkflowStatusMetadata("kafka-test",
        EventType.Success,
        None,
        "qwerty12345",
        MetadataPath(Seq("sparta","_home_test_subgroup","kafka-test","0","1519051473")),
        tags = List.empty[String],
        modificationTime = Option(1519051473L),
        accessTime = Option(1519051473L)
      )

      LineageUtils.statusMetadataLineage(statusEvent) should equal (Some(List(expected)))
    }
  }


}
