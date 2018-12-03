/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.postgres

import scala.util.{Failure, Success, Try}

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Milliseconds, Span}
import slick.jdbc.meta.MTable

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper

@RunWith(classOf[JUnitRunner])
class PostgresFactoryIT extends WordSpec with Matchers with BeforeAndAfterAll with JdbcSlickHelper with ScalaFutures with SLF4JLogging {

  private lazy val config = ConfigFactory.load()

  val host = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl = s""""$configHost:5432""""
      log.info(s"Postgres host from config: $hostUrl")
      s"host = $hostUrl\n"
    case Failure(e) =>
      log.info(s"Postgres host from default")
      val hostUrl =s""""localhost:5432""""
      s"host = $hostUrl\n"
  }

  Class.forName("org.postgresql.Driver")

  val updatedConf = ConfigFactory.parseString(host).withFallback(SpartaConfig.getPostgresConfig().get)
  SpartaConfig.postgresConfig = Option(updatedConf)
  val properties = ConfigFactory.parseProperties(slickConnectionProperties(updatedConf)).withFallback(updatedConf)
  val queryTimeout: Int = 20000

  import slick.jdbc.PostgresProfile.api._

  var db: Database = _

  "On Sparta startup PostgresFactory " must {

    "validate group table" in {
      PostgresFactory.invokeInitializationMethods
      whenReady(db.run(MTable.getTables), timeout(Span(queryTimeout, Milliseconds))) {
        result => result.exists(mTable => mTable.name.name == "groups") shouldBe true
      }
    }
  }

  override def beforeAll(): Unit = {
    db = Database.forConfig("", properties)
  }

  override def afterAll(): Unit = {
    db.close()
  }
}
