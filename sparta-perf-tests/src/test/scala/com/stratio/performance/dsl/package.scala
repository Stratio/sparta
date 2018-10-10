/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.performance

import com.stratio.sparta.serving.core.models.SpartaSerializer
import org.json4s.native.Serialization.write

package object dsl extends SpartaSerializer{

  implicit class ToJson(workflowBuilder: WorkflowBuilder){
    def toJson = write(workflowBuilder.baseWorkflow)
  }

  def workflow: WorkflowBuilder = WorkflowBuilder.workflow
}