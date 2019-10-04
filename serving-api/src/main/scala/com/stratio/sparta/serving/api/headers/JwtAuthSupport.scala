/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.http.directives

import java.io.ByteArrayInputStream
import java.security.cert.{CertificateFactory, X509Certificate}
import java.util.Base64

import akka.actor.{ActorContext, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.HttpMethods
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.api.headers.HeadersAuthSupport
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.HeaderAuthUser
import com.stratio.sparta.serving.core.utils.HttpRequestUtils
import org.json4s.jackson.Serialization._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtOptions}
import spray.routing._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.util.{Failure, Success, Try}


trait JwtAuthSupport extends SLF4JLogging
  with SpartaSerializer {

  this: HttpService =>

  val context: ActorContext

  val PublicKeyRequestTimeout = 4 seconds

  val httpRequestsUtils = new HttpRequestUtils {
    override implicit val system: ActorSystem = context.system
    override implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  }

  private lazy val publicKeyUrl = SpartaConfig.getJwtConfig().get.getString("public.key.url")
  private lazy val defaultHeaderName = SpartaConfig.getJwtConfig().get.getString("header.name")
  private lazy val defaultHeaderPrefix = SpartaConfig.getJwtConfig().get.getString("header.prefix")
  private lazy val defaultJwtOptions = JwtOptions(expiration = false)

  implicit val executor: ExecutionContext

  lazy val promise = Promise[String]()
  lazy val publicKeyRequest = httpRequestsUtils.doRequest(
    uri = publicKeyUrl,
    resource = "",
    method = HttpMethods.GET,
    cookies = Seq.empty
  ).onComplete {
    case Success((_, response)) =>
      promise.success(response)
    case Failure(ex) =>
      promise.failure(ex)
  }

  lazy val publicKey = Await.result({
    publicKeyRequest
    promise.future
  }, PublicKeyRequestTimeout)



  private val extractHeaders: Directive1[Option[HeaderAuthUser]] =  extract { context =>
    for {
      token <- retrieveJwtToken(context)
      plainClaim <- decodeJwtToken(token, publicKey)
      claimFields <- Option(read[ClaimFields](plainClaim))
    } yield {
      Jwt.validate(token, getRSAPublicKeyFromX509Certificate(publicKey), Seq(JwtAlgorithm.RS256))
      HeaderAuthUser(claimFields.user, claimFields.groups)
    }
  }

  val authorizeJwtHeaders: Directive1[HeaderAuthUser] =
    extractHeaders.flatMap { headersInfo =>
      headersInfo.map(hinfo => {
        provide(hinfo)
      }).getOrElse(complete(HeadersAuthSupport.UnauthorizedTemplate))
    }


  protected[this] def retrieveJwtToken(context: RequestContext): Option[String] =
    context.request
      .headers
      .find(_.name == defaultHeaderName)
      .map(httpHeader => Option(httpHeader.value.stripPrefix(defaultHeaderPrefix)))
      .getOrElse {
        log.warn(s"Impossible to retrieve Jwt Header: $defaultHeaderName")
        None
      }

  protected[this] def decodeJwtToken(token: String, publicKey: String): Option[String] =
    Jwt.decodeRawAll(token, getRSAPublicKeyFromX509Certificate(publicKey), Seq(JwtAlgorithm.RS256), defaultJwtOptions) match {
      case Success((_, payload, _)) => Option(payload)
      case Failure(exception) => {
        log.warn(s"Failure decoding Jwt token : ${exception.getLocalizedMessage}")
        None
      }
    }

  protected[this] def deserializePlainClaim(plainClaim: String): Option[ClaimFields] =
    Try(read[ClaimFields](plainClaim)) match {
      case Success(claimFields) => Option (claimFields)
      case Failure(exception) => {
        log.warn(s"Failure deserializing to a ClaimFields object: ${exception.getLocalizedMessage}")
        None
      }
    }

  def getRSAPublicKeyFromX509Certificate(certificateString: String): String = {
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certificate = certificateFactory.generateCertificate(
      new ByteArrayInputStream(certificateString.getBytes())).asInstanceOf[X509Certificate]
    Base64.getEncoder.encodeToString(certificate.getPublicKey.getEncoded)
  }
}

case class ClaimFields(user: String, nbf: Long, exp: Long, groups: String)