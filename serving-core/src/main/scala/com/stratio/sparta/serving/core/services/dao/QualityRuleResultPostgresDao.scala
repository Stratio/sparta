/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.dao.QualityRuleResultDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class QualityRuleResultPostgresDao extends QualityRuleResultDao{

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  def findAllResults(): Future[List[QualityRuleResult]] = findAll()

  def findById(id: String):Future[QualityRuleResult] = findByIdHead(id)

  def findByExecutionId(execId: String): Future[List[QualityRuleResult]] =
    for {
      results <- filterByExecutionId(execId)
    } yield {
      if (results.isEmpty) log.debug(s"No quality rule result found with execution id: $execId")
      results
    }

  def findAllUnsent(): Future[List[QualityRuleResult]] =
    for {
      results <- getAllNonSentToApi
    } yield {
      if (results.isEmpty)
        throw new ServerException(s"No unsent quality rule results found")
      else
        results
    }

  def createQualityRuleResult(result: QualityRuleResult): Future[QualityRuleResult] = {
    val resultWithDateAndId =
      addCreationDate(
        addQualityRuleResultId(result))

    createAndReturn(resultWithDateAndId)
  }

  def deleteAllQualityRules(): Future[Boolean] = {
    for {
      results <- findAllResults()
      outcome <- deleteYield(results)
    } yield outcome
  }

  def deleteById(id: String): Future[Boolean] = {
    for {
      result <- findByIdHead(id)
      outcome <- deleteYield(List(result))
    } yield outcome
  }

  def deleteByExecutionId(execId: String): Future[Boolean] = {
    for {
      results <- filterByExecutionId(execId)
      outcome <- deleteYield(results)
    } yield outcome
  }


  /**
    * PRIVATE METHODS
    */
  private[services] def deleteYield(results: List[QualityRuleResult]): Future[Boolean] = {
    val ids = results.flatMap(_.id.toList)
    for{
      _ <- deleteList(ids)
    } yield {
      log.info(s"The following results were successfully deleted: ${ids.mkString(",")}")
      true
    }

  }

  private[services] def findByIdHead(id: String): Future[QualityRuleResult] = {
    for {
      qualityRuleResults <- db.run(filterById(id).result)
    } yield {
      if(qualityRuleResults.isEmpty)
        throw new ServerException(s"No quality rule result found with id: $id")
      else
        qualityRuleResults.head
    }
  }

  private[services] def filterByExecutionId(execId: String): Future[List[QualityRuleResult]] = {
    db.run(table.filter(_.executionId === execId).result).map(_.toList)
  }

  private[services] def getAllNonSentToApi: Future[List[QualityRuleResult]] = {
    db.run(table.filter(qualityrule => qualityrule.sentToApi === false && qualityrule.successfulWriting === true).result).map(_.toList)
  }

  private[services] def addCreationDate(result: QualityRuleResult): QualityRuleResult = {
    result.creationDate match {
      case None => result.copy(creationDate = getNewCreationDate)
      case Some(_) => result
    }
  }

  private[services] def addQualityRuleResultId(result: QualityRuleResult): QualityRuleResult = {
    result.id match {
      case Some(id) => addId(result, id)
      case None => addId(result, UUID.randomUUID.toString)
    }
  }

  private[services] def getNewCreationDate: Option[DateTime] = Option(new DateTime())

  private[services] def addId(result: QualityRuleResult, newId: String): QualityRuleResult = {
    result.copy(id = Option(newId))
  }
}