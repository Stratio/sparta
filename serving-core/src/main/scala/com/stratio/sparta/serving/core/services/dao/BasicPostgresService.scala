/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.utils.{JdbcSlickConnection, JdbcSlickUtils}
import slick.jdbc.PostgresProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class BasicPostgresService extends JdbcSlickUtils with SLF4JLogging with SpartaSerializer {

  implicit val exc = PostgresDaoFactory.pgExecutionContext

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  def executeSql(sql : String): Unit = {
      Try(Await.result(db.run(sqlu"#$sql"), AppConstant.DefaultApiTimeout seconds)) match {
        case Success(_) =>
          log.debug(s"Sql $sql executed successfully")
        case Failure(e) =>
          throw e
      }
    }

}
