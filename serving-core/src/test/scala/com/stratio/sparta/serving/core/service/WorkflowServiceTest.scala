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
package com.stratio.sparta.serving.core.service

import java.util

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.workflow.{PipelineGraph, Settings, Workflow}
import com.stratio.sparta.serving.core.services.WorkflowService
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.zookeeper.data.Stat
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class WorkflowServiceTest extends WordSpecLike
with BeforeAndAfter
  with MockitoSugar
  with Matchers{

  val curatorFramework = mock[CuratorFramework]
  val existsBuilder = mock[ExistsBuilder]
  val getChildrenBuilder = mock[GetChildrenBuilder]
  val getDataBuilder = mock[GetDataBuilder]
  val createBuilder = mock[CreateBuilder]
  val setDataBuilder = mock[SetDataBuilder]
  val deleteBuilder = mock[DeleteBuilder]
  val protectedACL = mock[ProtectACLCreateModeStatPathAndBytesable[String]]
  val settings = mock[Settings]
  val pipeGraph = mock[PipelineGraph]

  val workflowService = new WorkflowService(curatorFramework)
  val workflowID = "wf1"
  val newWorkflowID = "wf2"
  val testWorkflow =  Workflow(Option(workflowID),"wfTest","", settings ,pipeGraph)
  val newWorkflow = Workflow(Option(newWorkflowID),"wfTest2","", settings ,pipeGraph)
  val workflowRaw =
    """
      |{
      |"id": "wf1",
      |"name": "wfTest",
      |"description": "",
      |"settings": {},
      |"pipelineGraph": {
      |    "nodes": [
      |      {
      |        "name": "kafka",
      |        "stepType": "Input",
      |        "className": "KafkaInputStep",
      |        "classPrettyName": "Kafka",
      |        "writer": {
      |          "autoCalculatedFields": [],
      |          "saveMode": "Append",
      |          "tableName": "inputkafka"
      |        },
      |        "configuration": {
      |          "outputField": "raw",
      |          "outputType": "string",
      |          "key.deserializer": "string",
      |          "locationStrategy": "preferconsistent",
      |          "topics": [
      |            {
      |              "topic": "offsetspr"
      |            }
      |          ],
      |          "auto.offset.reset": "latest",
      |          "partition.assignment.strategy": "range",
      |          "vaultTLSEnable": false,
      |          "offsets": [],
      |          "group.id": "sparta",
      |          "bootstrap.servers": [
      |            {
      |              "host": "127.0.0.1",
      |              "port": "9092"
      |            }
      |          ],
      |          "enable.auto.commit": false,
      |          "value.deserializer": "string",
      |          "kafkaProperties": [
      |            {
      |              "kafkaPropertyKey": "",
      |              "kafkaPropertyValue": ""
      |            }
      |          ],
      |          "storeOffsetInKafka": true
      |         }
      |       }
      |     ]
      |    }
      |}
    """.stripMargin

  val newWorkflowRaw =
    """
      |{
      |"id": "wf2",
      |"name": "wfTest2",
      |"description": "",
      |"settings": {},
      |"pipelineGraph": {}
    """.stripMargin

  before{
    CuratorFactoryHolder.setInstance(curatorFramework)
  }

  "workflowService" must {

    "existByName: returns an option wrapping a workflow with the matching name" in {
      existByNameMock
      val result = workflowService.existsByName("wfTest")

      result.get.name shouldBe "wfTest"
    }

    "existById: returns an option wrapping a workflow with the matching ID" in {
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowsZkPath}/$workflowID"))
        .thenReturn(new Stat)

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowsZkPath}/$workflowID"))
        .thenReturn(workflowRaw.getBytes)

      val result = workflowService.existsById(workflowID)

      result.get shouldBe a[Workflow]
    }

    "findAll: should return a list with all the workflows" in {
      mockListOfWorkflows
      mockFindByID

      val result = workflowService.findAll

      result shouldBe a[Seq[_]]
    }

    "findIdList: should return a list with the workflows" in {
      mockListOfWorkflows
      mockFindByID

      val result = workflowService.findByIdList(Seq("wf1"))

      result shouldBe a[Seq[_]]
    }

    "findByTemplateType: should return a list with all the workflow of a given type" in {
      mockListOfWorkflows
      mockFindByID

      val result = workflowService.findByTemplateType("KafkaInputStep")

     result.head.pipelineGraph.nodes.head.stepType shouldBe "Input"
    }

    "findByTemplateName: should return a list with all the workflow of a given type" in {
      mockListOfWorkflows
      mockFindByID

      val result = workflowService.findByTemplateName("KafkaInputStep", "kafka")

      result.head.pipelineGraph.nodes.head.name shouldBe "kafka"
    }

    "create: given a certain workflow model a new workflow should be created" in {
      existByNameMock

      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath(s"${AppConstant.WorkflowsZkPath}/newWorkflow"))
        .thenReturn(newWorkflowRaw)

      val result = workflowService.create(newWorkflow)

      result.id.get shouldBe "wf2"

    }

    "createList: given a certain workflow list a new workflows should be created" in {
      existByNameMock

      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath(s"${AppConstant.WorkflowsZkPath}/newWorkflow"))
        .thenReturn(newWorkflowRaw)

      val result = workflowService.createList(Seq(newWorkflow))

      result.head.id.get shouldBe "wf2"

    }

    "update: given a certain workflow if a matching id is found the information regarding that " +
      "workflow is updated" in {
      existByNameMock

      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath(s"${AppConstant.WorkflowsZkPath}/$workflowID"))
        .thenReturn(new Stat)

      val result = workflowService.update(testWorkflow)
      result shouldBe a[Workflow]
    }

    "updateList: given a certain workflow list if a matching id is found the information regarding that " +
      "workflows is updated" in {
      existByNameMock

      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath(s"${AppConstant.WorkflowsZkPath}/$workflowID"))
        .thenReturn(new Stat)

      val result = workflowService.updateList(Seq(testWorkflow))
      result shouldBe a[Seq[Workflow]]
    }

    "delete: given an id, looks up for a workflow with a matching id and deletes it" in {
      when(curatorFramework.delete)
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete
        // scalastyle:off null
        .forPath(s"${AppConstant.WorkflowsZkPath}/$workflowID"))
        .thenReturn(null)


      val result = workflowService.delete(workflowID)
    }

    def mockFindByID: OngoingStubbing[Array[Byte]] = {
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowsZkPath}/wf1"))
        .thenReturn(new Stat)

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowsZkPath}/wf1"))
        .thenReturn(workflowRaw.getBytes)
    }

    def mockListOfWorkflows: OngoingStubbing[util.List[String]] = {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath(s"${AppConstant.WorkflowsZkPath}"))
        .thenReturn(new util.ArrayList[String]() {
          add("wf1")
        })
    }

    def existByNameMock: OngoingStubbing[Array[Byte]]= {
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowsZkPath}"))
        .thenReturn(new Stat)

      mockListOfWorkflows
      mockFindByID
    }
  }
}
