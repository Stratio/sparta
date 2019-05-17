/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.enumerators.{DeployMode, NodeArityEnum, WorkflowExecutionMode}
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api.{ExistsBuilder, GetDataBuilder}
import org.apache.zookeeper.data.Stat
import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class WorkflowValidationTest extends WordSpec with Matchers with MockitoSugar {

  val nodes = Seq(
    NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
    NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )
  val validPipeGraph = PipelineGraph(nodes, edges)
  val emptyPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
  val settingsModel = Settings(
    GlobalSettings(executionMode = WorkflowExecutionMode.local),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(
      JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false, sparkMesosSecurity = false,
      None, SubmitArguments(), SparkConf(SparkResourcesConf())
    )
  )
  implicit val workflowValidatorService = new WorkflowValidatorService
  val emptyWorkflow = Workflow(
    id = None,
    settings = settingsModel,
    name = "testworkflow",
    description = "whatever",
    pipelineGraph = emptyPipeGraph
  )


  "workflowValidation" must {

    "validate non empty nodes" in {
      val pipeline = PipelineGraph(Seq.empty[NodeGraph], edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe false
    }

    "validate one node" in {
      val pipeline = PipelineGraph(Seq(nodes.head), edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe false
    }

    "validate correct nodes" in {
      val pipeline = PipelineGraph(nodes, edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe true
    }

    "validate non empty edges" in {
      val pipeline = PipelineGraph(nodes, Seq.empty[EdgeGraph])
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe false
    }

    "validate one edge" in {
      val pipeline = PipelineGraph(nodes, Seq(edges.head))
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe true
    }

    "validate correct edges" in {
      val pipeline = PipelineGraph(nodes, edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe true
    }

    "validate all edges exists in nodes: invalid" in {
      val pipeline = PipelineGraph(Seq(nodes.head), edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateEdgesNodesExists

      result.valid shouldBe false
    }

    "validate all edges exists in nodes, valid" in {
      val pipeline = PipelineGraph(nodes, edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateEdgesNodesExists

      result.valid shouldBe true
    }

    "validate an acyclic graph" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateGraphIsAcyclic

      result.valid shouldBe true

    }

    "not validate a graph with a cycle" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "b")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateGraphIsAcyclic

      result.valid shouldBe false
      assert(result.messages.exists(msg => msg.message.contains("cycle")))
    }

    "validate a graph with correct arity" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.UnaryToUnary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("e", "", "", "", Seq(NodeArityEnum.BinaryToNary), WriterGraph()),
        NodeGraph("f", "", "", "", Seq(NodeArityEnum.NullaryToNary, NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("j", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph()),
        NodeGraph("k", "", "", "", Seq(NodeArityEnum.NullaryToUnary), WriterGraph()),
        NodeGraph("l", "", "", "", Seq(NodeArityEnum.UnaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "e"),
        EdgeGraph("f", "e"),
        EdgeGraph("f", "j"),
        EdgeGraph("e", "j"),
        EdgeGraph("k", "l")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe true
    }


    "not validate a graph with invalid arity in input relation" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.UnaryToNullary), WriterGraph()),
        NodeGraph("e", "", "", "", Seq(NodeArityEnum.NullaryToUnary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("b", "d"),
        EdgeGraph("d", "e")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "not validate a graph with invalid arity in output relation" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.UnaryToNullary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NullaryToUnary), WriterGraph()),
        NodeGraph("e", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "e")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "not validate a graph with invalid arity in transform relation" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.BinaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NullaryToUnary), WriterGraph()),
        NodeGraph("e", "", "", "", Seq(NodeArityEnum.UnaryToNullary), WriterGraph()),
        NodeGraph("f", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "e"),
        EdgeGraph("e", "f")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "not validate a graph with invalid arity two relations" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToUnary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "validate a graph containing at least one Input-to-Output path" in {
      val nodes = Seq(
        NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph()),
        NodeGraph("e", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("b", "e"),
        EdgeGraph("c", "d")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateExistenceCorrectPath

      result.valid shouldBe true

    }

    "not validate a graph not containing any Input-to-Output path" in {
      val nodes = Seq(
        NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "Transformation", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "b")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateExistenceCorrectPath

      result.valid shouldBe false
    }

    "validate a graph if the group is correct" in {
      val invalidGroupJSON =
        """
          | {
          |  "id" : "aaaaa",
          |  "name": "/home/test1"
          |  }
        """.stripMargin
      val invalidGroup = Group(Some("aaaaa"), "/home/test1")
      implicit val workflow = emptyWorkflow.copy(group = invalidGroup)
      implicit val graph = GraphHelper.createGraph(workflow)

      val curatorFramework = mock[CuratorFramework]
      val existsBuilder = mock[ExistsBuilder]
      val getDataBuilder = mock[GetDataBuilder]

      CuratorFactoryHolder.setInstance(curatorFramework)

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(invalidGroupJSON.getBytes)

      val result = new WorkflowValidation().validateGroupName(workflow)

      result.valid shouldBe true
    }

    "not validate a graph if the group is not correct" in {
      val invalidGroupJSON =
        """
          | {
          |  "id" : "aaaaa",
          |  "name": "/home//"
          |  }
        """.stripMargin
      val invalidGroup = Group(Some("aaaaa"), "/home//")
      implicit val workflow = emptyWorkflow.copy(group = invalidGroup)
      implicit val graph = GraphHelper.createGraph(workflow)

      val curatorFramework = mock[CuratorFramework]
      val existsBuilder = mock[ExistsBuilder]
      val getDataBuilder = mock[GetDataBuilder]

      CuratorFactoryHolder.setInstance(curatorFramework)

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(invalidGroupJSON.getBytes)

      val result = new WorkflowValidation().validateGroupName(workflow)

      result.valid shouldBe false
    }

    "validate a correct workflow name" in {
      val pipeline = PipelineGraph(nodes, edges)
      implicit val workflow = emptyWorkflow.copy(name = "workflow-correct", pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateName
      result.valid shouldBe true
    }

    "not validate an incorrect workflow name" in {
      val pipeline = PipelineGraph(nodes, edges)
      implicit val workflow = emptyWorkflow.copy(name = "workflow-Incorrect!", pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateName
      result.valid shouldBe false
    }

    "not validate an incorrect deploy mode" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(deployMode = Option(DeployMode.cluster)),
          SparkConf(SparkResourcesConf())
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateDeployMode
      result.valid shouldBe false
    }

    "not validate an incorrect spark cores" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf(coresMax = Option(JsoneyString("1")), executorCores = Option(JsoneyString("3"))))
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateSparkCores
      result.valid shouldBe false
    }

    "not validate an incorrect spark cores with empty value" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf(coresMax = None, executorCores = Option(JsoneyString("3"))))
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateSparkCores
      result.valid shouldBe false
    }

    "not validate an incorrect spark cores with 0 value" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf(coresMax = Option(JsoneyString("0")), executorCores = Option(JsoneyString("3"))))
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateSparkCores
      result.valid shouldBe false
    }

    "not validate an incorrect spark executor cores with empty value" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf(coresMax = Option(JsoneyString("1")), executorCores = None))
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateSparkCores
      result.valid shouldBe false
    }

    "not validate an incorrect spark executor cores with 0 value" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf(coresMax = Option(JsoneyString("1")), executorCores = Option(JsoneyString("0"))))
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateSparkCores
      result.valid shouldBe false
    }

    "not validate an incorrect spark driver cores with empty value" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf(driverCores = None))
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateSparkCores
      result.valid shouldBe false
    }

    "not validate an incorrect spark driver cores with 0 value" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf(driverCores = Option(JsoneyString("0"))))
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateSparkCores
      result.valid shouldBe false
    }

    "not validate an incorrect cube checkpoint settings" in {
      val cubeNodes = Seq(
        NodeGraph("a", "", "CubeTransformStep", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val pipeline = PipelineGraph(cubeNodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(executionMode = WorkflowExecutionMode.marathon),
        StreamingSettings(
          JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"), false)),
        SparkSettings(
          JsoneyString("mesos://zk://master.mesos:2181/mesos"), sparkKerberos = false, sparkDataStoreTls = false,
          sparkMesosSecurity = false, None, SubmitArguments(),
          SparkConf(SparkResourcesConf())
        )
      )
      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateCheckpointCubes
      result.valid shouldBe false
    }

    "validate a graph whose step names are unique" in {
      val nodes = Seq(
        NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph()),
        NodeGraph("e", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("b", "e"),
        EdgeGraph("c", "d")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateDuplicateNames

      result.valid shouldBe true

    }

    "not validate a graph containing duplicate names" in {
      val nodes = Seq(
        NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("e", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "e"),
        EdgeGraph("e", "d")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateDuplicateNames

      result.valid shouldBe false
    }

    "not validate Mesos constraints without a colon" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(WorkflowExecutionMode.marathon, Seq.empty, Seq.empty, Seq.empty, true, Some(JsoneyString("constraint1constraint2")))
      )

      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateMesosConstraints

      result.valid shouldBe false
    }

    "not validate Mesos constraints with more than a colon" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(WorkflowExecutionMode.marathon, Seq.empty, Seq.empty, Seq.empty, true, Some(JsoneyString("constraint1:constraint2:")))
      )

      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateMesosConstraints

      result.valid shouldBe false
    }

    "not validate Mesos constraints with a colon at the beginning or end of the string" in {
      val pipeline = PipelineGraph(nodes, edges)
      val wrongSettingsModel = Settings(
        GlobalSettings(WorkflowExecutionMode.marathon, Seq.empty, Seq.empty, Seq.empty, true, Some(JsoneyString(":constraint1")))
      )

      implicit val workflow = emptyWorkflow.copy(settings = wrongSettingsModel, pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateMesosConstraints

      result.valid shouldBe false
    }
  }
}