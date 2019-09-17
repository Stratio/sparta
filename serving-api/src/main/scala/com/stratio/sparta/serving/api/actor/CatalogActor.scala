/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, Props}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.security.{CatalogResource, Select, View}
import com.stratio.sparta.serving.api.actor.CatalogActor._
import com.stratio.sparta.serving.api.utils.CrossdataUtils
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, LoggedUser}
import com.stratio.sparta.serving.core.models.crossdata.{QueryRequest, TableInfoRequest, TablesRequest}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

class CatalogActor() extends Actor with SLF4JLogging with ActionUserAuthorize {

  val ResourceType: String = CatalogResource.name()

  def receiveApiActions(action : Any): Any = action match {
    case FindAllDatabasesJob(user, _) => findAllDatabases(user)
    case FindAllTablesJob(user, _) => findAllTables(user)
    case FindTablesJob(tablesRequest, user, _) => findTables(tablesRequest, user)
    case DescribeTableJob(tableInfoRequest, user, _) => describeTable(tableInfoRequest, user)
    case ExecuteQueryJob(queryRequest, user, _) => executeQuery(queryRequest, user)
    case _ => log.info("Unrecognized message in Catalog actor")
  }

  def findAllDatabases(user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      CrossdataUtils.listDatabases(user.map(_.id))
        .map(_.map(db => SpartaDatabase(db.name, db.description, db.locationUri)))
    }
  }

  def findAllTables(user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      CrossdataUtils.listAllTables(user.map(_.id))
        .map(_.map(tb => SpartaTable(tb.name, tb.database, tb.description, tb.tableType, tb.isTemporary)))
    }
  }

  def findTables(tablesRequest: TablesRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      CrossdataUtils.listTables(tablesRequest.dbName.notBlank, tablesRequest.temporary, user.map(_.id))
        .map(_.map(tb => SpartaTable(tb.name, tb.database, tb.description, tb.tableType, tb.isTemporary)))
    }
  }

  def describeTable(tableInfoRequest: TableInfoRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> View)) {
      CrossdataUtils.listColumns(tableInfoRequest.tableName, tableInfoRequest.dbName, user.map(_.id))
        .map(_.map(col => SpartaColumn(col.name, col.description, col.dataType, col.nullable, col.isPartition, col.isBucket)))
    }
  }

  def executeQuery(queryRequest: QueryRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    authorizeActions(user, Map(ResourceType -> Select)) {
      CrossdataUtils.executeQuery(queryRequest.query, user.map(_.id))
    }
  }

}

object CatalogActor {

  case class FindAllDatabasesJob(user: Option[GosecUser], findAllDatabases: Boolean)

  case class FindAllTablesJob(user: Option[GosecUser], findAllTables: Boolean)

  case class FindTablesJob(tablesRequest: TablesRequest, user: Option[GosecUser], findTables: Boolean)

  case class DescribeTableJob(tableInfoRequest: TableInfoRequest, user: Option[GosecUser], describeTable: Boolean)

  case class ExecuteQueryJob(queryRequest: QueryRequest, user: Option[GosecUser], executeQuery: Boolean)

  def props: Props = Props[CatalogActor]

}

case class SpartaDatabase(name: String, description: String, locationUri: String)
case class SpartaTable(name: String, database: String, description: String, tableType: String, isTemporary: Boolean)
case class SpartaColumn(name: String, description: String, dataType: String, nullable: Boolean, isPartition: Boolean, isBucket: Boolean)


