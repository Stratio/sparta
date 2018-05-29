/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import com.stratio.sparta.serving.api.actor.StatusHistoryActor._
import com.stratio.sparta.serving.api.dao.StatusHistoryDaoImpl
import com.typesafe.config.Config
import slick.jdbc.JdbcProfile

class StatusHistoryActor(val profileHistory: JdbcProfile, config: Config) extends Actor
  with StatusHistoryDaoImpl{

  val profile = profileHistory

  import profile.api._

  override val db: profile.api.Database = Database.forConfig("", config)


  override def receive: Receive = {
    case FindAll() => sender ! findAll()
    case FindByWorkflowId(id) => sender ! findByWorkflowId(id)
  }
}

//scalastyle:off
object StatusHistoryActor {

  case class FindAll()

  case class FindByWorkflowId(id: String)
}
