/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.bootstrap

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import akka.stream.ActorMaterializer
import com.stratio.sparta.dg.agent.lineage.LineageServiceActor
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.actor._
import com.stratio.sparta.serving.api.actor.remote._
import com.stratio.sparta.serving.api.service.ssl.SSLSupport
import com.stratio.sparta.serving.core.actor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant
import com.stratio.sparta.serving.core.models.RocketModes
import com.stratio.sparta.serving.core.models.RocketModes.RocketMode
import com.stratio.sparta.serving.core.utils.AkkaClusterUtils
import com.stratio.sparta.serving.core.utils.AkkaClusterUtils.ActorSingletonInfo
import spray.can.Http

import scala.util.Try

case class ClusterAPIBootstrap(title: String, rocketMode : RocketMode)
                              (implicit system: ActorSystem,
                         materializer: ActorMaterializer) extends Bootstrap with SLF4JLogging with SSLSupport {

  def start: Unit = {
    log.info(s"# Bootstraping $title #")
    initAkkaCluster()
  }

  protected[bootstrap] def initAkkaCluster(): Unit = {
    system.actorOf(Props[SpartaClusterNodeActor], "clusterNode")
    Cluster(system) registerOnMemberUp {

      system.actorOf(Props[TopLevelSupervisorActor])

      val marathonSingletons = if (
        sys.env.get(MarathonConstant.SpartaServerMarathonAppId).isDefined &&
          rocketMode == RocketModes.Remote
      ) {
        Seq(
          ActorSingletonInfo(DebugGuardianActorName, DebugGuardianActor.props, MasterRole),
          ActorSingletonInfo(ValidatorGuardianActorName, ValidatorGuardianActor.props, MasterRole),
          ActorSingletonInfo(CatalogGuardianActorName, CatalogGuardianActor.props, MasterRole)
        )
      } else Seq.empty
      val actorsSingleton = Seq(
        ActorSingletonInfo(SchedulerMonitorActorName, SchedulerMonitorActor.props, MasterRole),
        ActorSingletonInfo(QualityRuleResultSenderActorName, QualityRuleResultSenderActor.props, MasterRole),
        ActorSingletonInfo(DebugDispatcherActorName, DebugDispatcherActor.props, MasterRole),
        ActorSingletonInfo(ValidatorDispatcherActorName, ValidatorDispatcherActor.props, MasterRole),
        ActorSingletonInfo(CatalogDispatcherActorName, CatalogDispatcherActor.props, MasterRole),
        ActorSingletonInfo(ExecutionStatusChangePublisherActorName, Props[ExecutionStatusChangePublisherActor], MasterRole),
        ActorSingletonInfo(ExecutionStatusChangeListenerActorName, Props[ExecutionStatusChangeListenerActor], MasterRole),
        ActorSingletonInfo(RunWorkflowPublisherActorName, RunWorkflowPublisherActor.props, MasterRole)
      )
      val metricsSingleton = {
        if(metricsEnabled())
          Seq(ActorSingletonInfo(JmxMetricsActorName, JmxMetricsActor.props, MasterRole))
        else Seq.empty
      }
      val lineageSingleton = {
        if(lineageEnabled())
          Seq(ActorSingletonInfo(LineageServiceActorName, LineageServiceActor.props, MasterRole))
        else Seq.empty
      }

      val controllerActor = system.actorOf(Props(new ControllerActor()), ControllerActorName)

      log.info("Binding Sparta API ...")
      IO(Http) ! Http.Bind(controllerActor,
        interface = SpartaConfig.getApiConfig().get.getString("host"),
        port = SpartaConfig.getApiConfig().get.getInt("port")
      )

      log.info("Sparta server initiated successfully")

      AkkaClusterUtils.startClusterSingletons(actorsSingleton ++ marathonSingletons ++ metricsSingleton ++ lineageSingleton)
      log.info(s"Rocket singletons: ${actorsSingleton.map(_.name).mkString(" ")} initiated successfully")
    }
  }

  private def metricsEnabled(): Boolean = {
    (
      Try(SpartaConfig.getDetailConfig().get.getBoolean("metrics.jmx.enable")).getOrElse(false) ||
        Try(SpartaConfig.getDetailConfig().get.getBoolean("metrics.prometheus.enable")).getOrElse(false)
      ) &&
      Try(SpartaConfig.getDetailConfig().get.getBoolean("user.metrics.enable")).getOrElse(false)
  }

  private def lineageEnabled(): Boolean =
    Try(SpartaConfig.getDetailConfig().get.getBoolean("lineage.enable")).getOrElse(false)

}
