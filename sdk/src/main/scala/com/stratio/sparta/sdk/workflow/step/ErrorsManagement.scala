/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenFieldError.WhenFieldError
import com.stratio.sparta.sdk.workflow.enumerators.{WhenError, WhenFieldError, WhenRowError}
import com.stratio.sparta.sdk.workflow.enumerators.WhenRowError.WhenRowError

case class ErrorsManagement(
                             genericErrorManagement: GenericManagement = GenericManagement(),
                             transformationStepsManagement: TransformationStepManagement = TransformationStepManagement(),
                             transactionsManagement: TransactionsManagement = TransactionsManagement()
                           )

case class GenericManagement(
                              whenError: WhenError.Value = WhenError.Error
                            )

case class TransformationStepManagement(
                                         whenError: WhenError = WhenError.Error,
                                         whenRowError: WhenRowError = WhenRowError.RowError,
                                         whenFieldError: WhenFieldError = WhenFieldError.FieldError
                                       )

case class TransactionsManagement(
                                   sendToOutputs: Seq[ErrorOutputAction] = Seq.empty,
                                   sendStepData: Boolean = false,
                                   sendPredecessorsData: Boolean = false,
                                   sendInputData: Boolean = true
                                 )

case class ErrorOutputAction(
                              outputStepName: String,
                              omitSaveErrors: Boolean = true,
                              addRedirectDate: Boolean = false,
                              redirectDateColName: Option[String] = None
                            )
