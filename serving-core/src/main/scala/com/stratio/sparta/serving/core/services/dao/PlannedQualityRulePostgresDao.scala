/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.dao

import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.serving.core.dao.PlannedQualityRuleDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import slick.jdbc.PostgresProfile

import scala.concurrent.Future

class PlannedQualityRulePostgresDao extends PlannedQualityRuleDao{

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  def findAllResults(): Future[List[SpartaQualityRule]] = findAll()

  def findAllActiveQualityRules(): Future[List[SpartaQualityRule]] =
    for {
      results <- getAllEnabledQR
    } yield {
      if (results.isEmpty)
        throw new ServerException(s"No active planned quality rules found")
      else
        results
    }

  def findById(id: Long):Future[SpartaQualityRule] = findByIdHead(id)

  def createOrUpdate(plannedQR: SpartaQualityRule): Future[_] = {
    for {
      _ <- upsert(plannedQR)
    } yield {
      log.info(s"The planned quality rule with the following id was successfully created o updated: ${plannedQR.id}")
      true
    }
  }

  def deleteAllPlannedQualityRules(): Future[Boolean] = {
    for {
      results <- findAllResults()
      outcome <- deleteYield(results)
    } yield {
      outcome
    }
  }

  def deleteById(id: Long): Future[Boolean] = {
    for {
      result <- findByIdHead(id)
      outcome <- deleteYield(List(result))
    } yield outcome
  }

  def getLatestModificationDate(): Future[Option[Long]] = {
    for {
     result <- db.run(table.sortBy(_.modificationDate.desc).take(1).result)
    } yield {
      result.headOption.flatMap(_.modificationDate)
    }
  }

  /**
    * PRIVATE METHODS
    */
  private[services] def findByIdHead(id: Long): Future[SpartaQualityRule] = {
    for {
      plannedQualityRules <- db.run(filterById(id).result)
    } yield {
      if(plannedQualityRules.isEmpty)
        throw new ServerException(s"No planned quality rule found with id: $id")
      else
        plannedQualityRules.head
    }
  }

  private[services] def getAllEnabledQR: Future[List[SpartaQualityRule]] = {
    db.run(table.filter(plannedqr => plannedqr.enable === true).result).map(_.toList)
  }

  private[services] def deleteYield(results: List[SpartaQualityRule]): Future[Boolean] = {
    val ids = results.map(_.id)

    for{
      _ <- deleteList(ids)
    } yield {
      log.info(s"The planned quality rules with the following ids were successfully deleted: ${ids.mkString(",")}")
      true
    }
  }
}
