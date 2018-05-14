/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit._
import akka.util.Timeout
import com.stratio.sparta.sdk.properties.JsoneyString
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension}
import com.stratio.sparta.serving.core.actor.SchedulerMonitorActor.{RetrieveStatuses, RetrieveWorkflowsEnv, _}
import com.stratio.sparta.serving.core.actor.SchedulerMonitorActorTest._
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.StatusChange
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor.WorkflowRemove
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionMode, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.MarathonAPIUtils
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.{mock,_}

import scala.concurrent.Future
import scala.concurrent.duration._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits._

@RunWith(classOf[JUnitRunner])
class SchedulerMonitorActorTest extends TestKit(ActorSystem("SchedulerActorSpec", SpartaConfig.daemonicAkkaConfig))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with ScalaFutures
  with BeforeAndAfterAll
  with InMemoryCleanup {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val checkerProp = TestProbe()

  implicit val timeout: Timeout = Timeout(15.seconds)

  //We need to override some values of SchedulerMonitorActor to perform our tests,

  trait TestActor {
    self: SchedulerMonitorActor =>

    override lazy val currentInstanceName = Some("sparta-fl")
    override def getInstanceCurator: CuratorFramework = mock[CuratorFramework]
    override lazy val inconsistentStatusCheckerActor: ActorRef = checkerProp.ref
    workflowsRaw = scala.collection.mutable.Map("1234" -> testWorkflow, "5678" -> testWorkflow2)
    workflowsWithEnv = scala.collection.mutable.Map("1234" -> testWorkflow,"5678" -> testWorkflow2)
    statuses = scala.collection.mutable.Map("1234" -> testStatus, "5678" -> testStatus2)

    def getStatuses: Map[String, WorkflowStatus] = statuses.toMap

    def getWorkflowsEnv: Map[String, Workflow] = workflowsWithEnv.toMap

    def receiveTest: Receive = {
      case RetrieveStatuses =>
        sender ! getStatuses
      case RetrieveWorkflowsEnv =>
        sender ! getWorkflowsEnv
    }
  }

  "SchedulerMonitorActorTest" when {

    import SchedulerMonitorActorTest._
    val actor = system.actorOf(Props(new SchedulerMonitorActor() with TestActor {
      override def managementReceive: Receive = {
        super.managementReceive orElse receiveTest
      }
    }))

    "it receives a message TriggerCheck" should {
      "send to the inconsistentStatusCheckerActor a Map <DCOS App ID,Workflow id>" in {
        actor ! TriggerCheck
        checkerProp.expectMsg(CheckConsistency(mapMarathonWorkflowIDs))
      }
    }

    "it receives an event StatusChange" should {
      "mark it as stopped updating its internal state" in {
        actor ! StatusChange("myPath", testStatus.copy(status = WorkflowStatusEnum.Stopped))
        actor ! RetrieveStatuses
        expectMsgPF() { case messageStatuses: Map[String, WorkflowStatus] =>
          messageStatuses.get(testStatus.id).fold(false) {
            status => status.status == WorkflowStatusEnum.Stopped
          }
        }
        actor ! TriggerCheck
        checkerProp.expectMsg(CheckConsistency(Map.empty[String, String]))
      }

      "mark it as running updating its internal state" in {
        actor ! StatusChange("myPath", testStatus)
        actor ! RetrieveStatuses
        expectMsgPF() { case messageStatuses: Map[String, WorkflowStatus] =>
          messageStatuses.get(testStatus.id).fold(false) {
            status => status.status == WorkflowStatusEnum.Started
          }
        }
        actor ! TriggerCheck
        checkerProp.expectMsg(CheckConsistency(mapMarathonWorkflowIDs))
      }
    }

    "it receives an event WorkflowRemove" should {
      "remove the workflow and change its internal state" in {
        actor ! WorkflowRemove("myPath", testWorkflow)
        actor ! RetrieveWorkflowsEnv
        expectMsgPF() { case messageWorkflow: Map[String, Workflow] =>
          messageWorkflow.isEmpty
        }
      }
    }

    "the method CheckDiscrepancy in its utils is invoked" should {
      "retrieve correctly only inconsistent status through MarathonAPI" in {
        val mockMarathon = mock[MarathonAPIUtils](CALLS_REAL_METHODS)
        doReturn(Future(testJSON)).when(mockMarathon).retrieveApps()
        when(mockMarathon.checkDiscrepancy(Map.empty[String, String])).thenCallRealMethod()
        when(mockMarathon.extractWorkflowStatus(testJSON)).thenCallRealMethod()
        whenReady(mockMarathon.checkDiscrepancy(mapMarathonWorkflowIDs)) { res =>
          assert(res._2.isEmpty)
        }
        whenReady(mockMarathon.checkDiscrepancy(Map.empty[String, String])) { res =>
          assert(res._2 === Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0"))
        }
        whenReady(mockMarathon.checkDiscrepancy(
          Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-v0" -> "1234"))) { res =>
          assert(
            res._1 === Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-v0" -> "1234") &&
              res._2 === Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0"))
        }
        val extractApp = mockMarathon.extractWorkflowStatus(testJSON)
        extractApp should be(Some(Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0")))
      }
    }

    "the method fromStatusesToMapWorkflow is invoked" should {
      "retrieve correctly the app names in Marathon" in {
        val objectSchedulerMonitorActor = SchedulerMonitorActor
        val extractApp = objectSchedulerMonitorActor.fromStatusesToMapWorkflowNameAndId(statusesTest,
          workflowsTest)(Some("sparta-fl"))
        extractApp should be(
          Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0" -> "1234"))
      }
    }

    "the method fromDCOSName2NameAndTupleGroupVersion is invoked" should {
      "retrieve correctly the workflow name, group and version" in {
        val objectSchedulerMonitorActor = SchedulerMonitorActor
        val seqTest = Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0",
          "/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v2",
          "/sparta/sparta-fl/workflows/home/aaa/bbb/ccc/test-input-print/test-input-print-v3")
        val extractApp =  objectSchedulerMonitorActor.fromDCOSName2NameAndTupleGroupVersion(seqTest)
        extractApp should be(
          Map("test-input-print" -> ("/home", 0L),
            "test-input-print" -> ("/home", 2L),
            "test-input-print" -> ("/home/aaa/bbb/ccc", 3L)))
      }
    }
  }
}

  object SchedulerMonitorActorTest {

    val nodes = Seq(
      NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
      NodeGraph("b", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
    )
    val edges = Seq(
      EdgeGraph("a", "b")
    )
    val settingsModel = Settings(
      GlobalSettings(WorkflowExecutionMode.marathon, Seq.empty, Seq.empty, true,
        Some(JsoneyString("constraint1:constraint2"))),
      StreamingSettings(
        JsoneyString("6s"), None, None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
      SparkSettings(
        JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false,
        sparkMesosSecurity = false, None, SubmitArguments(), SparkConf(SparkResourcesConf()))
    )

    val timestampEpochTest = 1519051473L
    val pipeline = PipelineGraph(nodes, edges)
    val testWorkflow = Workflow(Option("1234"), "test-input-print",
      settings = settingsModel,
      pipelineGraph = pipeline,
      group = AppConstant.DefaultGroup,
      lastUpdateDate = Option(new DateTime(timestampEpochTest))
    )

    val testWorkflow2 = Workflow(Option("5678"), "test-input",
      settings = settingsModel,
      pipelineGraph = pipeline,
      group = AppConstant.DefaultGroup,
      lastUpdateDate = Option(new DateTime(timestampEpochTest))
    )

    val testStatus = WorkflowStatus("1234", WorkflowStatusEnum.Started,
      lastExecutionMode = Some(WorkflowExecutionMode.marathon))

    val testStatus2 = WorkflowStatus("5678", WorkflowStatusEnum.Failed,
      lastExecutionMode = Some(WorkflowExecutionMode.marathon))

    val statusesTest = scala.collection.mutable.Map("1234" -> testStatus, "5678" -> testStatus2)
    val workflowsTest = scala.collection.mutable.Map("1234" -> testWorkflow, "5678" -> testWorkflow2)


    val testJSON =
      """
        |{
        |  "apps": [{
        |    "id": "/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0",
        |    "cmd": null,
        |    "args": null,
        |    "user": null,
        |    "env": null,
        |    "acceptedResourceRoles": null,
        |    "ipAddress": {
        |      "groups": [],
        |      "labels": {},
        |      "discovery": {
        |        "ports": []
        |      },
        |      "networkName": "stratio"
        |    },
        |    "version": "2018-04-18T13:44:05.003Z",
        |    "residency": null,
        |    "secrets": {
        |      "role": {
        |        "source": "open"
        |      }
        |    },
        |    "taskKillGracePeriodSeconds": null,
        |    "versionInfo": {
        |      "lastScalingAt": "2018-04-18T13:44:05.003Z",
        |      "lastConfigChangeAt": "2018-04-17T11:37:20.496Z"
        |    },
        |    "tasksStaged": 0,
        |    "tasksRunning": 0,
        |    "tasksHealthy": 0,
        |    "tasksUnhealthy": 0,
        |    "deployments": [{
        |      "id": "cfc53c93-2083-4fd1-a5f8-910aebb67e8a"
        |    }]
        |  }]
        |}
      """.stripMargin

    val mapMarathonWorkflowIDs =
      Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0" -> "1234")



    // This trait prompts the cleaning of the journal of the persistent actor before each test
    trait InMemoryCleanup extends BeforeAndAfterEach {
      _: Suite =>

      implicit def system: ActorSystem

      override protected def beforeEach(): Unit = {
        val tp = TestProbe()
        tp.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
        tp.expectMsg(akka.actor.Status.Success(""))
        tp.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
        tp.expectMsg(akka.actor.Status.Success(""))
        super.beforeEach()
      }
    }
  }