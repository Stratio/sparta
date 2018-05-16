/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers}

import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.ExecutionHistoryActor.{QueryAll, QueryByUserId, QueryByWorkflowId, WorkflowExecutionHistory}
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.ExecutionChange
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionMode, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.TemporalSparkContext
import scala.concurrent.ExecutionContext.Implicits.global

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class ExecutionHistoryActorTest extends TestKit(ActorSystem("FragmentActorSpec", SpartaConfig.daemonicAkkaConfig))
  with TemporalSparkContext
  with ImplicitSender
  with SLF4JLogging
  with Matchers
  with BeforeAndAfterAll {

  private lazy val config = ConfigFactory.load()

  val host = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl = s"jdbc:postgresql://$configHost:5432/postgres?user=postgres"
      log.info(s"Postgres host from config: $hostUrl")
      hostUrl
    case Failure(e) =>
      log.info(s"Postgres host from default")
      "jdbc:postgresql://172.17.0.3:5432/postgres?user=postgres&password=1234"
  }

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

  trait ExecutionHistoryTrait {

    val table = "workflow_execution_history"

    lazy val xdSession = sparkSession
  }

  "A WorkflowExecutionHistoryActor" should {

    val actor = system.actorOf(ExecutionHistoryActor.props())

    "Create new table on prestart" in new ExecutionHistoryTrait {
      xdSession.sql(s"CREATE TABLE $table USING org.apache.spark.sql.jdbc OPTIONS (url '$host', dbtable '$table', driver 'org.postgresql.Driver')")
      expectNoMsg()
    }

    "Insert new data" in new ExecutionHistoryTrait {
      actor ! ExecutionChange("/test", getWorkflowExecutionModel(false))
      xdSession.sql(s"SELECT * FROM $table").collect().foreach(row =>
        row.getAs[String]("workflow_id") shouldBe "id")
    }

    "Update data" in new ExecutionHistoryTrait {
      actor ! ExecutionChange("/test", getWorkflowExecutionModel(true))
      xdSession.sql(s"REFRESH table $table")
      xdSession.sql(s"SELECT * FROM $table").collect().foreach(row =>
        row.getString(row.fieldIndex("workflow_id")) shouldBe "id-update")
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
