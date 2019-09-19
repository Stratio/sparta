/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.daos

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.PlannedQualityRuleDao
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.services.dao.PlannedQualityRulePostgresDao
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import com.typesafe.config.Config
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class PlannedQualityRuleDaoIT extends DAOConfiguration
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

  val defaultThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
    value = 3.0,
    operation = ">=",
    `type` = "abs",
    actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
  )

  val plannedQuery1 = PlannedQuery(
    query = "select count(*) from writepeople where price > 20",
    queryReference = "select from writepeople",
    metadatapathResource = "postgreseos://postgreseos>/:writepeople:",
    resource = "writepeople",
    urlResource = ""
  )

  val plannedQuery2 = PlannedQuery(
    query = "select count(*) from bannedpeople where price > 30",
    queryReference = "select from bannedpeople",
    metadatapathResource = "postgreseos://postgreseos>/:bannedpeople:",
    resource = "bannedpeople",
    urlResource = ""
  )

  val plannedQuery3 = PlannedQuery(
    query = "select count(*) from allowedpeople where price > 20",
    queryReference = "select from allowedpeople",
    metadatapathResource = "postgreseos://postgreseos>/:allowedpeople:",
    resource = "allowedpeople",
    urlResource = ""
  )


  val plannedQR1 = SpartaQualityRule(
    id = 1,
    metadataPath = "postgreseos://postgreseos>/:writepeople:",
    name = "planned quality rule",
    qualityRuleScope = "data",
    logicalOperator = Some("and"),
    enable = true,
    threshold =  defaultThreshold,
    predicates = Seq.empty[SpartaQualityRulePredicate],
    stepName =  "tableA",
    outputName =  "writepeople",
    executionId = None,
    qualityRuleType = Planned,
    plannedQuery = Some(plannedQuery1),
    creationDate = Some(1566378851000L),
    modificationDate = Some(1566378891000L),
    initDate = None,
    period = None,
    sparkResourcesSize = None,
    taskId = None
  )

  val plannedQR2 =  SpartaQualityRule(
    id = 2,
    metadataPath = "postgreseos://postgreseos>/:bannedpeople:",
    name = "planned quality rule",
    qualityRuleScope = "data",
    logicalOperator = Some("and"),
    enable = false,
    threshold =  defaultThreshold,
    predicates = Seq.empty[SpartaQualityRulePredicate],
    stepName =  "tableA",
    outputName =  "bannedpeople",
    executionId = None,
    qualityRuleType = Planned,
    plannedQuery = Some(plannedQuery2),
    creationDate = Some(1566389651000L),
    modificationDate = Some(1566994451000L),
    initDate = None,
    period = None,
    sparkResourcesSize = None,
    taskId = None
  )

  val plannedQR3 =  SpartaQualityRule(
    id = 3,
    metadataPath = "postgreseos://postgreseos>/:allowedpeople:",
    name = "planned quality rule",
    qualityRuleScope = "data",
    logicalOperator = Some("and"),
    enable = true,
    threshold =  defaultThreshold,
    predicates = Seq.empty[SpartaQualityRulePredicate],
    stepName =  "tableA",
    outputName =  "allowedpeople",
    executionId = None,
    qualityRuleType = Planned,
    plannedQuery = Some(plannedQuery3),
    creationDate = Some(1566411251000L),
    modificationDate = Some(1569953651000L),
    initDate = None,
    period = None,
    sparkResourcesSize = None,
    taskId = None
  )

  val plannedQualityRuleDao = new PlannedQualityRulePostgresDao()

  trait PlannedQualityRuleDaoTrait extends PlannedQualityRuleDao  {
    override val profile = PostgresProfile
    override val db: profile.api.Database = db1
  }

  override def beforeAll(): Unit = {

    db1 = Database.forConfig("", properties)

    val actions = DBIO.seq(sqlu"DROP TABLE IF EXISTS spartatest.planned_quality_rule CASCADE;")
    Await.result(db1.run(actions), queryTimeout millis)

    PostgresFactory.invokeInitializationMethods()
    PostgresFactory.invokeInitializationDataMethods()
  }

  "A planned quality rule/s" must{
    "be created" in new PlannedQualityRuleDaoTrait {
      whenReady(plannedQualityRuleDao.createOrUpdate(plannedQR1), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.filter(_.id === plannedQR1.id).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>

          result.head.id shouldBe plannedQR1.id
        }
      }

      plannedQualityRuleDao.createOrUpdate(plannedQR2)
      plannedQualityRuleDao.createOrUpdate(plannedQR3)

    }

    "be found and returned if it matches the specified id" in new PlannedQualityRuleDaoTrait {
      whenReady(plannedQualityRuleDao.findById(plannedQR2.id), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.id shouldBe plannedQR2.id
      }
    }

    "be found if it has its enabled flag set to true" in new PlannedQualityRuleDaoTrait {
      whenReady(plannedQualityRuleDao.findAllActiveQualityRules(), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.size shouldBe 2
      }
    }

    "be found and returned" in new PlannedQualityRuleDaoTrait {
      whenReady(plannedQualityRuleDao.findAllResults(), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.size shouldBe 3
      }
    }

    "be returned with the latest modification time" in new PlannedQualityRuleDaoTrait {
      whenReady(plannedQualityRuleDao.getLatestModificationDate(), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result shouldBe plannedQR3.modificationDate
      }
    }

    "be deleted if its id matches" in new PlannedQualityRuleDaoTrait {
      whenReady(plannedQualityRuleDao.deleteByID(plannedQR1.id), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.size shouldBe 2
        }
      }
    }

    "be deleted" in new PlannedQualityRuleDaoTrait {
       whenReady(plannedQualityRuleDao.deleteAll(), timeout(Span(queryTimeout, Milliseconds))) { _ =>
         whenReady(db.run(table.result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
           result.isEmpty shouldBe true
         }
       }
    }
  }
}