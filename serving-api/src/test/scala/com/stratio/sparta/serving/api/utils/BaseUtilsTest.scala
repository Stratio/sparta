/**
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
/**
  * Copyright (C) 2015 Stratio (http://stratio.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package com.stratio.sparta.serving.api.utils

import akka.actor.{ActorSystem, Props}
import akka.testkit._
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFramework
import org.scalatest._
import org.scalatest.mock.MockitoSugar

import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.sdk.{DimensionType, Input}
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor

abstract class BaseUtilsTest extends TestKit(ActorSystem("UtilsText"))

  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar {

  val curatorFramework = mock[CuratorFramework]
  val streamingContextService = mock[StreamingContextService]

  val policyStatusTestActorRef = TestActorRef(new PolicyStatusActor(curatorFramework))
  val policyStatusActorRef = system.actorOf(Props(new PolicyStatusActor(curatorFramework)))
  val policyStatusActor = policyStatusTestActorRef.underlyingActor

  val sparkStreamingContextTestActorRef = TestActorRef(new SparkStreamingContextActor(
    streamingContextService = streamingContextService,
    policyStatusActor = policyStatusActorRef,
    curatorFramework = curatorFramework))
  val sparkStreamingContextActorRef = system.actorOf(Props(new SparkStreamingContextActor(
    streamingContextService = streamingContextService,
    policyStatusActor = policyStatusActorRef,
    curatorFramework = curatorFramework)))

  val localConfig = ConfigFactory.parseString(
    """
      |sparta{
      |   config {
      |     executionMode = local
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
  val standaloneConfig = ConfigFactory.parseString(
    """
      |sparta{
      |   config {
      |     executionMode = standalone
      |   }
      |}
    """.stripMargin)

  val yarnConfig = ConfigFactory.parseString(
    """
      |sparta{
      |   config {
      |     executionMode = yarn
      |   }
      |}
    """.stripMargin)

  val mesosConfig = ConfigFactory.parseString(
    """
      |sparta{
      |   config {
      |     executionMode = mesos
      |   }
      |
      |   mesos {
      |    sparkHome = ""
      |    deployMode = cluster
      |    numExecutors = 2
      |    master =  "mesos://127.0.0.1:5050"
      |    spark.app.name = SPARTA
      |    spark.streaming.concurrentJobs = 1
      |    spark.cores.max = 2
      |    spark.mesos.extra.cores = 1
      |    spark.mesos.coarse = true
      |    spark.executor.memory = 2G
      |    spark.driver.cores = 1
      |    spark.driver.memory= 2G
      |    #spark.metrics.conf = /opt/sds/sparta/benchmark/src/main/resources/metrics.properties
      |  }
      |}
    """.stripMargin)
  val interval = 60000

  protected def getPolicyModel(id: Option[String] = Some("id"), name: String = "testPolicy"):
  AggregationPoliciesModel = {
    val rawData = new RawDataModel
    val outputFieldModel1 = OutputFieldsModel("out1")
    val outputFieldModel2 = OutputFieldsModel("out2")

    val transformations = Seq(TransformationsModel(
      "Morphlines",
      0,
      Input.RawDataKey,
      Seq(outputFieldModel1, outputFieldModel2),
      Map()))
    val dimensionModel = getDimensionModel
    val operators = getOperators
    val cubes = Seq(populateCube("cube1", outputFieldModel1, outputFieldModel2, dimensionModel, operators))
    val outputs = Seq(PolicyElementModel("mongo", "MongoDb", Map()))
    val input = Some(PolicyElementModel("kafka", "Kafka", Map()))
    val policy = AggregationPoliciesModel(
      id = id,
      version = None,
      storageLevel = AggregationPoliciesModel.storageDefaultValue,
      name = name,
      description = "whatever",
      sparkStreamingWindow = AggregationPoliciesModel.sparkStreamingWindow,
      checkpointPath = "test/test",
      rawData,
      transformations,
      Seq(),
      cubes,
      input,
      outputs,
      Seq(),
      userPluginsJars = Seq.empty[String])
    policy
  }


  def populateCube(name: String,
                   outputFieldModel1: OutputFieldsModel,
                   outputFieldModel2: OutputFieldsModel,
                   dimensionModel: Seq[DimensionModel],
                   operators: Seq[OperatorModel]): CubeModel =
    CubeModel(name, dimensionModel, operators, WriterModel(Seq(outputFieldModel1.name, outputFieldModel2.name)), Seq())

  def getOperators: Seq[OperatorModel] = {
    Seq(OperatorModel("Count", "countoperator", Map()))
  }

  def getDimensionModel: Seq[DimensionModel] = {
    Seq(DimensionModel(
      "dimensionName",
      "field1",
      DimensionType.IdentityName,
      DimensionType.DefaultDimensionClass)
    )
  }
}
