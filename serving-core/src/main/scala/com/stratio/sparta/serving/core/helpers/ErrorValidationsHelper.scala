/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.helpers

import com.stratio.sparta.core.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.serving.core.models.workflow.WorkflowValidation

object ErrorValidationsHelper {

  type HasError = Boolean

  def validate(validations: Seq[(HasError, String)], stepName: String): ErrorValidations = {
    (ErrorValidations(valid = true, messages = Seq.empty) /: validations) { case (errValidation, (hasError, validationMessage)) =>
      if (hasError) {
        errValidation.copy(valid = false, messages = errValidation.messages :+ WorkflowValidationMessage(validationMessage, stepName))
      } else {
        errValidation
      }
    }
  }

  //scalastyle:off
  def validateWorkflow(
                        validations: Seq[(HasError, String)],
                        initValidation: Option[WorkflowValidation] = None,
                        stepName: Option[String] = None,
                        subStepName: Option[String] = None
                      ): WorkflowValidation = {
    (initValidation.getOrElse(WorkflowValidation(valid = true, messages = Seq.empty)) /: validations) { case (workflowValidation, (hasError, validationMessage)) =>
      if (hasError) {
        workflowValidation.copy(
          valid = false,
          messages = workflowValidation.messages :+ WorkflowValidationMessage(validationMessage, stepName, subStepName)
        )
      } else workflowValidation
    }
  }

  def errorWhenEmpty(str: String): String = ???

}
