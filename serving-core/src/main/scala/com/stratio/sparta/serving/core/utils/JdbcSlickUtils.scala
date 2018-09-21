/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import java.util.Properties
import scala.util.{Failure, Success, Try}

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.JdbcProfile

import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection.log

trait JdbcSlickUtils {

  val profile: JdbcProfile

  import profile.api._

  val db: Database
}

trait JdbcSlickHelper {

  //scalastyle:off
  def slickConnectionProperties(config: Config) = {
    def properties(newUrl: String) = {
      val props = new Properties()
      val extraParams = Try(config.getString("extraParams")).toOption.notBlank
      val url = extraParams.fold(newUrl) { extraParameters => newUrl.concat(s"&$extraParameters") }
      props.put("url",s"""$url""")
      props
    }

    val urlConnection = config.getString("host")
    val user = config.getString("user")
    val urlWithDatabase = urlConnection.concat(s"/${config.getString("database")}").concat(s"?user=$user")
    if (config.getBoolean("sslenabled")) {
      val sslCert = config.getString("sslcert")
      val sslKey = config.getString("sslkey")
      val sslRootCert = config.getString("sslrootcert")
      val newUrl = urlWithDatabase.concat(s"&ssl=true&sslmode=verify-full&sslcert=$sslCert&sslkey=$sslKey&sslrootcert=$sslRootCert")
      properties(newUrl)
    }
    else {
      properties(urlWithDatabase)
    }
  }

  import slick.jdbc.JdbcBackend.Database

  def tryConnection(dbconf: Database) = {
    Try(dbconf.createSession.conn) match {
      case Success(con) => {
        con.close
      }
      case Failure(f) => {
        dbconf.close()
        dbconf.shutdown
        log.warn(s"Unable to connect to dataSource ${f.getMessage} ")
      }
    }
  }
}

object JdbcSlickConnection extends JdbcSlickHelper with SLF4JLogging {

  import slick.jdbc.JdbcBackend.Database

  lazy val db = synchronized {
    val conf = SpartaConfig.getPostgresConfig().getOrElse(ConfigFactory.load())
    val dbconf = Database.forConfig("", conf.withFallback(ConfigFactory.parseProperties(slickConnectionProperties(conf))))
    tryConnection(dbconf)
    dbconf
  }
}
