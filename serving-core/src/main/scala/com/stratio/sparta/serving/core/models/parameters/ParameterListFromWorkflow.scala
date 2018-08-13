/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.parameters

import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.workflow.Workflow

case class ParameterListFromWorkflow(
                                      workflow: Workflow,
                                      name: Option[String] = None,
                                      includeEnvironmentVariables : Boolean = true,
                                      tags: Seq[String] = Seq.empty,
                                      description: Option[String] = None
                                    )  extends EntityAuthorization {

  def authorizationId: String = name.getOrElse("AutoCreatedList")

}
