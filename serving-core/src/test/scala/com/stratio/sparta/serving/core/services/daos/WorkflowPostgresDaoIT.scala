/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.daos

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.WorkflowDao
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowVersion}
import com.stratio.sparta.serving.core.services.dao.WorkflowPostgresDao
import com.stratio.sparta.serving.core.services.daos.util.WorkflowBuilder
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import com.typesafe.config.Config
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

import scala.concurrent.Await
import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class WorkflowPostgresDaoIT extends DAOConfiguration
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging
  with JdbcSlickHelper
  with ScalaFutures {

  val profile = PostgresProfile

  import profile.api._

  var db1: profile.api.Database = _
  val postgresConf: Config = SpartaConfig.getPostgresConfig().get

  val workflowPostgresDao = new WorkflowPostgresDao()

  trait WorkflowDaoTrait extends WorkflowDao {

    override val profile = PostgresProfile
    override val db: profile.api.Database = db1
  }

  override def beforeAll(): Unit = {
    db1 = Database.forConfig("", properties)

    val actions = DBIO.seq(sqlu"DROP TABLE IF EXISTS spartatest.workflow CASCADE;")
    Await.result(db1.run(actions), queryTimeout millis)

    PostgresFactory.invokeInitializationMethods()
    Thread.sleep(3000)
    PostgresFactory.invokeInitializationDataMethods()
    Thread.sleep(1000)
  }

  "A workflow " must {
    "be created" in new WorkflowDaoTrait {

      val wf: Workflow = WorkflowBuilder.workflow.build

      whenReady(workflowPostgresDao.createWorkflow(wf), timeout(Span(queryTimeout, Milliseconds))) { createdWF =>
        whenReady(workflowPostgresDao.findWorkflowById(wf.id.get), timeout(Span(queryTimeout, Milliseconds))) { returnedWF =>
          returnedWF shouldBe createdWF
        }
      }
    }

    "be created from old version with new version" in new WorkflowDaoTrait {

      val newVersion: Long = 5L
      val wf: Workflow = WorkflowBuilder.workflow.build
      val workflowVersion = WorkflowVersion(wf.id.get, name = None, version = Some(newVersion), None, None)

      whenReady(workflowPostgresDao.createWorkflow(wf), timeout(Span(queryTimeout, Milliseconds))) { createdWF =>
        whenReady(workflowPostgresDao.createVersion(workflowVersion), timeout(Span(queryTimeout, Milliseconds))) { returnedWF =>
          returnedWF.version shouldBe newVersion
          createdWF.version should not be equal(newVersion)
        }
      }
    }

    "be created from old version with a new name" in new WorkflowDaoTrait {

      val newName = "newname"
      val wf: Workflow = WorkflowBuilder.workflow.build
      val workflowVersion = WorkflowVersion(wf.id.get, name = Some(newName), None, None, None)

      whenReady(workflowPostgresDao.createWorkflow(wf), timeout(Span(queryTimeout, Milliseconds))) { createdWF =>
        whenReady(workflowPostgresDao.createVersion(workflowVersion), timeout(Span(queryTimeout, Milliseconds))) { rWF =>
          whenReady(workflowPostgresDao.findWorkflowById(rWF.id.get), timeout(Span(queryTimeout, Milliseconds))) { returnedWF =>
            returnedWF.name shouldBe newName
            createdWF.name should not be newName
            returnedWF.version shouldBe 0
          }
        }
      }
    }
  }

  override def afterAll(): Unit = {

    val actions = DBIO.seq(sqlu"DROP TABLE IF EXISTS spartatest.workflow CASCADE;")
    Await.result(db1.run(actions), queryTimeout millis)

    db1.close()
  }

}