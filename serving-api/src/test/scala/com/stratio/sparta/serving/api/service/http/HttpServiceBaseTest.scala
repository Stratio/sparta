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

package com.stratio.sparta.serving.api.service.http

import akka.actor.{ActorRef, ActorRefFactory}
import akka.testkit.TestActor.AutoPilot
import akka.testkit.{TestActor, TestProbe}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.sparta.serving.core.models.workflow._
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import spray.testkit.ScalatestRouteTest

/**
 * Common operations for http service specs. All of them must extend from this class.
 */
trait HttpServiceBaseTest extends WordSpec
  with Matchers
  with BeforeAndAfterEach
  with ScalatestRouteTest
  with SpartaSerializer {

  val testProbe: TestProbe = TestProbe()

  def actorRefFactory: ActorRefFactory = system

  val granularity = 30000
  val interval = 60000

  val localConfig = ConfigFactory.parseString(
    """
      |sparta{
      |   config {
      |     executionMode = local
      |   }
      |   zookeeper{
      |     storagePath = "/stratio/sparta/sparta"
      |   }
      |
      |   local {
      |    spark.app.name = SPARTA
      |    spark.master = "local[*]"
      |    spark.executor.memory = 1024m
      |    spark.app.name = SPARTA
      |    spark.sql.parquet.binaryAsString = true
      |    spark.streaming.concurrentJobs = 1
      |    #spark.metrics.conf = /opt/sds/sparta/benchmark/src/main/resources/metrics.properties
      |  }
      |}
    """.stripMargin)

  // XXX Protected methods.

  protected def getFragmentModel(id: Option[String]): TemplateElement =
    TemplateElement(id, "input", "name", Option("description"), "TestInputStep", "Test", Map())

  protected def getFragmentModel(): TemplateElement =
    getFragmentModel(None)

  protected def getWorkflowStatusModel(): WorkflowStatus =
    WorkflowStatus("id", WorkflowStatusEnum.Launched)

  protected def getWorkflowModel(): Workflow = {
    val settingsModel = Settings(
      GlobalSettings(),
      StreamingSettings("6s", None, None, None, CheckpointSettings("test/test")),
      SparkSettings("local[*]", false, false, None, SubmitArguments(),
        SparkConf(SparkResourcesConf(), SparkDockerConf(), SparkMesosConf()))
    )
    val workflow = Workflow(
      id = Option("id"),
      settings = settingsModel,
      name = "testworkflow",
      description = "whatever",
      pipelineGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
    )

    workflow
  }

  protected def getWorkflowExecutionModel: WorkflowExecution =
    WorkflowExecution(
      id = "exec1",
      sparkSubmitExecution = SparkSubmitExecution(
        driverClass = "driver",
        driverFile = "file",
        master = "master",
        submitArguments = Map(),
        sparkConfigurations = Map(),
        driverArguments = Map(),
        sparkHome = "sparkHome"
      )
    )

  protected def getSpartaFiles: Seq[SpartaFile] =
    Seq(SpartaFile(fileName = "file.jar",
      uri = "http://localhost:9090/driver/file.jar",
      localPath = "/tmp/file.jar",
      size = "0",
      version = None
  ))


  /**
   * Starts and actor used to reply messages though the akka system. By default it can send a message to reply and it
   * will be use a default TestProbe. Also, it is possible to send a custom autopilot with a specific logic.
   *
   * @param message          that will be sent to the actor.
   * @param currentTestProbe with the [TestProbe] to use.
   * @param autopilot        is an actor that contains the logic that will be used when a message arrives.
   */
  protected def startAutopilot(message: Any,
                               currentTestProbe: TestProbe = this.testProbe,
                               autopilot: Option[AutoPilot] = None): Unit = {
    currentTestProbe.setAutoPilot(
      autopilot.getOrElse(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case x =>
              sender ! message
              TestActor.NoAutoPilot
          }
      })
    )
  }
}
