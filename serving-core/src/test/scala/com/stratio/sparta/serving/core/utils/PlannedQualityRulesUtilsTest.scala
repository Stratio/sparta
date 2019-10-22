/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum.Reactive
import com.stratio.sparta.core.models._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.HeaderAuthUser
import com.stratio.sparta.serving.core.models.enumerators.ScheduledActionType._
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskState.{apply => _, _}
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskType.{apply => _, _}
import com.stratio.sparta.serving.core.models.orchestrator.ScheduledWorkflowTask
import com.stratio.sparta.serving.core.models.parameters.ParameterVariable
import com.stratio.sparta.serving.core.models.workflow.ExecutionContext
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import com.stratio.sparta.serving.core.utils.PlannedQualityRulesUtils._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class PlannedQualityRulesUtilsTest extends WordSpec with Matchers with SpartaSerializer {

  import PlannedQualityRulesUtilsTest._

  "PlannedQualityRulesUtils" should {

    "create a parametrized workflow" when {

      "createWorkflowFromScratch is called" in {

        val parametrizedWorkflow = createWorkflowFromScratch
        val validationResult = workflowValidator.validateBasicSettings(parametrizedWorkflow)
        validationResult.valid shouldBe true
      }
    }

    "correctly rename the tables inside the query( different name to avoid clashes with already existent crossdata tables)" in {

      val resPostgres: ResourcePlannedQuery = ResourcePlannedQuery(
        id = 1,
        metadataPath = "",
        resource = "tableA",
        typeResource = QualityRuleResourceTypeEnum.JDBC,
        listProperties = Seq(PropertyKeyValue("driver", "postgres"))
      )

      val resHDFS: ResourcePlannedQuery = ResourcePlannedQuery(
        id = 2,
        metadataPath = "",
        resource = "tableB",
        typeResource = QualityRuleResourceTypeEnum.HDFS,
        listProperties = Seq(PropertyKeyValue("driver", "postgres"))
      )


      val complexPlannedQuery: PlannedQuery = PlannedQuery(
        query = "select count(*) from ${1}",
        queryReference = "select count(*) from ${2}",
        resources = Seq(resPostgres, resHDFS)
      )

      val plannedQRTest = defaultQR.copy(plannedQuery = Option(complexPlannedQuery))
      val expectedQuery = s"select count(*) from ${resHDFS.resource}_${resHDFS.id}"

      plannedQRTest.retrieveQueryForInputStepForPlannedQR should equal(expectedQuery)
    }

    "correctly rename the tables inside the query (same name for crossdata)" in {
      val resPostgres: ResourcePlannedQuery = ResourcePlannedQuery(
        id = 1,
        metadataPath = "",
        resource = "tableA",
        typeResource = QualityRuleResourceTypeEnum.JDBC,
        listProperties = Seq(PropertyKeyValue("driver", "postgres"))
      )

      val resXD: ResourcePlannedQuery = ResourcePlannedQuery(
        id = 2,
        metadataPath = "",
        resource = "tableB",
        typeResource = QualityRuleResourceTypeEnum.XD,
        listProperties = Seq(PropertyKeyValue("driver", "postgres"))
      )

      val complexPlannedQuery: PlannedQuery = PlannedQuery(
        query = "select count(*) from ${1}",
        queryReference = "select count(*) from ${2}",
        resources = Seq(resPostgres, resXD)
      )

      val plannedQRTest = defaultQR.copy(plannedQuery = Option(complexPlannedQuery))
      val expectedQuery = s"select count(*) from ${resXD.resource}"

      plannedQRTest.retrieveQueryForInputStepForPlannedQR should equal(expectedQuery)
    }

    "two executionContext should be marked equal" in {
      val ex2 = ExecutionContext(extraParams= Seq(ParameterVariable("a", Some("value_a"))), paramsLists = Seq("a"))

      ex1.equals(ex2) shouldBe true
    }

    "two executionContext should be marked different" in {
      val ex1 = ExecutionContext(extraParams= Seq(ParameterVariable("a", Some("value_a"))), paramsLists = Seq("a"))
      val ex2 = ExecutionContext(extraParams= Seq(ParameterVariable("a", Some("value_a")), ParameterVariable("b", None)), paramsLists = Seq("a"))
      val ex3 = ExecutionContext(extraParams= Seq(ParameterVariable("a", None)), paramsLists = Seq("a"))

      ex1.equals(ex2) shouldBe false
      ex2.equals(ex1) shouldBe false
      ex1.equals(ex3) shouldBe false
    }


    "two ScheduledWorkflowTask should be marked equal" in {
      // Same as ex1 but different object
      val ex2 = ExecutionContext(extraParams= Seq(ParameterVariable("a", Some("value_a"))), paramsLists = Seq("a"))

      val task2 = ScheduledWorkflowTask(
        id = "1234",
        taskType = PERIODICAL,
        actionType = RUN,
        entityId = "WTF",
        executionContext = Some(ex2),
        active = true,
        state = EXECUTED,
        initDate = 0,
        duration = None,
        loggedUser = None)

      task1.equals(task2) shouldBe true
    }

    "two ScheduledWorkflowTask should be marked different" in {
      val ex2 = ExecutionContext(extraParams= Seq(ParameterVariable("a", None)), paramsLists = Seq("a"))

      val task2 = ScheduledWorkflowTask(
        id = "1234",
        taskType = PERIODICAL,
        actionType = RUN,
        entityId = "WTF",
        executionContext = Some(ex2),
        active = true,
        state = EXECUTED,
        initDate = 0,
        duration = None,
        loggedUser = None)

      task1.equals(task2) shouldBe false

      val task3 = task1.copy(initDate = 20)
      task1.equals(task3) shouldBe false

    }
  }
}

object PlannedQualityRulesUtilsTest {

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
    logicalOperator = Some("and"),
    enable = true,
    threshold = defaultThreshold,
    predicates = Seq.empty[SpartaQualityRulePredicate],
    stepName = "tableA",
    outputName = "",
    qualityRuleType = Reactive)

  val workflowValidator = new WorkflowValidatorService()

  val ex1 = ExecutionContext(extraParams= Seq(ParameterVariable("a", Some("value_a"))), paramsLists = Seq("a"))

  val task1 = ScheduledWorkflowTask(
    id = "1234",
    taskType = PERIODICAL,
    actionType = RUN,
    entityId = "WTF",
    executionContext = Some(ex1),
    active = true,
    state = EXECUTED,
    initDate = 0,
    duration = None,
    loggedUser = Some(HeaderAuthUser("Corto Maltese", "Sailor")))

}
