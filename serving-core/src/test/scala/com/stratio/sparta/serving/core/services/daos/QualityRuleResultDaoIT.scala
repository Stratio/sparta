/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.daos

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.QualityRuleResultDao
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionMode, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.{QualityRuleResultPostgresDao, WorkflowExecutionPostgresDao}
import com.stratio.sparta.serving.core.services.daos.util.WorkflowBuilder
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import com.typesafe.config.Config
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class QualityRuleResultDaoIT extends DAOConfiguration
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

  val qrResult1 = QualityRuleResult(
    id = Option("id1"),
    executionId = "exec1",
    dataQualityRuleId = "rule1",
    numTotalEvents = 100,
    numPassedEvents = 40,
    numDiscardedEvents =  60,
    metadataPath = "a/path",
    transformationStepName = "transformationStep",
    outputStepName = "outputStep",
    satisfied = true,
    conditionThreshold = ">= 80 %",
    successfulWriting = true,
    warning = false,
    qualityRuleName = "Dummy quality rule",
    conditionsString = "If column date is not null",
    globalAction = "ACT_PASS"
  )

  val qrResult2 = QualityRuleResult(
    id = Option("id2"),
    executionId = "exec1",
    dataQualityRuleId = "rule2",
    numTotalEvents = 100,
    numPassedEvents = 40,
    numDiscardedEvents =  60,
    metadataPath = "a/path",
    transformationStepName = "transformationStep",
    outputStepName = "outputStep",
    satisfied = true,
    conditionThreshold = ">= 80 %",
    successfulWriting = true,
    warning = false,
    qualityRuleName = "Dummy quality rule",
    conditionsString = "If column date is not null",
    globalAction = "ACT_PASS"
  )

  val qrResult3 = QualityRuleResult(
    id = Option("id3"),
    executionId = "exec2",
    dataQualityRuleId = "rule3",
    numTotalEvents = 100,
    numPassedEvents = 40,
    numDiscardedEvents =  60,
    metadataPath = "a/path",
    transformationStepName = "transformationStep",
    outputStepName = "outputStep",
    satisfied = true,
    conditionThreshold = " field > 50",
    successfulWriting = true,
    sentToApi = true,
    warning = false,
    qualityRuleName = "Dummy quality rule",
    conditionsString = "If column date is not null",
    globalAction = "ACT_PASS"
  )

  val wf: Workflow = WorkflowBuilder.workflow.build

  val testExecution = WorkflowExecution(
    Option("exec1"),
    Seq(ExecutionStatus(WorkflowStatusEnum.Started)),
    GenericDataExecution(wf, WorkflowExecutionMode.marathon,  ExecutionContext()),
    None,
    None,
    None,
    Option(MarathonExecution("sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234")),
    None
  )

  val testExecution2 = WorkflowExecution(
    Option("exec2"),
    Seq(ExecutionStatus(WorkflowStatusEnum.Started)),
    GenericDataExecution(wf, WorkflowExecutionMode.marathon,  ExecutionContext()),
    None,
    None,
    None,
    Option(MarathonExecution("sparta/sparta-fl/workflows/home/test-input-print/test-input-print-v0/1234")),
    None
  )

  val workflowExecutionDao = new WorkflowExecutionPostgresDao()
  val qualityRuleResultDao = new QualityRuleResultPostgresDao()

  trait QualityRuleResultDaoTrait extends QualityRuleResultDao  {
    override val profile = PostgresProfile
    override val db: profile.api.Database = db1
  }

  override def beforeAll(): Unit = {

    db1 = Database.forConfig("", properties)

    val actions = DBIO.seq(sqlu"DROP TABLE IF EXISTS spartatest.quality_rule_result CASCADE;")
    Await.result(db1.run(actions), queryTimeout millis)

    PostgresFactory.invokeInitializationMethods()
    PostgresFactory.invokeInitializationDataMethods()

    workflowExecutionDao.createExecution(testExecution)
    workflowExecutionDao.createExecution(testExecution2)

  }

  "A quality rule result" must {
    "be created" in new QualityRuleResultDaoTrait {

      whenReady(qualityRuleResultDao.createQualityRuleResult(qrResult1), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.filter(_.id === qrResult1.id).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>

          result.head.id shouldBe qrResult1.id


          whenReady(Future.sequence(Seq(
            qualityRuleResultDao.createQualityRuleResult(qrResult2),
            qualityRuleResultDao.createQualityRuleResult(qrResult3)
          )), timeout(Span(queryTimeout, Milliseconds))) { res =>
            res.isEmpty shouldBe false
          }
        }
      }
    }

    "be found and returned if it matches the specified id" in new QualityRuleResultDaoTrait {
      whenReady(qualityRuleResultDao.findById(qrResult1.id.get), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.id shouldBe qrResult1.id
      }
    }

    "be found and returned if it matches the specified execution id" in new QualityRuleResultDaoTrait {
      whenReady(qualityRuleResultDao.findByExecutionId(testExecution.getExecutionId), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.head.executionId shouldBe testExecution.getExecutionId
        result.size shouldBe 2
      }
    }

    "be found and returned" in new QualityRuleResultDaoTrait {
      whenReady(qualityRuleResultDao.findAll(), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.size shouldBe 3
      }
    }

    "be found and returned and its unsent flag must be false" in new QualityRuleResultDaoTrait {
      whenReady(qualityRuleResultDao.findAllUnsent(), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.head.sentToApi shouldBe false
        result.size shouldBe 2
      }
    }

    "be deleted " in new QualityRuleResultDaoTrait {
      whenReady(qualityRuleResultDao.deleteById("id1"), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result shouldBe true
      }
    }

    "be deleted if its execution id matches with the one specified" in new QualityRuleResultDaoTrait {
      whenReady(qualityRuleResultDao.deleteByExecutionId("exec1"), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result shouldBe true
      }
    }

    "be deleted along with the rest of results" in new QualityRuleResultDaoTrait {
      whenReady(qualityRuleResultDao.deleteAllQualityRules(), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result shouldBe true
      }
    }
  }
}
