/*
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

package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel.{ErrorCodesMessages, UnknownError}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import spray.http.{StatusCode, StatusCodes}
import spray.httpx.Json4sJacksonSupport
import spray.httpx.marshalling.ToResponseMarshaller
import spray.routing._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * It gives common operations such as error handling, i18n, etc. All HttpServices should extend of it.
  */
trait BaseHttpService extends HttpService with Json4sJacksonSupport with SpartaSerializer with SLF4JLogging {

  private val apiTimeout = Try(SpartaConfig.getDetailConfig.get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1

  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  implicit def executionContext: ExecutionContextExecutor = actorRefFactory.dispatcher

  implicit val actors: Map[String, ActorRef]

  val supervisor: ActorRef

  def routes(user: Option[LoggedUser] = None): Route

  def getResponse[T](
                      context: RequestContext,
                      errorCodeToReturn: String,
                      response: Either[Try[T], UnauthorizedResponse],
                      genericError: ErrorModel
                    )(implicit marshaller: ToResponseMarshaller[T]): Unit = {
    response match {
      case Left(Success(data)) =>
        context.complete(data)
      case Left(Failure(e)) =>
        Try(ErrorModel.toErrorModel(e.getLocalizedMessage)) match {
          case Success(error) =>
            context.complete(error.statusCode, error)
          case Failure(_) =>
            context.complete(StatusCodes.InternalServerError, ErrorModel(
              StatusCodes.InternalServerError.intValue,
              errorCodeToReturn,
              ErrorCodesMessages.getOrElse(errorCodeToReturn, UnknownError),
              None,
              Option(e.getLocalizedMessage)
            ))
        }
      case Right(UnauthorizedResponse(exception)) =>
        val errorModel = ErrorModel.toErrorModel(exception.getLocalizedMessage)
        context.complete(errorModel.statusCode, errorModel)
      case _ =>
        context.complete(genericError.statusCode, genericError)
    }
  }

  def deletePostPutResponse[T](
                                errorCodeToReturn: String,
                                response: Either[Try[T], UnauthorizedResponse],
                                genericError: ErrorModel
                              ): T = {
    response match {
      case Left((Success(data))) =>
        data
      case Left((Failure(e))) =>
        throw new ServerException(ErrorModel.toString(ErrorModel(
          StatusCodes.InternalServerError.intValue,
          errorCodeToReturn,
          ErrorCodesMessages.getOrElse(errorCodeToReturn, UnknownError),
          None,
          Option(e.getLocalizedMessage)
        )))
      case Right(UnauthorizedResponse(exception)) =>
        throw exception
      case _ =>
        throw new ServerException(ErrorModel.toString(genericError))
    }
  }

  def deletePostPutResponse(
                                errorCodeToReturn: String,
                                response: Either[Try[_], UnauthorizedResponse],
                                genericError: ErrorModel,
                                statusResponse: StatusCode
                              ): StatusCode = {
    response match {
      case Left((Success(_))) =>
        statusResponse
      case Left((Failure(e))) =>
        throw new ServerException(ErrorModel.toString(ErrorModel(
          StatusCodes.InternalServerError.intValue,
          errorCodeToReturn,
          ErrorCodesMessages.getOrElse(errorCodeToReturn, UnknownError),
          None,
          Option(e.getLocalizedMessage)
        )))
      case Right(UnauthorizedResponse(exception)) =>
        throw exception
      case _ =>
        throw new ServerException(ErrorModel.toString(genericError))
    }
  }
}
