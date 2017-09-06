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

package com.stratio.sparta.serving.core.utils

import akka.actor.ActorSystem
import akka.testkit._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.scalatest._
import org.scalatest.mock.MockitoSugar

abstract class BaseUtilsTest extends TestKit(ActorSystem("UtilsText", SpartaConfig.daemonicAkkaConfig))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar {

  SpartaConfig.initMainConfig()
  val curatorFramework = mock[CuratorFramework]
  val interval = 60000

  protected def getWorkflowModel(id: Option[String] = Some("id"),
                                 name: String = "testWorkflow",
                                 executionMode : String = "local"): Workflow = {
    val settingsModel = SettingsModel(
      GlobalSettings(executionMode),
      CheckpointSettings(),
      StreamingSettings(),
      SparkSettings("local[*]", false, false, None, None, None, SubmitArguments(),
        SparkConf(SparkResourcesConf(), SparkDockerConf(), SparkMesosConf()))
    )
    val workflow = Workflow(
      id = id,
      settings = settingsModel,
      name = name,
      description = "whatever",
      pipelineGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
    )
    workflow
  }
}
