/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.wordnik.swagger.annotations.Api
import spray.routing.Route

@Api(value = HttpConstant.StatusHistoryPath, description = "Workflow status history", position = 0)
trait StatusHistoryHttpsService extends BaseHttpService {

  def findByWorkflowId(user: Option[LoggedUser]) = ???
  def findByUser(user: Option[LoggedUser]) = ???


  override def routes(user: Option[LoggedUser] = None): Route =
    findByWorkflowId(user) ~ findByUser(user)
}
