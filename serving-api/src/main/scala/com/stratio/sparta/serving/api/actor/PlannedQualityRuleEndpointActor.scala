/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

import scala.util.Try

class PlannedQualityRuleEndpointActor() extends ActionUserAuthorize{

  private val plannedQualityRulePgService = PostgresDaoFactory.plannedQualityRulePgService

  val ResourceType = "QRResult"

  import PlannedQualityRuleEndpointActor._

  def receiveApiActions(action: Any): Any = action match {
    case FindAll(user) => findAll(user)
    case FindById(id, user) => findById(id, user)
    case FindByTaskId(taskId, user) => findByTaskId(taskId, user)
    case DeleteAll(user) => deleteAll(user)
    case DeleteById(id, user) => deleteById(id, user)
    case _ => log.info("Unrecognized message in QualityRulePlannedEndpointActor actor")
  }

  def findAll(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      plannedQualityRulePgService.findAllResults()
    }

  def findById(id: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      plannedQualityRulePgService.findById(id)
    }

  def deleteAll(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      plannedQualityRulePgService.deleteAllPlannedQualityRules
    }

  def deleteById(id: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      plannedQualityRulePgService.deleteById(id.toLong)
    }

  def findByTaskId(taskId: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      plannedQualityRulePgService.findByTaskId(taskId)
    }

}

object PlannedQualityRuleEndpointActor {

  case class FindAll(user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class DeleteById(id: String, user: Option[LoggedUser])

  case class FindByTaskId(taskId: String, user: Option[LoggedUser])

  type PlannedQualityRuleResultResponse = Try[SpartaQualityRule]

  type PlannedQualityRuleResultsResponse = Try[List[SpartaQualityRule]]
}

