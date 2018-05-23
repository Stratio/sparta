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

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.ExecutionHistoryActor.{QueryAll, QueryByUserId, QueryByWorkflowId}
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.{ExecutionChange, Notification}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.ExecutionHistoryDaoImpl
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionMode, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.history.WorkflowExecutionHistory
import com.stratio.sparta.serving.core.models.workflow._

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class ExecutionHistoryActorTestIT extends TestKit(ActorSystem("ExecutionHistoryActorSpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging {

  private lazy val config = ConfigFactory.load()
  implicit val timeout = Timeout(10 seconds)

  def publishEvent(event: Notification): Unit = system.eventStream.publish(event)

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

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

  val hostConf = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl = s""""jdbc:postgresql://$configHost:5432/postgres?user=postgres""""
      log.info(s"Postgres host from config: $hostUrl")
      s"sparta.postgres.url = $hostUrl\n"
    case Failure(e) =>
      log.info(s"Postgres host from default")
      val hostUrl =s""""jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres""""
      s"sparta.postgres.url = $hostUrl\n"
  }

  val host = ConfigFactory.parseString(hostConf).withFallback(ConfigFactory.parseString("sparta.postgres.executionHistory.table = execution_test"))

  val conf = SpartaConfig.initSpartaPostgresConfig(Some(host))

  trait ExecutionHistoryTrait extends ExecutionHistoryDaoImpl {

    override val profile = PostgresProfile

    import profile.api._

    override val db: profile.api.Database = Database.forConfig("", conf.get)
  }

  "A WorkflowExecutionHistoryActor" should {

    val actor = system.actorOf(ExecutionHistoryActor.props(PostgresProfile, conf.get))

    "Insert new data" in new ExecutionHistoryTrait {

      import profile.api._

      val wk = getWorkflowExecutionModel(false)
      publishEvent(ExecutionChange("/test", wk))
      expectNoMsg()
      db.run(table.filter(_.executionId === wk.id).result).map(_.toList) onSuccess {
        case s: Future[_] => assert(true)
      }
      db.close()
    }

    "Update data" in new ExecutionHistoryTrait {

      import profile.api._

      val wk = getWorkflowExecutionModel(true)
      publishEvent(ExecutionChange("/test", wk))
      expectNoMsg()
      db.run(table.filter(_.workflowId === wk.genericDataExecution.get.workflow.id.get).result).map(_.toList) onSuccess {
        case s: Future[_] => assert(true)
      }
      db.close()
    }

    "Query All" in new ExecutionHistoryTrait {
      actor ! QueryAll()
      expectMsgPF() {
        case future: Future[List[WorkflowExecutionHistory]] => future onSuccess {
          case s: List[WorkflowExecutionHistory] => s.size shouldBe 1
        }
      }
      db.close()
    }

    "Query By WorkflowId" in {
      val wfId = getWorkflowModel(true).id.get
      actor ! QueryByWorkflowId(wfId)
      expectMsgPF() {
        case future: Future[List[WorkflowExecutionHistory]] => future onSuccess {
          case s: List[WorkflowExecutionHistory] => assert(s.head.workflowId == wfId)
        }
      }
    }

    "Query By UserId" in {
      val userId = getWorkflowExecutionModel(false).genericDataExecution.get.userId.get
      actor ! QueryByUserId(userId)
      expectMsgPF() {
        case future: Future[List[WorkflowExecutionHistory]] => future onSuccess {
          case s: List[WorkflowExecutionHistory] => assert(s.head.userId.get == userId)
        }
      }
    }
  }
}
