/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import scala.collection.JavaConversions
import scala.util.Try

import akka.actor.Address
import akka.cluster.Cluster
import org.json4s.jackson.Serialization._

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer

case class SeedNodeAddress(id: String, host: String, port: Int, description: String, protocol: String, system: String)

trait SpartaClusterUtils extends SpartaSerializer {

  lazy val curatorFramework = CuratorFactoryHolder.getInstance()
  val SeedPath = AppConstant.ClusterSeedNodesZkPath

  //scalastyle:off
  def isThisNodeClusterLeader(cluster: Cluster): Boolean = Try(cluster.state.getLeader.toString.equals(cluster.selfAddress.toString)).getOrElse(false)

  def findSeedNodes(): Seq[SeedNodeAddress] = {
    Try {
      if (!CuratorFactoryHolder.existsPath(SeedPath)) {
        curatorFramework.create().creatingParentsIfNeeded.forPath(SeedPath)
        Seq.empty
      } else {
        val children = curatorFramework.getChildren.forPath(SeedPath)
        JavaConversions.asScalaBuffer(children).filterNot(_.contains("latch")).map(id => {
          read[SeedNodeAddress](new String(curatorFramework.getData.forPath(s"$SeedPath/$id")))
        })
      }
    }.getOrElse(Seq.empty)
  }

  def deleteZkNode(memberAddress: Address) = {
    val children = curatorFramework.getChildren.forPath(SeedPath)
    JavaConversions.asScalaBuffer(children).filter(_.contains(memberAddress.hashCode.toString)).
      map(id => {
        curatorFramework.delete().forPath(s"$SeedPath/$id")
      })
  }
}