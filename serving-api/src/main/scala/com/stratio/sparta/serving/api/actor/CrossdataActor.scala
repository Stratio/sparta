/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.CatalogActor._
import com.stratio.sparta.serving.api.actor.CrossdataActor._
import com.stratio.sparta.serving.api.actor.remote.DispatcherActor.EnqueueJob
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, LoggedUser}
import com.stratio.sparta.serving.core.models.crossdata.{QueryRequest, TableInfoRequest, TablesRequest}
import com.stratio.sparta.serving.core.utils.AkkaClusterUtils
import org.json4s.native.Serialization.write

class CrossdataActor() extends Actor with SLF4JLogging with SpartaSerializer {

  implicit val actorSystem = context.system

  lazy val catalogDispatcherActor = AkkaClusterUtils.proxyInstanceForName(CatalogDispatcherActorName,  MasterRole)

  override def receive: Receive = {
    case FindAllDatabases(user) => findAllDatabases(user)
    case FindAllTables(user) => findAllTables(user)
    case FindTables(tablesRequest, user) => findTables(tablesRequest, user)
    case DescribeTable(tableInfoRequest, user) => describeTable(tableInfoRequest, user)
    case ExecuteQuery(queryRequest, user) => executeQuery(queryRequest, user)
    case _ => log.info("Unrecognized message in CrossdataActor")
  }

  def findAllDatabases(user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    val job = FindAllDatabasesJob(
      user.map(userInstance => GosecUser(id = userInstance.id, name = userInstance.name, gid = userInstance.gid)),
      true
    )
    catalogDispatcherActor forward EnqueueJob(write(job))
  }

  def findAllTables(user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    val job = FindAllTablesJob(
      user.map(userInstance => GosecUser(id = userInstance.id, name = userInstance.name, gid = userInstance.gid)),
      true
    )
    catalogDispatcherActor forward EnqueueJob(write(job))
  }

  def findTables(tablesRequest: TablesRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    val job = FindTablesJob(
      tablesRequest,
      user.map(userInstance => GosecUser(id = userInstance.id, name = userInstance.name, gid = userInstance.gid)),
      true
    )
    catalogDispatcherActor forward EnqueueJob(write(job))
  }


  def describeTable(tableInfoRequest: TableInfoRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    val job = DescribeTableJob(
      tableInfoRequest,
      user.map(userInstance => GosecUser(id = userInstance.id, name = userInstance.name, gid = userInstance.gid)),
      true
    )
    catalogDispatcherActor forward EnqueueJob(write(job))
  }

  def executeQuery(queryRequest: QueryRequest, user: Option[LoggedUser]): Unit = maybeWithHdfsUgiService {
    val job = ExecuteQueryJob(
      queryRequest,
      user.map(userInstance => GosecUser(id = userInstance.id, name = userInstance.name, gid = userInstance.gid)),
      true
    )
    catalogDispatcherActor forward EnqueueJob(write(job))
  }

}

object CrossdataActor {

  case class FindAllDatabases(user: Option[LoggedUser])

  case class FindAllTables(user: Option[LoggedUser])

  case class FindTables(tablesRequest: TablesRequest, user: Option[LoggedUser])

  case class DescribeTable(tableInfoRequest: TableInfoRequest, user: Option[LoggedUser])

  case class ExecuteQuery(queryRequest: QueryRequest, user: Option[LoggedUser])

}
