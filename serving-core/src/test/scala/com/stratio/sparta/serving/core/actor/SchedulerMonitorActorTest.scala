/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension}
import akka.testkit._
import akka.util.Timeout
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.SchedulerMonitorActor.{RetrieveStatuses, RetrieveWorkflowsEnv, _}
import com.stratio.sparta.serving.core.actor.SchedulerMonitorActorTest._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionMode, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.MarathonAPIUtils
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._

//TODO test more cases
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

    override lazy val inconsistentStatusCheckerActor: ActorRef = checkerProp.ref

    /*def receiveTest: Receive = {

    }*/
  }

  "SchedulerMonitorActorTest" when {

    import SchedulerMonitorActorTest._
    val actor = system.actorOf(Props(new SchedulerMonitorActor() with TestActor))

    "the method CheckDiscrepancy in its utils is invoked" should {
      "retrieve correctly only inconsistent status through MarathonAPI" in {
        val mockMarathon = mock[MarathonAPIUtils](CALLS_REAL_METHODS)
        doReturn(Future(testJSON)).when(mockMarathon).retrieveApps()
        when(mockMarathon.checkDiscrepancy(Map.empty[String, String])).thenCallRealMethod()
        when(mockMarathon.extractWorkflowAppsFromMarathonResponse(testJSON)).thenCallRealMethod()
        whenReady(mockMarathon.checkDiscrepancy(mapMarathonWorkflowIDs)) { res =>
          assert(res._2.isEmpty)
        }
        whenReady(mockMarathon.checkDiscrepancy(Map.empty[String, String])) { res =>
          assert(res._2 === Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234"))
        }
        whenReady(mockMarathon.checkDiscrepancy(
          Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-v0/5678" -> "5678"))) { res =>
          assert(
            res._1 === Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-v0/5678" -> "5678") &&
              res._2 === Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234"))
        }
        val extractApp = mockMarathon.extractWorkflowAppsFromMarathonResponse(testJSON)
        extractApp should be(Some(Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234")))
      }
    }

    "the method fromExecutionsToMapMarathonIdExecutionId is invoked" should {
      "retrieve correctly the app names in Marathon" in {
        val objectSchedulerMonitorActor = SchedulerMonitorActor
        val extractApp = objectSchedulerMonitorActor.fromExecutionsToMapMarathonIdExecutionId(executionsTest)
        extractApp should be(
          Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234" -> "1234"))
      }
    }

    "the method fromDCOSName2ExecutionId is invoked" should {
      "retrieve correctly the workflow name, group and version" in {
        val objectSchedulerMonitorActor = SchedulerMonitorActor
        val seqTest = Seq("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234",
          "/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v2/5678",
          "/sparta/sparta-fl/workflows/home/aaa/bbb/ccc/test-input-print/test-input-print-v3/91011")
        val extractApp =  objectSchedulerMonitorActor.fromDCOSName2ExecutionId(seqTest)
        extractApp should be(
          Seq("1234", "5678", "91011")
        )
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
      GlobalSettings(WorkflowExecutionMode.marathon, Seq.empty, Seq.empty, Seq.empty, true,
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


    val exContext = ExecutionContext()

    val testExecution = WorkflowExecution(
      Option("1234"),
      Seq(ExecutionStatus(WorkflowStatusEnum.Started)),
      GenericDataExecution(testWorkflow, WorkflowExecutionMode.marathon, exContext),
      None,
      None,
      None,
      Option(MarathonExecution("sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234")),
      None
    )

    val testExecution2 = WorkflowExecution(
      Option("5678"),
      Seq(ExecutionStatus(WorkflowStatusEnum.Started)),
      GenericDataExecution(testWorkflow, WorkflowExecutionMode.local, exContext),
      None,
      None,
      None,
      None,
      None
    )

    val executionsTest = Seq(testExecution, testExecution2)

    val testJSON =
      """
        |{
        |  "apps": [{
        |    "id": "/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234",
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
      Map("/sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234" -> "1234")



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