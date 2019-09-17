/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor.remote

import akka.actor.{ActorRef, Props}
import com.stratio.sparta.serving.api.actor.CatalogActor
import com.stratio.sparta.serving.api.actor.CatalogActor._
import com.stratio.sparta.serving.api.actor.remote.CatalogWorkerActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import com.stratio.sparta.serving.core.models.RocketModes
import org.json4s.native.Serialization.read

import scala.concurrent.Future
import scala.util.Try

class CatalogWorkerActor extends WorkerActor {

  val dispatcherActorName = AkkaConstant.CatalogDispatcherActorName
  val rocketMode = RocketModes.Catalog
  val workerTopic = CatalogWorkerTopic

  lazy val catalogActor = context.actorOf(CatalogActor.props)

  override def workerPreStart(): Unit = {
    SparkContextFactory.getOrCreateStandAloneXDSession(None)
  }

  override def executeJob(job: String, jobSender: ActorRef): Future[Unit] = Future {
    Try {
      val findAllDatabasesJob = read[FindAllDatabasesJob](job)
      log.debug(s"Sending FindAllDatabasesJob in worker with sender ${jobSender.path}")
      catalogActor.tell(findAllDatabasesJob, jobSender)
    } recover { case _ =>
      val findAllTablesJob = read[FindAllTablesJob](job)
      log.debug(s"Sending FindAllTablesJob in worker with sender ${jobSender.path}")
      catalogActor.tell(findAllTablesJob, jobSender)
    } recover { case _ =>
      val findTablesJob = read[FindTablesJob](job)
      log.debug(s"Sending FindTablesJob in worker with sender ${jobSender.path}")
      catalogActor.tell(findTablesJob, jobSender)
    } recover { case _ =>
      val describeTableJob = read[DescribeTableJob](job)
      log.debug(s"Sending DescribeTableJob in worker with sender ${jobSender.path}")
      catalogActor.tell(describeTableJob, jobSender)
    } recover { case _ =>
      val executeQueryJob = read[ExecuteQueryJob](job)
      log.debug(s"Sending ExecuteQueryJob in worker with sender ${jobSender.path}")
      catalogActor.tell(executeQueryJob, jobSender)
    } getOrElse log.warn(s"Invalid job message in Catalog worker actor: $job")
  }
}

object CatalogWorkerActor {

  val CatalogWorkerTopic = "catalog-worker"

  def props: Props = Props[CatalogWorkerActor]

}



