/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum._
import com.stratio.sparta.core.models.qualityrule.SparkQualityRuleThreshold
import com.stratio.sparta.core.models._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class SpartaQualityRulesTest extends WordSpec with Matchers {

  val defaultThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
    value = 3.0,
    operation = ">=",
    `type` = "abs",
    actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
  )

  val defaultQR: SpartaQualityRule = SpartaQualityRule(id = 1,
    metadataPath = "blabla1",
    name = "",
    qualityRuleScope = "data",
    logicalOperator = None,
    enable = true,
    threshold = defaultThreshold,
    predicates = Seq.empty[SpartaQualityRulePredicate],
    stepName = "tableA",
    outputName = "",
    qualityRuleType = Reactive,
    creationDate = Some(0L),
    modificationDate = Some(0L))

  val seqPredicatesTest = Seq(SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("1"), field = "section_id", operation = "="))

  val qrPlannedQuery = Some(PlannedQuery(
      query = s"select count(*) from testTable WHERE price > 10.0 AND PRICE < 13.5",
      queryReference = "",
      metadatapathResource= "",
      resource = "testTable",
      urlResource = ""))

  "A PLANNED quality rule" should {

    val defaultPlannedQR: SpartaQualityRule = defaultQR.copy(qualityRuleType = Planned, initDate = Some(0), period = Some(100), sparkResourcesSize = Some("S"))

    "be considered VALID" when {
      "is simple but has all the scheduling info" in {
        val testQR: SpartaQualityRule = defaultPlannedQR.copy(logicalOperator = Some("OR"), predicates = seqPredicatesTest)
        testQR.validSpartaQR shouldBe true
      }

      "is complex but has all the scheduling info" in{
        val testQR = defaultPlannedQR.copy(plannedQuery = qrPlannedQuery)
        testQR.validSpartaQR shouldBe true
      }
    }

    "be considered INVALID" when {
      "is simple but has NOT all the scheduling info" in {
        val testQR: SpartaQualityRule = defaultPlannedQR.copy(logicalOperator = Some("OR"), predicates = seqPredicatesTest, period = None)
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("scheduling"))
      }

      "is complex but has NOT all the scheduling info" in {
        val testQR = defaultPlannedQR.copy(plannedQuery = qrPlannedQuery, period = None)
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("scheduling"))
      }

      "is complex but the resource is empty" in {
        val testQR = defaultPlannedQR.copy(plannedQuery = Some(qrPlannedQuery.get.copy(resource = "")))
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("resource"))
      }

      "is complex but the query is empty" in {
        val testQR = defaultPlannedQR.copy(plannedQuery = Some(qrPlannedQuery.get.copy(query = "")))
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("empty"))
      }

      "is complex but the query is Invalid because it has not a count(*)" in {
        val testQR = defaultPlannedQR.copy(plannedQuery = Some(qrPlannedQuery.get.copy(query = "Select * from testTable")))
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("count(*)"))
      }

      "is complex but the query is Invalid because it does not contain the test table" in {
        val testQR = defaultPlannedQR.copy(plannedQuery = Some(qrPlannedQuery.get.copy(query = "Select count(*) from testTableAAAAA")))
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("resource"))
      }
    }
  }

  "A REACTIVE quality rule" should {
    val defaultProactiveQR: SpartaQualityRule = defaultQR.copy(qualityRuleType = Reactive)
    "be considered VALID" when {
      "is simple and has no scheduling info" in {
        val testQR = defaultProactiveQR.copy(logicalOperator = Some("AND"), predicates = seqPredicatesTest)
        testQR.validSpartaQR shouldBe true
      }
    }
    "be considered INVALID" when {
      "is advanced (has a query)" in {
        val testQR = defaultProactiveQR.copy(plannedQuery = qrPlannedQuery)
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("An advanced quality rule cannot be Proactive"))
      }
    }
  }


  "A quality rule" should {
    "be considered INVALID" when {
      "it has both predicates and query" in {
        val testQR = defaultQR.copy(logicalOperator = Some("AND"), predicates = seqPredicatesTest, plannedQuery = qrPlannedQuery)
        val caughtException = intercept[RuntimeException] {testQR.validSpartaQR}
        assert(caughtException.getLocalizedMessage.contains("both predicates and an advanced query"))
      }
    }
  }

  "A quality rule threshold" should {

    val defaultThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
      value = 89.86,
      operation = ">=",
      `type` = "%",
      actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
    )

    "be considered NOT satisfied" when {
      "after the two decimal rounding" in {
        val check = new SparkQualityRuleThreshold(defaultThreshold, rowsSatisfyingQR = 89859, totalRows = 100000)
        check.truncateTwoDecimalPositions(89859.toDouble/100000) shouldEqual 89.85
        check.isThresholdSatisfied shouldBe false
      }
    }
    "be considered satisfied" when {
      "after the two decimal rounding" in {
        val check = new SparkQualityRuleThreshold(defaultThreshold, rowsSatisfyingQR = 89860, totalRows = 100000)
        check.truncateTwoDecimalPositions(89860.toDouble/100000) shouldEqual 89.86
        check.isThresholdSatisfied shouldBe true
      }
    }
  }
}