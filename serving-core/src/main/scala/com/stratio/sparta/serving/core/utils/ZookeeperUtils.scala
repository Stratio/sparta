/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import com.stratio.sparta.serving.core.config.SpartaConfig
import org.apache.zookeeper.common._

import scala.util.{Failure, Properties, Success, Try}

trait ZookeeperUtils {

  def retrievePathFromEnv: Option[String] = Properties.envOrNone("SPARTA_ZOOKEEPER_PATH") match {
    case Some(path) if path.nonEmpty => Option(path)
    case Some(_) => None
    case None => None
  }

  def retrieveIgnitePathFromEnv: String = Properties.envOrNone("SPARTA_IGNITE_ZOOKEEPER_PATH") match {
    case Some(path) if path.nonEmpty => path
    case Some(_) => "igniteNodes"
    case None => "igniteNodes"
  }

  def retrieveFromConf: Option[String] = Try(SpartaConfig.getZookeeperConfig().get.getString("storagePath")) match {
    case Success(confPath) if confPath.nonEmpty => Option(confPath)
    case Success(_) => None
    case Failure(_) => None
  }


  def checkIfValidPath(zkPath: String) : Boolean =
    Try(PathUtils.validatePath(zkPath)) match {
      case Success(_) => true
      case Failure(_) => false
    }
}
