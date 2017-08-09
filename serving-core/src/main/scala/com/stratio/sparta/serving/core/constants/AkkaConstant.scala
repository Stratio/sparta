/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.serving.core.constants

/**
 * Akka constants with Akka's actor paths.
 *
 * @author anistal
 */
object AkkaConstant {

  val FragmentActorName = "fragmentActor"
  val PolicyActorName = "policyActor"
  val ExecutionActorName = "executionActor"
  val ClusterLauncherActorName = "clusterLauncherActor"
  val LauncherActorName = "launcherActor"
  val PluginActorName = "pluginActor"
  val DriverActorName = "driverActor"
  val ControllerActorName = "controllerActor"
  val StatusActorName = "statusActor"
  val MarathonAppActorName = "marathonAppActor"
  val UpDownMarathonActor = "upDownMarathonActor"
  val ConfigActorName = "configurationActor"
  val MetadataActorName = "metadataActor"
  val CrossdataActorName = "crossdataActor"

  val DefaultTimeout = 19
  val DefaultInstances = 3

  def cleanActorName(initialName: String): String = initialName.replace(" ", "_")
}
