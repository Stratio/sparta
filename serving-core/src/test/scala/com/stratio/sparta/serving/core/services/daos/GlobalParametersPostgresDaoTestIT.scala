/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.daos

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.dao.GlobalParametersDao
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.models.parameters.{GlobalParameters, ParameterVariable}
import com.stratio.sparta.serving.core.services.dao.GlobalParametersPostgresDao
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import slick.jdbc.PostgresProfile

@RunWith(classOf[JUnitRunner])
class GlobalParametersPostgresDaoTestIT extends DAOConfiguration
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with SLF4JLogging
  with JdbcSlickHelper
  with ScalaFutures {

  val profile = PostgresProfile
  import profile.api._
  var db1: profile.api.Database = _
  val queryTimeout : Int = 500

  val paramVar_1 = ParameterVariable("PV1", Some("value1"))
  val paramVar_2 = ParameterVariable("PV2", Some("value2"))
  val paramVar_1_mod = ParameterVariable("PV1", Some("value1_modified"))
  val paramVar_2_mod = ParameterVariable("PV2", Some("value2_modified"))
  val paramVar_3 = ParameterVariable("PV3", Some("value3"))

  val globalParams_1 = GlobalParameters(Seq(paramVar_1))
  val globalParams_2 = GlobalParameters(Seq(paramVar_1_mod, paramVar_2))

  val globalParametersDao = new GlobalParametersPostgresDao()

  PostgresFactory.invokeInitializationMethods()
  PostgresFactory.invokeInitializationDataMethods()

  trait GlobalParametersDaoTrait extends GlobalParametersDao {

    override val profile = PostgresProfile
    override val db: profile.api.Database = db1
  }

  override def beforeAll(): Unit = {
    db1 = Database.forConfig("", properties)
  }

  "A global parameter list" must {
    "be created" in new GlobalParametersDaoTrait {

      whenReady(globalParametersDao.createGlobalParameters(globalParams_1)) {
        _ =>
          whenReady(db.run(table.filter(_.name === paramVar_1.name).result).map(_.toList)){
            result => result.head.name shouldBe paramVar_1.name
          }
      }
    }

    "be returned if the find method is called" in new GlobalParametersDaoTrait{
      val result = globalParametersDao.find().futureValue

      result shouldBe a[GlobalParameters]
    }

    "return an environmental variable" in new GlobalParametersDaoTrait{
      val result = globalParametersDao.findGlobalParameterVariable(paramVar_1.name).futureValue

      result.name shouldBe paramVar_1.name
    }

    "be updated, adding new variables and updating existing ones" in new GlobalParametersDaoTrait{
      import profile.api._

      whenReady(globalParametersDao.updateGlobalParameters(globalParams_2)) {
        _ =>
          whenReady(db.run(table.filter(_.name === paramVar_1.name).result).map(_.toList)){
            result =>
              result.head.value shouldBe paramVar_1_mod.value
          }

          whenReady(db.run(table.filter(_.name === paramVar_2.name).result).map(_.toList)){
            result =>
              result.head.value shouldBe paramVar_2.value
          }
      }
    }

    "be updated, inserting a new variable and update an existing one" in new GlobalParametersDaoTrait{
      import profile.api._

      whenReady(globalParametersDao.upsertParameterVariable(paramVar_2_mod)) {
        _ =>
          whenReady(db.run(table.filter(_.name === paramVar_2_mod.name).result).map(_.toList)) {
            result => {
              result.size shouldBe 1
              result.head.value shouldBe paramVar_2_mod.value
            }
          }
      }

      whenReady(globalParametersDao.upsertParameterVariable(paramVar_3)) {
        _ =>
          whenReady(db.run(table.filter(_.name === paramVar_3.name).result).map(_.toList)) {
            result =>
              result.head.value shouldBe paramVar_3.value
          }
      }
    }

    "delete a parameter if it exists in the list" in new GlobalParametersDaoTrait {

      val result = globalParametersDao.deleteGlobalParameterVariable(paramVar_3.name).futureValue

      result.variables.contains(paramVar_3) shouldBe false
    }

    "deleted" in new GlobalParametersDaoTrait {
      import profile.api._

      val result = globalParametersDao.deleteGlobalParameters().futureValue

      if (result)
        whenReady(db.run(table.result).map(_.toList)) {
          result => result.isEmpty shouldBe true
        }
    }
  }

  override def afterAll(): Unit = {
    db1.close()
  }
}