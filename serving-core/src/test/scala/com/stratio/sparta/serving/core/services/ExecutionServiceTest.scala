/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import java.util

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionMode}
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.zookeeper.data.Stat
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class ExecutionServiceTest extends WordSpecLike
  with BeforeAndAfter
  with MockitoSugar
  with Matchers {


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
  val pipeGraph = PipelineGraph(nodes, edges)
  val workflowService = new WorkflowService(curatorFramework)
  val workflowID = "wf1"
  val newWorkflowID = "wf2"
  val testWorkflow = Workflow(Option(workflowID), "wf-test", "", settings, pipeGraph)

  val executionService = new ExecutionService(curatorFramework)
  val executionID = "exec1"
  val exec = WorkflowExecution(
    id = "exec1",
    genericDataExecution = Option(GenericDataExecution(testWorkflow, testWorkflow, WorkflowExecutionMode.local, "1", ExecutionContext(true))),
    sparkSubmitExecution = Option(SparkSubmitExecution(
      driverClass = "driver",
      driverFile = "file",
      pluginFiles = Seq(),
      master = "master",
      submitArguments = Map(),
      sparkConfigurations = Map(),
      driverArguments = Map(),
      sparkHome = "sparkHome"
    ))
  )


  val executionRaw =
    """
      |{
      |"id": "exec1",
      |"uniqueId" : "uniqueId",
      |"genericDataExecution": {
      | "workflow": {
      |  "id": "wf1",
      |  "name": "wf-test",
      |  "description": "",
      |  "settings": {},
      |  "version": 0,
      |  "group": {
      |   "name" : "/home",
      |   "id" : "940800b2-6d81-44a8-84d9-26913a2faea4" },
      |  "pipelineGraph": {
      |    "nodes": [
      |     {
      |        "name": "a",
      |        "stepType": "Input",
      |        "className": "",
      |        "classPrettyName": "",
      |        "arity": ["NullaryToNary"],
      |        "executionEngine": "Streaming",
      |        "writer": {
      |          "autoCalculatedFields": [],
      |          "saveMode": "Append"
      |        },
      |        "configuration": {}
      |      },
      |      {
      |        "name": "b",
      |        "stepType": "Output",
      |        "className": "",
      |        "classPrettyName": "",
      |        "executionEngine": "Streaming",
      |        "arity": ["NaryToNullary"],
      |        "writer": {
      |          "saveMode": "Append"
      |        },
      |        "configuration": {}
      |      }
      |    ],
      |    "edges": [
      |        {
      |          "origin": "a",
      |          "destination": "b"
      |        }
      |      ]
      |  }
      | },
      | "executionContext":{
      |   "withEnvironment": true
      | },
      | "workflowRaw": {
      |  "id": "wf1",
      |  "name": "wf-test",
      |  "description": "",
      |  "settings": {},
      |  "version": 0,
      |  "group": {
      |   "name" : "/home",
      |   "id" : "940800b2-6d81-44a8-84d9-26913a2faea4" },
      |  "pipelineGraph": {
      |    "nodes": [
      |     {
      |        "name": "a",
      |        "stepType": "Input",
      |        "className": "",
      |        "classPrettyName": "",
      |        "arity": ["NullaryToNary"],
      |        "executionEngine": "Streaming",
      |        "writer": {
      |          "autoCalculatedFields": [],
      |          "saveMode": "Append"
      |        },
      |        "configuration": {}
      |      },
      |      {
      |        "name": "b",
      |        "stepType": "Output",
      |        "className": "",
      |        "classPrettyName": "",
      |        "executionEngine": "Streaming",
      |        "arity": ["NaryToNullary"],
      |        "writer": {
      |          "saveMode": "Append"
      |        },
      |        "configuration": {}
      |      }
      |    ],
      |    "edges": [
      |        {
      |          "origin": "a",
      |          "destination": "b"
      |        }
      |      ]
      |  }
      | },
      | "executionMode": "local",
      | "executionId": "1"
      |},
      |"sparkSubmitExecution": {
      |"driverClass": "driver",
      |"driverFile": "file",
      |"master": "master",
      |"submitArguments": {},
      |"sparkConfigurations": {},
      |"driverArguments": {},
      |"sparkHome": "sparkHome"
      |}
      |}
    """.stripMargin


  before {
    CuratorFactoryHolder.setInstance(curatorFramework)
  }

  "executionService" must {
    "findById: returns success wrapping a workflowExecution with the matching ID" in {
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(new Stat)

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(executionRaw.getBytes)

      val result = executionService.findById(executionID)

      result shouldBe Success(exec)
    }

    "findById: returns a failure when there's no execution matching the ID" in {
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        // scalastyle:off null
        .thenReturn(null)

      val result = executionService.findById(executionID)

      result.isFailure shouldEqual true
    }

    "findAll: returns all the available executions" in {
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}"))
        .thenReturn(new Stat)

      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}"))
        .thenReturn(new util.ArrayList[String]() {
          add("execution1")
        })

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/execution1"))
        .thenReturn(executionRaw.getBytes)

      val result = executionService.findAll()

      result shouldBe Success(Seq(exec))
    }

    "create: returns success with the execution workflow" in {
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        // scalastyle:off null
        .thenReturn(null)
      // scalastyle:on null
      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/newExec"))
        .thenReturn(executionRaw)

      val result = executionService.create(exec)
      result shouldBe Success(exec)
    }

    "update: if the path exists it updates the execution in said path instead of creating it" in {
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        // scalastyle:off null
        .thenReturn(new Stat)
      // scalastyle:on null
      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(new Stat)

      val result = executionService.create(exec)
      result shouldBe Success(exec)
    }


    "update: checks if a execution with a given id exists and updates it" in {
      // scalastyle:off null
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(new Stat)
      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(new Stat)

      val result = executionService.update(exec)
      result shouldBe Success(exec)
    }

    "create: checks if an execution with a given id exists if not it creates it" in {
      // scalastyle:off null
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(null)

      when(curatorFramework.setData()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(new Stat)

      val result = executionService.update(exec)
      result shouldBe Success(exec)
    }

    "delete: removes the execution and returns and empty Success" in {
      // scalastyle:off null
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(new Stat)
      when(curatorFramework.delete)
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$executionID"))
        .thenReturn(null)


      val result = executionService.delete(executionID)
      result shouldBe Success(())
    }

    "deleteAll: deletes all the execution in a given path" in {
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}"))
        .thenReturn(new Stat)

      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}"))
        .thenReturn(new util.ArrayList[String]() {
          add("execution1")
        })

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/execution1"))
        .thenReturn(executionRaw.getBytes)

      when(curatorFramework.delete)
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete
        .forPath(s"${AppConstant.WorkflowExecutionsZkPath}/execution1"))
        .thenReturn(null)

      val result = executionService.deleteAll()
      result shouldBe Success()
    }
  }
}
