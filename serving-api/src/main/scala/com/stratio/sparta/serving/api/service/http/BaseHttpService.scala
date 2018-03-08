/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
                    )(implicit marshaller: ToResponseMarshaller[T]): Unit =
    response match {
      case Left(Success(data)) =>
        context.complete(data)
      case Left(Failure(e)) =>
        context.failWith(e)
      case Right(UnauthorizedResponse(exception)) =>
        val errorModel = ErrorModel.toErrorModel(exception.getLocalizedMessage)
        context.complete(errorModel.statusCode, errorModel)
      case _ =>
        context.failWith(throw new ServerException(ErrorModel.toString(genericError)))
    }

  def deletePostPutResponse[T](
                                errorCodeToReturn: String,
                                response: Either[Try[T], UnauthorizedResponse],
                                genericError: ErrorModel
                              ): T =
    response match {
      case Left((Success(data))) =>
        data
      case Left((Failure(e))) =>
        throwExceptionResponse(e, errorCodeToReturn)
      case Right(UnauthorizedResponse(exception)) =>
        throw exception
      case _ =>
        throw new ServerException(ErrorModel.toString(genericError))
    }

  def deletePostPutResponse(
                             errorCodeToReturn: String,
                             response: Either[Try[_], UnauthorizedResponse],
                             genericError: ErrorModel,
                             statusResponse: StatusCode
                           ): StatusCode =
    response match {
      case Left((Success(_))) =>
        statusResponse
      case Left((Failure(e))) =>
        throwExceptionResponse(e, errorCodeToReturn)
      case Right(UnauthorizedResponse(exception)) =>
        throw exception
      case _ =>
        throw new ServerException(ErrorModel.toString(genericError))
    }

  protected def throwExceptionResponse(exception: Throwable, errorCodeToReturn: String): Nothing =
    Try(ErrorModel.toErrorModel(exception.getLocalizedMessage)) match {
      case Success(error) =>
        throw new ServerException(ErrorModel.toString(error))
      case Failure(_) =>
        throw new ServerException(ErrorModel.toString(ErrorModel(
          StatusCodes.InternalServerError.intValue,
          errorCodeToReturn,
          ErrorCodesMessages.getOrElse(errorCodeToReturn, UnknownError),
          None,
          Option(exception.getLocalizedMessage)
        )))
    }
}
