/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection.{slickConnectionProperties, tryConnection}
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.{JdbcBackend, PostgresProfile}

import scala.concurrent.Await
import scala.concurrent.duration._

class CustomPostgresService(uri: String, config: Config) extends SLF4JLogging with SpartaSerializer {

  val profile = PostgresProfile

  import profile.api._

  val db: Database = CustomPostgresService.getDatabase(uri, config)

  def executeMetadataSql(sql: String): Seq[String] = {
    log.debug(s"Executing sql in custom postgres service: $sql")
    Await.result(db.run(sql"#$sql".as[String]), AppConstant.DefaultApiTimeout seconds).toList
  }
}

object CustomPostgresService extends SLF4JLogging {

  import slick.jdbc.JdbcBackend.Database

  private val connections : scala.collection.mutable.Map[String, JdbcBackend.Database] = scala.collection.mutable.Map.empty
  private val db: Option[JdbcBackend.Database] = None

  def getDatabase(connectionKey: String,
                  configuration: Config
                 ): JdbcBackend.Database =
    synchronized {
      connections.getOrElse(connectionKey, {
        db.getOrElse {
          val slickConf = slickConnectionProperties(configuration)
          log.debug(s"Creating postgres connection in Custom postgres service with slick conf: ${slickConf.toString}")
          val database = Database.forConfig("", configuration.withFallback(ConfigFactory.parseProperties(slickConf)))
          tryConnection(database)
          connections.put(connectionKey, database)
          database
        }
      })
    }
}