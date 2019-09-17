/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon.builder

import com.stratio.sparta.serving.core.marathon.{MarathonContainer, _}
import com.stratio.sparta.serving.core.models.SpartaSerializer

object DefaultBuilderImplicits {

  def newMarathonApplication(): MarathonApplication = {
    MarathonApplication(
      id = "None",
      cpus = -1,
      mem = -1,
      instances = Option(-1),
      container = MarathonContainer(
        docker = Docker(
          image = "None",
          network = "None"
        )
      )
    )
  }

  implicit class Builder(marathonApplication: MarathonApplication) extends SpartaSerializer {

    def withId(id: String): MarathonApplication = marathonApplication.copy(id = id)

    def withCpus(cpus: Double): MarathonApplication = marathonApplication.copy(cpus = cpus)

    def withMem(mem: Int): MarathonApplication = marathonApplication.copy(mem = mem)

    def withInstances(instances: Option[Int]): MarathonApplication = marathonApplication.copy(instances = instances)

    def withContainer(container: MarathonContainer): MarathonApplication = marathonApplication.copy(container = container)

    def addEnv(newEnvs: Map[String, String]): MarathonApplication = marathonApplication.copy(
      env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ newEnvs))
  }
}
