/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.handler

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.json4s.jackson.Serialization._
import spray.http.{MediaTypes, StatusCodes}
import spray.routing.ExceptionHandler
import spray.routing.directives.{MiscDirectives, RespondWithDirectives, RouteDirectives}
import spray.util.LoggingContext

import scala.util.{Failure, Success, Try}

/**
 * This exception handler will be used by all our services to return a [ErrorModel] that will be used by the frontend.
 */
object CustomExceptionHandler extends MiscDirectives
  with RouteDirectives
  with RespondWithDirectives
  with SLF4JLogging
  with SpartaSerializer {

  implicit def exceptionHandler(implicit logg: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case exception: Throwable =>
        requestUri { _ =>
          log.warn(exception.getLocalizedMessage)
          respondWithMediaType(MediaTypes.`application/json`) {
            Try(ErrorModel.toErrorModel(exception.getLocalizedMessage)) match {
              case Success(error) =>
                complete(error.statusCode, write(error))
              case Failure(_) =>
                val error = new ErrorModel(
                  StatusCodes.InternalServerError.intValue,
                  ErrorModel.UnknownErrorCode,
                  ErrorModel.UnknownError,
                  None,
                  Option(exception.getLocalizedMessage)
                )
                complete(StatusCodes.InternalServerError, write(error))
            }
          }
        }
    }
  }
}