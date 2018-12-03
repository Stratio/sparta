/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.daos

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.dao.ParameterListDao
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterListFromWorkflow, ParameterVariable}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.ParameterListPostgresDao
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ParameterListPostgresDaoT extends DAOConfiguration
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging
  with JdbcSlickHelper
  with ScalaFutures {

  val profile = PostgresProfile

  import profile.api._

  var db1: profile.api.Database = _

  val paramVar_1 = ParameterVariable("pv1", Some("value1"))
  val paramVar_2 = ParameterVariable("pv2", Some("value2"))
  val paramVar_1_mod = ParameterVariable("pv1", Some("value1_modified"))
  val paramVar_3 = ParameterVariable("pv3", Some("value3"))
  val paramVar_4 = ParameterVariable("pv4", Some("value4"))
  val paramVar_5 = ParameterVariable("pv5", Some("value5"))

  val paramList_1 = ParameterList(name = "pl1", parameters = Seq(paramVar_1))
  val paramList_1_mod = ParameterList(name = "pl1", parameters = Seq(paramVar_1_mod, paramVar_2))
  val paramList_2 = ParameterList(name = "pl2", parameters = Seq(paramVar_1), parent = Some("pl1"))
  val paramList_3 = ParameterList(name = "pl3", parameters = Seq(paramVar_1_mod, paramVar_2), parent = Some("pl1"))
  val paramList_4 = ParameterList(name = "pl4", parameters = Seq(paramVar_1), parent = Some("pl1"))

  val nodes = Seq(
    NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
    NodeGraph("b", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(EdgeGraph("a", "b"))
  val pipeGraph = PipelineGraph(nodes, edges)
  val wfSettings = Settings()
  val testWorkflow = Workflow(Some("workflowID"), "wf-test", "{{WF_DESCRIPTION}}", wfSettings, pipeGraph)

  val plFromWorkflow = ParameterListFromWorkflow(
    workflow = testWorkflow,
    name = Some("plFromWf")
  )

  val parameterList = new ParameterListPostgresDao()

  trait ParameterListDaoTrait extends ParameterListDao {

    override val profile = PostgresProfile
    override val db: profile.api.Database = db1
  }

  override def beforeAll(): Unit = {
    db1 = Database.forConfig("", properties)

    val actions = DBIO.seq(sqlu"DROP TABLE IF EXISTS spartatest.parameter_list CASCADE;")
    Await.result(db1.run(actions), queryTimeout millis)

    PostgresFactory.invokeInitializationMethods()
    PostgresFactory.invokeInitializationDataMethods()

  }

  "A parameter list or lists" must {
    "be created" in new ParameterListDaoTrait {

      whenReady(parameterList.createFromParameterList(paramList_1), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.filter(_.name === paramList_1.name).result).map(_.toList)) { result =>
          result.size shouldBe 1
        }
      }
    }

    "be created with the parameters used in a workflow" in new ParameterListDaoTrait {

      whenReady(parameterList.createFromWorkflow(plFromWorkflow), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.filter(_.name === plFromWorkflow.name.get).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          val resultParamList = result.head
          resultParamList.name shouldBe plFromWorkflow.name.get
          resultParamList.parameters.head.name shouldBe "WF_DESCRIPTION"
        }
      }
    }

    "be found if a valid name is given" in new ParameterListDaoTrait {

      val result = parameterList.findByName(paramList_1.name).futureValue

      result.name shouldBe paramList_1.name
    }

    "be found if a valid id is given" in new ParameterListDaoTrait {

      val preResult = parameterList.findByName(paramList_1.name).futureValue
      val result = parameterList.findById(preResult.id.get).futureValue

      result.id shouldBe preResult.id
    }


    "be found if a valid parent name is given" in new ParameterListDaoTrait {

      whenReady(parameterList.createFromParameterList(paramList_2), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(parameterList.findByParent(paramList_1.name), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.head.parent.get shouldBe paramList_1.name
        }
      }
    }

    "be returned" in new ParameterListDaoTrait {

      val result = parameterList.findAllParametersList().futureValue

      whenReady(db.run(table.result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { res =>
        res.size shouldBe result.size
      }
    }

    "be updated along with its child lists and its values" in new ParameterListDaoTrait {

      val paramList_1_mod = ParameterList(
        name = "pl1",
        id = parameterList.findByName(paramList_1.name).futureValue.id,
        parameters = Seq(paramVar_1_mod, paramVar_2)
      )

      whenReady(parameterList.update(paramList_1_mod), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.filter(_.name === paramList_1_mod.name).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.head.name shouldBe paramList_1_mod.name
        }

        whenReady(db.run(table.filter(_.name === paramList_2.name).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.head.parameters.size shouldBe 2
          result.head.parameters.head.value.get shouldBe "value1_modified"
        }
      }
    }

    "be deleted if a valid name is given" in new ParameterListDaoTrait {

      val deleted = parameterList.deleteByName(paramList_2.name).futureValue
      deleted shouldBe true

      whenReady(db.run(table.filter(_.name === paramList_2.name).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
        result.isEmpty shouldBe true
      }
    }

    "be deleted if a valid ID is given" in new ParameterListDaoTrait {

      whenReady(parameterList.createFromParameterList(paramList_3), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        val id = parameterList.findByName(paramList_3.name).futureValue.id
        val deleted = parameterList.deleteByName(paramList_3.name).futureValue

        assert(deleted === true)

        whenReady(db.run(table.filter(_.name === paramList_3.name).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.isEmpty shouldBe true
        }
      }
    }

    "be deleted along with its child lists" in new ParameterListDaoTrait {

      whenReady(parameterList.createFromParameterList(paramList_4), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        val deleted = parameterList.deleteByName(paramList_1.name).futureValue

        assert(deleted === true)

        whenReady(db.run(table.filter(_.name === paramList_1.name).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.isEmpty shouldBe true
        }
        whenReady(db.run(table.filter(_.name === paramList_3.name).result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.isEmpty shouldBe true
        }
      }
    }

    "be deleted if the deleteAll method is called leaving an empty table in the database" in new ParameterListDaoTrait {

      whenReady(parameterList.deleteAllParameterList(), timeout(Span(queryTimeout, Milliseconds))) { _ =>
        whenReady(db.run(table.result).map(_.toList), timeout(Span(queryTimeout, Milliseconds))) { result =>
          result.isEmpty shouldBe true
        }
      }
    }
  }

  override def afterAll(): Unit = {

    val actions = DBIO.seq(sqlu"DROP TABLE IF EXISTS spartatest.parameter_list CASCADE;")
    Await.result(db1.run(actions), queryTimeout millis)

    db1.close()
  }
}