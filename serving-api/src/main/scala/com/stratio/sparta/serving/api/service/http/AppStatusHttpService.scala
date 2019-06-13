/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import com.wordnik.swagger.annotations._
import slick.jdbc.JdbcBackend
import spray.http.StatusCodes
import spray.routing._

import scala.util.Try

@Api(value = HttpConstant.AppStatus, description = "Sparta service status")
trait AppStatusHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = checkStatus

  @ApiOperation(value = "Checks Sparta status based on the Zookeeper and Postgres connection",
    notes = "Returns Sparta status",
    httpMethod = "GET",
    response = classOf[String],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def checkStatus: Route = {
    path(HttpConstant.AppStatus) {
      get {
        complete {
          val maybeZKError = if(CuratorFactoryHolder.getInstance().getZookeeperClient.getZooKeeper.getState.isConnected) {
            None
          } else {
            Option(ErrorModel(StatusCodes.InternalServerError.intValue, AppStatusZk, ErrorCodesMessages.getOrElse(AppStatusZk, UnknownError)))
          }

          val maybePostgresError =
            Try(
              withSession(JdbcSlickConnection.getDatabase)(session => session.conn)
            ).failed.map { _ =>
              ErrorModel(StatusCodes.InternalServerError.intValue, AppStatusPostgres, ErrorCodesMessages.getOrElse(AppStatusPostgres, UnknownError))
            }.toOption

          val maybeErrors = maybeZKError ++ maybePostgresError

          if(maybeErrors.isEmpty) {
            StatusCodes.OK
          } else {
            import org.json4s.native.Serialization._
            throw new ServerException(write(maybeErrors))
          }
        }
      }
    }
  }

  private def withSession[T](slickDatabase: JdbcBackend.Database)(block: JdbcBackend.Session => T) = {
    val session = slickDatabase.createSession()
    try {
      block(session)
    } finally {
      session.close()
    }
  }

}
