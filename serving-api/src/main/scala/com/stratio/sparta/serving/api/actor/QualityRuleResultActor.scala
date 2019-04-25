/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.QualityRuleResultActor._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

import scala.util.Try

class QualityRuleResultActor(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends ActionUserAuthorize{

  private val qualityRuleResult = PostgresDaoFactory.qualityRuleResultPgService

  val ResourceType = "QRResult"

  def receiveApiActions(action: Any): Any = action match {
    case FindAll(user) => findAll(user)
    case FindById(id, user) => findById(id, user)
    case FindByExecutionId(execId, user) => findByExecutionId(execId, user)
    case FindAllUnsent(user) => findAllUnsent(user)
    case CreateQualityRuleResult(result, user) => createQualityRuleResult(result, user)
    case DeleteAll(user) => deleteAll(user)
    case DeleteById(id, user) => deleteById(id, user)
    case DeleteByExecutionId(execId, user) => deleteByExecutionId(execId, user)
    case _ => log.info("Unrecognized message in QualityRuleResult actor")
  }

  def findAll(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      qualityRuleResult.findAllResults()
    }

  def findById(id: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      qualityRuleResult.findById(id)
    }

  def findByExecutionId(execId: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      qualityRuleResult.findByExecutionId(execId)
    }

  def findAllUnsent(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      qualityRuleResult.findAllUnsent()
    }

  def createQualityRuleResult(result: QualityRuleResult, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Create)) {
      qualityRuleResult.createQualityRuleResult(result)
    }

  def deleteAll(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Delete)) {
      qualityRuleResult.deleteAllQualityRules()
    }

  def deleteById(id: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Delete)) {
      qualityRuleResult.deleteById(id)
    }

  def deleteByExecutionId(execId: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Delete)) {
      qualityRuleResult.deleteByExecutionId(execId)
    }
}

object QualityRuleResultActor {

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class FindByExecutionId(execId: String, user: Option[LoggedUser])

  case class FindAllUnsent(user: Option[LoggedUser])

  case class CreateQualityRuleResult(result: QualityRuleResult, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class DeleteById(id: String, user: Option[LoggedUser])

  case class DeleteByExecutionId(execId: String, user: Option[LoggedUser])

  type QualityRuleResultResponse = Try[QualityRuleResult]

  type QualityRuleResultsResponse = Try[List[QualityRuleResult]]
}