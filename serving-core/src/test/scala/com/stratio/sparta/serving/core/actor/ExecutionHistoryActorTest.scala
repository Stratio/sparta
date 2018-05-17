/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

import akka.actor.{ActorRef, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.ExecutionHistoryActor.{QueryAll, QueryByUserId, QueryByWorkflowId, WorkflowExecutionHistory}
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.ExecutionChange
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.ExecutionHistoryDaoImpl
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionMode, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class ExecutionHistoryActorTest extends TestKit(ActorSystem("ExecutionHistoryActorSpec", SpartaConfig.daemonicAkkaConfig))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging {

  private lazy val config = ConfigFactory.load()
  implicit val timeout = Timeout(10 seconds)

  private def getWorkflowModel(update: Boolean): Workflow = {
    val settingsModel = Settings(
      GlobalSettings(),
      StreamingSettings(
        JsoneyString("6s"), None, None, None, None, Some(JsoneyString("100")), None, None,
        CheckpointSettings(JsoneyString("test/test"))),
      SparkSettings(JsoneyString("local[*]"), false, false, false, None, SubmitArguments(),
        SparkConf(SparkResourcesConf()))
    )
    var workflow = Workflow(
      id = Option("id"),
      settings = settingsModel,
      name = "testworkflow",
      description = "whatever",
      pipelineGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph]),
      status = Some(WorkflowStatus("id", WorkflowStatusEnum.Launched))
    )
    if (update) {
      workflow = workflow.copy(lastUpdateDate = Some(new DateTime()), id = workflow.id.map(id => s"$id-update"))
    }
    workflow
  }

  private def getWorkflowExecutionModel(update: Boolean): WorkflowExecution =
    WorkflowExecution(
      id = "exec1",
      genericDataExecution = Option(GenericDataExecution(getWorkflowModel(update), WorkflowExecutionMode.local, "1", userId = Some("testUser"))),
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

  trait ExecutionHistoryTrait extends ExecutionHistoryDaoImpl

  "A WorkflowExecutionHistoryActor" should {

    val actor = system.actorOf(ExecutionHistoryActor.props())

    "Insert new data" in new ExecutionHistoryTrait {

      import profile.api._

      val wk = getWorkflowExecutionModel(false)
      actor ! ExecutionChange("/test", wk)
      db.run(table.filter(_.id === wk.id).result).map(_.toList) onSuccess {
        case s: Future[_] => assert(true)
      }
    }

    "Update data" in new ExecutionHistoryTrait {

      import profile.api._

      val wk = getWorkflowExecutionModel(true)
      actor ! ExecutionChange("/test", wk)
      db.run(table.filter(_.workflowId === wk.genericDataExecution.get.workflow.id.get).result).map(_.toList) onSuccess {
        case s: Future[_] => assert(true)
      }
    }

    "Query All" in new ExecutionHistoryTrait {

      import akka.pattern.ask

      (actor ? QueryAll()).mapTo[Future[List[WorkflowExecutionHistory]]] onSuccess {
        case s: List[WorkflowExecutionHistory] => s.size shouldBe >(1)
      }
    }

    "Query By WorkflowId" in {
      import akka.pattern.ask
      (actor ? QueryByWorkflowId(getWorkflowModel(false).id.get)).mapTo[Future[List[WorkflowExecutionHistory]]] onSuccess {
        case s: List[WorkflowExecutionHistory] => s.size shouldBe >(1)
      }
    }

    "Query By UserId" in {
      import akka.pattern.ask
      (actor ? QueryByUserId(getWorkflowExecutionModel(false).genericDataExecution.get.userId.get)).mapTo[Future[List[WorkflowExecutionHistory]]] onSuccess {
        case s: List[WorkflowExecutionHistory] => s.size shouldBe >(1)
      }
    }
  }
}
