/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor


import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.serving.api.actor.StatusHistoryActor.FindByWorkflowId
import com.stratio.sparta.serving.api.dao.StatusHistoryDaoImpl
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{Notification, StatusChange}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.history.WorkflowStatusHistoryDto
import com.stratio.sparta.serving.core.models.workflow._
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class StatusHistoryActorTestIT extends TestKit(ActorSystem("StatusHistoryActorSpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging {

  implicit val timeout = Timeout(10 seconds)
  private lazy val config = ConfigFactory.load()
  implicit val secManager = None

  def publishEvent(event: Notification): Unit = system.eventStream.publish(event)

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  val host: String = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl = s""""jdbc:postgresql://$configHost:5432/postgres?user=postgres""""
      log.info(s"Postgres host from config: $hostUrl")
      s"sparta.postgres.url = $hostUrl\n"
    case Failure(e) =>
      log.info(s"Postgres host from default")
      val hostUrl =s""""jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres""""
      s"sparta.postgres.url = $hostUrl\n"
  }
  val hostConf = ConfigFactory.parseString(host).withFallback(
    ConfigFactory.parseString("sparta.postgres.statusHistory.table = status_test"))
    .withFallback(ConfigFactory.parseString("sparta.postgres.historyEnabled = true"))

  val conf = SpartaConfig.initSpartaPostgresConfig(Some(hostConf))

  trait StatusHistoryTrait extends StatusHistoryDaoImpl {

    val profile = PostgresProfile

    import profile.api._

    override val db: profile.api.Database = Database.forConfig("", conf.get)
  }

  "A WorkflowStatusHistoryActor" should {
    system.actorOf(Props(new StatusHistoryListenerActor(PostgresProfile, conf.get)))

    val actor = system.actorOf(Props(new StatusHistoryActor))

    val status1 = WorkflowStatus("existingID", WorkflowStatusEnum.Launched, Some("statusId1"))
    val status2 = WorkflowStatus("existingID", WorkflowStatusEnum.Stopped, Some("statusId1"))
    val status3 = WorkflowStatus("existingID", WorkflowStatusEnum.Stopped, Some("statusId2"))


    "Insert new data" in new StatusHistoryTrait {

      import profile.api._

      publishEvent(StatusChange("test", status1))
      expectNoMsg()

      db.run(statusHistoryTable.filter(_.statusId === status1.statusId).result).map(_.toList) onSuccess {
        case s: Future[_] => assert(true)
      }
      db.close()
    }

    "Update data" in new StatusHistoryTrait {

      import profile.api._

      publishEvent(StatusChange("test", status2))
      expectNoMsg()

      db.run(statusHistoryTable.filter(_.statusId === status2.statusId).result).map(_.toList) onSuccess {
        case s: Future[_] => assert(true)
      }
      db.close()
    }


    "Find by workflowId" in new StatusHistoryTrait {

      val workflowId: String = status1.id

      actor ! FindByWorkflowId(workflowId, None)
      expectMsgPF() {
        case response: Either[Success[Future[Seq[WorkflowStatusHistoryDto]]], _] => {
          response match {
            case Left((Success(result))) => {
              result onSuccess {
                case s: List[WorkflowStatusHistoryDto] => assert(s.head.workflowId == workflowId)
              }
            }
            case Right(_) => assert(false)
          }
        }
      }
    }
  }
}