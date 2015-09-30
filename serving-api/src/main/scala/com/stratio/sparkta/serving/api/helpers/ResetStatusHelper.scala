package com.stratio.sparkta.serving.api.helpers


import com.stratio.sparkta.serving.api.Sparkta._
import com.stratio.sparkta.serving.core.models.{SparktaSerializer, PolicyStatusModel}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import com.stratio.sparkta.serving.core.{SparktaConfig, AppConstant, CuratorFactoryHolder}
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

/**
 * Created by arincon on 23/09/15.
 */
object ResetStatusHelper extends SparktaSerializer {

  def ResetStatuses {
    Try {
      if (SparktaConfig.getClusterConfig.isEmpty) {
        val curator = CuratorFactoryHolder.getInstance()
        val contextPath = s"${AppConstant.ContextPath}"
        val children = curator.getChildren.forPath(contextPath)
        val statuses = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[PolicyStatusModel](new String(curator.getData.forPath(
            s"${AppConstant.ContextPath}/$element")))).toSeq
        statuses.foreach(p => update(PolicyStatusModel(p.id, PolicyStatusEnum.NotStarted)))
      }
      def update(policyStatus: PolicyStatusModel): Unit = {
        val curator = CuratorFactoryHolder.getInstance()
        val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
        val ips =
          read[PolicyStatusModel](new String(curator.getData.forPath(statusPath)))
        log.info(s">> Updating context ${policyStatus.id} : <${ips.status}> to <${policyStatus.status}>")
        curator.setData().forPath(statusPath, write(policyStatus).getBytes)
      }
    } match {
      case Failure(ex: NoNodeException) => log.error("No Zookeeper node for /stratio/sparkta/contexts yet")
      case Failure(ex: Exception) => throw ex
      case Success(())=> {}

    }
  }

}
