/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

import akka.actor.{Actor, Address, PoisonPill}
import akka.cluster.{Cluster, Member}
import akka.event.slf4j.SLF4JLogging

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.utils.SpartaClusterUtils

class SpartaClusterWorkerActor(member: Member) extends Actor with SpartaClusterUtils with SLF4JLogging {

  val cluster = Cluster(context.system)
  val interval = SpartaConfig.getZookeeperConfig().get.getLong("retryInterval")

  implicit val ec: ExecutionContext = context.system.dispatchers.lookup("clusterSparta.cluster-io-dispatcher")

  override def preStart(): Unit = {
    log.info(s"Worker Actor schedule remove zkNode task every $interval millis")
    context.system.scheduler.schedule(0 milli, interval milli)({
      self ! ValidateMemberLeft
    })
  }

  override def postStop(): Unit = {
    log.info(s"Worker Actor stopped")
  }

  override def receive: Receive = {
    case ValidateMemberLeft => forceStopZkNode(member.address)
  }

  private def forceStopZkNode(memberAddress: Address) = {
    Try {
      if (findSeedNodes.exists(node => node.id == memberAddress.hashCode.toString && node.host == memberAddress.host.getOrElse(""))) {
        if(isThisNodeClusterLeader(cluster)) {
          log.info(s"Zk seedNode to remove ${memberAddress.toString}")
          deleteZkNode(memberAddress)
        }
      } else {
        if(isThisNodeClusterLeader(cluster)) {
          log.info(s"Zk seedNode already removed ${memberAddress.toString}, down member from cluster")
          Try(cluster.down(memberAddress))
        }
        self ! PoisonPill
      }
    }.recover { case e => log.error(s"Error deleting zkNode $SeedPath/${memberAddress.hashCode.toString} on force Stop", e) }
  }
}

case object ValidateMemberLeft

case object ValidateUnreachable