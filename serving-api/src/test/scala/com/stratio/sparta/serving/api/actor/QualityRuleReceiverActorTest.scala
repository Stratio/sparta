/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.serving.api.actor.QualityRuleReceiverActor._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
import org.json4s.native.Serialization.read
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class QualityRuleReceiverActorTest extends TestKit(ActorSystem("QualityRuleReceiverActorTest"))
  with ImplicitSender
  with FlatSpecLike
  with ShouldMatchers
  with SpartaSerializer
  with BeforeAndAfterAll
  with DefaultTimeout {

  "QualityRuleActor" should "retrieve all predecessors of all outputs in a workflow" in {
    val predecessors = getOutputPredecessorsWithTableName(workflow)
    val predecessorsNamesWithTableNames = (for {
      predecessor <- predecessors
      (nodeGraph, (_, tableName)) <- predecessor
    } yield {
      (nodeGraph.name, tableName)
    }).toMap
    predecessorsNamesWithTableNames.find { case (predecessorName, _) => predecessorName == "Postgres" } should be(Option(("Postgres", "mytable")))
    predecessorsNamesWithTableNames.find { case (predecessorName, _) => predecessorName == "Postgres_1" } should be(Option(("Postgres_1", "append")))
  }

  it should "retrieve all inputs and outputs in a workflow" in {
    val inputOutputGraphNodes = retrieveInputOutputGraphNodes(workflow)
    inputOutputGraphNodes.find { case (nodeGraph, _) => nodeGraph.name == "Test" }.isDefined should be(true)
    inputOutputGraphNodes.find { case (nodeGraph, _) => nodeGraph.name == "Postgres" }.isDefined should be(true)
    inputOutputGraphNodes.find { case (nodeGraph, _) => nodeGraph.name == "Postgres_1" }.isDefined should be(true)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override implicit val timeout: Timeout = Timeout(50 seconds)
  val workflow = read[Workflow](Source.fromInputStream(getClass.getResourceAsStream("/actor/QualityRuleActor/workflow.json")).mkString)
}