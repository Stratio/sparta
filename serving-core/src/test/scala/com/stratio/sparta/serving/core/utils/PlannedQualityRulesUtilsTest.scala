/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import com.stratio.sparta.serving.core.utils.PlannedQualityRulesUtils._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class PlannedQualityRulesUtilsTest extends WordSpec with Matchers with SpartaSerializer {

  "PlannedQualityRulesUtils" should {

    val workflowValidator = new WorkflowValidatorService()

    "create a parametrized workflow" when {

      "createWorkflowFromScratch is called" in {

        val parametrizedWorkflow = createWorkflowFromScratch
        val validationResult = workflowValidator.validateBasicSettings(parametrizedWorkflow)
        validationResult.valid shouldBe true
      }
    }
  }

}
