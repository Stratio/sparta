/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.security.{Select, SpartaSecurityManager, View}
import com.stratio.sparta.serving.api.actor.CrossdataActor._
import com.stratio.sparta.serving.api.services.CrossdataService
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.models.crossdata.{QueryRequest, TableInfoRequest, TablesRequest}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

class CrossdataActor(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with SLF4JLogging
  with ActionUserAuthorize {

  lazy val crossdataService = new CrossdataService
  val ResourceType = "Catalog"

  def receiveApiActions(action : Any): Any = action match {
    case FindAllDatabases(user) => findAllDatabases(user)
    case FindAllTables(user) => findAllTables(user)
    case FindTables(tablesRequest, user) => findTables(tablesRequest, user)
    case DescribeTable(tableInfoRequest, user) => describeTable(tableInfoRequest, user)
    case ExecuteQuery(queryRequest, user) => executeQuery(queryRequest, user)
    case _ => log.info("Unrecognized message in CrossdataActor")
  }

  def findAllDatabases(user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      crossdataService.listDatabases(user.map(_.id))
    }
  }

  def findAllTables(user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      crossdataService.listAllTables(user.map(_.id))
    }
  }

  def findTables(tablesRequest: TablesRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      crossdataService.listTables(tablesRequest.dbName.notBlank, tablesRequest.temporary, user.map(_.id))
    }
  }


  def describeTable(tableInfoRequest: TableInfoRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      crossdataService.listColumns(tableInfoRequest.tableName, tableInfoRequest.dbName, user.map(_.id))
    }
  }

  def executeQuery(queryRequest: QueryRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> Select)) {
      crossdataService.executeQuery(queryRequest.query, user.map(_.id))
    }
  }

}

object CrossdataActor {

  case class FindAllDatabases(user: Option[LoggedUser])

  case class FindAllTables(user: Option[LoggedUser])

  case class FindTables(tablesRequest: TablesRequest, user: Option[LoggedUser])

  case class DescribeTable(tableInfoRequest: TableInfoRequest, user: Option[LoggedUser])

  case class ExecuteQuery(queryRequest: QueryRequest, user: Option[LoggedUser])

}
