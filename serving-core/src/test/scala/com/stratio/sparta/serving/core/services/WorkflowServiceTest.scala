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
package com.stratio.sparta.serving.core.services

import java.util

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.zookeeper.data.Stat
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import com.stratio.sparta.serving.core.constants.AppConstant._

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
  val settings = Settings()

  val nodes = Seq(
    NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
    NodeGraph("b", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )
  val pipeGraph = PipelineGraph(nodes , edges)
  val wrongPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])

  val workflowService = new WorkflowService(curatorFramework)
  val workflowID = "wf1"
  val newWorkflowID = "wf2"
  val testWorkflow =  Workflow(Option(workflowID),"wf-test","", settings , pipeGraph)
  val newWorkflow = Workflow(Option(newWorkflowID),"wf-test2","", settings , pipeGraph)
  val wrongWorkflow = Workflow(Option(newWorkflowID),"wf-test3","", settings ,wrongPipeGraph)

  val workflowRaw =
    """{
      |  "id": "wf1",
      |  "name": "wf-test",
      |  "description": "",
      |  "settings": {},
      |  "version": 0,
      |  "group": {
      |   "name" : "/home",
      |   "id" : "940800b2-6d81-44a8-84d9-26913a2faea4" },
      |  "pipelineGraph": {
      |    "nodes": [{
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
      |          "topics": [{
      |            "topic": "offsetspr"
      |          }],
      |          "auto.offset.reset": "latest",
      |          "partition.assignment.strategy": "range",
      |          "vaultTLSEnable": false,
      |          "offsets": [],
      |          "group.id": "sparta",
      |          "bootstrap.servers": [{
      |            "host": "127.0.0.1",
      |            "port": "9092"
      |          }],
      |          "enable.auto.commit": false,
      |          "value.deserializer": "string",
      |          "kafkaProperties": [{
      |            "kafkaPropertyKey": "",
      |            "kafkaPropertyValue": ""
      |          }],
      |          "storeOffsetInKafka": true
      |        }
      |      },
      |      {
      |        "name": "Print",
      |        "stepType": "Output",
      |        "className": "PrintOutputStep",
      |        "classPrettyName": "Print",
      |        "arity": [],
      |        "writer": {
      |          "saveMode": "Append"
      |        },
      |        "description": "",
      |        "uiConfiguration": {
      |          "position": {
      |            "x": 1207,
      |            "y": 389
      |          }
      |        },
      |        "configuration": {
      |          "printMetadata": false,
      |          "printData": true,
      |          "printSchema": false,
      |          "logLevel": "error"
      |        }
      |      }
      |    ]
      |  }
      |}
    """.stripMargin

  val workflowStatusRaw =
    """{
      |  "id": "wfs1",
      |  "name": "wfTest",
      |  "description": "",
      |  "settings": {},
      |  "version": 0,
      |  "group": "default",
      |  "pipelineGraph": {
      |    "nodes": [{
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
      |          "topics": [{
      |            "topic": "offsetspr"
      |          }],
      |          "auto.offset.reset": "latest",
      |          "partition.assignment.strategy": "range",
      |          "vaultTLSEnable": false,
      |          "offsets": [],
      |          "group.id": "sparta",
      |          "bootstrap.servers": [{
      |            "host": "127.0.0.1",
      |            "port": "9092"
      |          }],
      |          "enable.auto.commit": false,
      |          "value.deserializer": "string",
      |          "kafkaProperties": [{
      |            "kafkaPropertyKey": "",
      |            "kafkaPropertyValue": ""
      |          }],
      |          "storeOffsetInKafka": true
      |        }
      |      },
      |      {
      |        "name": "Print",
      |        "stepType": "Output",
      |        "className": "PrintOutputStep",
      |        "classPrettyName": "Print",
      |        "arity": [],
      |        "writer": {
      |          "saveMode": "Append"
      |        },
      |        "description": "",
      |        "uiConfiguration": {
      |          "position": {
      |            "x": 1207,
      |            "y": 389
      |          }
      |        },
      |        "configuration": {
      |          "printMetadata": false,
      |          "printData": true,
      |          "printSchema": false,
      |          "logLevel": "error"
      |        },
      |        "status": {
      |           "id": "wfs1",
      |           "status": "Launched",
      |           "statusInfo": "Workflow stopped correctly",
      |           "lastExecutionMode": "local",
      |           "lastUpdateDate": "2018-01-17T14:38:39Z"
      |       }
      |      }
      |    ]
      |  }
      |}
    """.stripMargin

  val status=
    """
      |{
      |"id": "wfs1",
      |"status": "Launched",
      |"statusInfo": "Workflow stopped correctly",
      |"lastExecutionMode": "local",
      |"lastUpdateDate": "2018-01-17T14:38:39Z"
      |}
  """.stripMargin

  val newWorkflowRaw =
    """
      |{
      |"id": "wf2",
      |"name": "wf-test2",
      | "version": 0,
      | "group": {
      |   "name" : "/home",
      |   "id" : "940800b2-6d81-44a8-84d9-26913a2faea4" },
      |"description": "",
      |"settings": {},
      |"pipelineGraph": {}
    """.stripMargin

  val groupRaw =
    """
      |{
      |"name" : "/home",
      |"id" : "940800b2-6d81-44a8-84d9-26913a2faea4"
      |}
    """.stripMargin

  before{
    CuratorFactoryHolder.setInstance(curatorFramework)
  }

  "workflowService" must {

    "existByName: returns an option wrapping a workflow with the matching name" in {
      existMock
      val result = workflowService.exists("wf-test", 0L, DefaultGroup.id.get)

      result.get.name shouldBe "wf-test"
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

    "findById with status: should return a workflow with status Launched" in {
      mockFindByIDwithStatus
      val result = workflowService.findById("wfs1")

      result.status.get.status should be eq(WorkflowStatusEnum.Launched)
    }

    "findByGroup: should return a list with the workflows" in {
      mockListOfWorkflows
      mockFindByID

      val result = workflowService.findByGroupID(DefaultGroup.id.get)

      result shouldBe a[Seq[_]]
    }

    "findIdList: should return a list with the workflows" in {
      mockListOfWorkflows
      mockFindByID

      val result = workflowService.findByIdList(Seq("wf1"))

      result shouldBe a[Seq[_]]
    }

    "create: given a certain workflow model a new workflow should be created" in {
      existMock

      mockDefaultGroup

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

    "create: given a certain wrong workflow model one exception should be generated" in {
      an[ServerException] should be thrownBy workflowService.create(wrongWorkflow)
    }

    "update: given a certain wrong workflow model one exception should be generated" in {
      an[ServerException] should be thrownBy workflowService.update(wrongWorkflow)
    }

    "createList: given a certain wrong workflow model one exception should be generated" in {
      an[ServerException] should be thrownBy workflowService.createList(Seq(wrongWorkflow))
    }

    "updateList: given a certain wrong workflow model one exception should be generated" in {
      an[ServerException] should be thrownBy workflowService.updateList(Seq(wrongWorkflow))
    }

    "createList: given a certain workflow list a new workflows should be created" in {
      existMock

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
      existMock

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
      existMock

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

    "create new version: given a certain workflow model a new workflow should be created" in {
      existMock
      mockListOfWorkflows
      mockFindByID

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

    def mockDefaultGroup: OngoingStubbing[Array[Byte]] = {
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.GroupZkPath}/${DefaultGroup.id.get}"))
        .thenReturn(new Stat)

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.GroupZkPath}/${DefaultGroup.id.get}"))
        .thenReturn(groupRaw.getBytes)
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

    def mockFindByIDwithStatus: OngoingStubbing[Array[Byte]] = {
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowsZkPath}/wfs1"))
        .thenReturn(new Stat)

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowsZkPath}/wfs1"))
        .thenReturn(workflowStatusRaw.getBytes)

      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/wfs1"))
        .thenReturn(new Stat)
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/wfs1"))
        .thenReturn(status.getBytes)
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

    def existMock: OngoingStubbing[Array[Byte]]= {
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
