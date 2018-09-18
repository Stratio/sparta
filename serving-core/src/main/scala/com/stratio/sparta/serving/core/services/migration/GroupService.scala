/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.Group
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.Try

class GroupService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def findAll: Seq[Group] = {
    Try {
      JavaConversions.asScalaBuffer(curatorFramework.getChildren.forPath(GroupZkPath)).toList.map(groupID =>
        read[Group](new String(curatorFramework.getData.forPath(s"$GroupZkPath/$groupID"))))
    }.getOrElse(Seq.empty[Group])
  }

}
