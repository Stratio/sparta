/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.control.Breaks

import akka.actor.{Actor, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.event.slf4j.SLF4JLogging
import org.apache.curator.framework.recipes.leader.LeaderLatch
import org.apache.zookeeper.CreateMode
import org.json4s.jackson.Serialization._

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.utils.{SeedNodeAddress, SpartaClusterUtils}

//scalastyle:off
class SpartaClusterNodeActor extends Actor with SpartaClusterUtils with SLF4JLogging {

  val cluster = Cluster(context.system)

  lazy val latchTimeout = SpartaConfig.getZookeeperConfig().get.getLong("connectionTimeout")
  lazy val latchAttempts = SpartaConfig.getZookeeperConfig().get.getLong("retryAttempts")

  lazy val seedNode = createSeedNode(cluster.selfAddress)

  override def preStart(): Unit = {
    Try {
      log.debug(s"Executing Zookeeper Latch in the sparta cluster initialization")
      val latch = new LeaderLatch(curatorFramework, SeedPath)
      latch.start()
      log.debug(s"Started Zookeeper Latch in the sparta cluster initialization")
      var count = 1
      Breaks.breakable {
        while (count <= latchAttempts) {
          if (latch.await(latchTimeout, TimeUnit.MILLISECONDS)) {
            log.info(s"Acquired Zookeeper Latch in the sparta cluster initialization")
            val currentSeeds = findSeedNodes()
            currentSeeds.foreach(seed => log.info(s"Existing seed node = ${seed.toString}"))
            if (!currentSeeds.exists(_.description == seedNode.description)) {
              curatorFramework.create.creatingParentsIfNeeded.withProtection.withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(s"$SeedPath/${seedNode.id}", write(seedNode).getBytes)
              log.info(s"Seed node created as ephemeral node ${seedNode.toString}")
            }
            val seeds = currentSeeds.map(s => Address(s.protocol, s.system, Option(s.host), Option(s.port))) :+ cluster.selfAddress
            cluster.joinSeedNodes(seeds.distinct.asJava)
            cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[LeaderChanged], classOf[MemberEvent], classOf[MemberLeft], classOf[MemberJoined], classOf[UnreachableMember])
            Breaks.break
          } else {
            count += 1
            log.info(s"Retry $count Zookeeper Latch acquisition")
          }
        }
        if (count == latchAttempts) throw new ServerException("Timeout acquiring subscription leadership")
      }
      latch.close()
      log.debug(s"Close Zookeeper Latch in the sparta cluster initialization")
    }
  }

  override def receive: Receive = {
    case MemberJoined(member) =>
      log.info(s"Member Joined ${member.address}")
    case MemberUp(member) ⇒
      log.info(s"Member Up, welcome to SpartaCluster ${member.address} ")
      if (isThisNodeClusterLeader(cluster)) forceUpsertZkNode(member.address)
    case UnreachableMember(member) ⇒
      log.info(s"Member detected as unreachable ${member.address}")
      context.actorOf(Props(new SpartaClusterWorkerActor(member)))
    case MemberLeft(member) ⇒
      log.info(s"Member left cluster ${member.address}")
      context.actorOf(Props(new SpartaClusterWorkerActor(member)))
    case MemberExited(member) ⇒
      log.info(s"Member exit ${member.address}")
    case MemberRemoved(member, previousStatus) ⇒
      log.info(s"Member removed ${member.address} with previous status ${previousStatus.toString}")
    case leader: LeaderChanged =>
      log.info(s"Leader Changed , new leader: ${leader.leader.map(_.toString).getOrElse("")}")
  }

  override def postStop(): Unit = {
    //deregister automatically from zk - delete ephemeral node
    log.info(s"Unsubscribe from cluster ${cluster.selfAddress}")
    cluster.unsubscribe(self)
    cluster.leave(cluster.selfAddress)
    forceRemoveZkNode(cluster.selfAddress)
  }


  /** PRIVATE METHODS */

  private def forceRemoveZkNode(memberAddress: Address) = {
    log.info(s"Force Remove zkNode for member $memberAddress")
    Try {
      findSeedNodes().find(node => node.id == memberAddress.hashCode.toString
        && node.host == memberAddress.host.getOrElse("")).
        map(_ => deleteZkNode(memberAddress))
    }.recover { case e => log.error(s"Error deleting zkNode $SeedPath/${memberAddress.hashCode.toString} on post Stop", e) }
  }

  private def forceUpsertZkNode(memberAddress: Address) = {
    Try {
      val memberUpNode = createSeedNode(memberAddress)
      if (!findSeedNodes().exists(_.id == memberUpNode.id)) {
        curatorFramework.create.creatingParentsIfNeeded.withProtection.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
          .forPath(s"$SeedPath/${memberUpNode.id}", write(memberUpNode).getBytes)
        log.info(s"Force creation of seed as ephemeral node ${memberUpNode.toString}")
      }
    }
  }

  private def createSeedNode(address: Address) =
    SeedNodeAddress(address.hashCode.toString, address.host.get, address.port.get, address.toString, address.protocol, address.system)
}
