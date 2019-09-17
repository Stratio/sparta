/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor.remote

import akka.actor.Props
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.marathon.factory.MarathonApplicationFactory
import com.stratio.sparta.serving.core.models.RocketModes

class ValidatorGuardianActor extends GuardianActor {

  lazy val instanceConfig = SpartaConfig.getValidatorConfig().get

  lazy val marathonApplication = MarathonApplicationFactory.createWorkerApplication(
    workerBootstrapMode = rocketMode,
    id = marathonDeploymentTaskId,
    cpus = cpu,
    mem = mem,
    instances = Option(instances)
  )

  lazy val rocketMode = RocketModes.Validator

}

object ValidatorGuardianActor {

  def props: Props = Props[ValidatorGuardianActor]

}