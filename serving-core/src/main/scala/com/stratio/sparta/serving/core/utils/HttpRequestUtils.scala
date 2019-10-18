/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import java.net.HttpCookie

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.http.scaladsl.model.headers.{Accept, BasicHttpCredentials, RawHeader}
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import com.stratio.sparta.core.helpers.SSLHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait HttpRequestUtils extends SLF4JLogging {

  implicit val system: ActorSystem
  implicit val actorMaterializer: ActorMaterializer

  private lazy val sslContext = ConnectionContext.https(SSLHelper.getSSLContextV2(withHttps = true))
  private lazy val withoutSSLContext = ConnectionContext.https(SSLHelper.getSSLContextV2(withHttps = false))

  lazy val httpSystem = Http(system)

  def doRequest(
                 uri: String,
                 resource: String,
                 method: HttpMethod = HttpMethods.GET,
                 body: Option[String] = None,
                 cookies: Seq[HttpCookie] = Seq.empty[HttpCookie],
                 headers: Seq[RawHeader] = Seq.empty[RawHeader]
               )(implicit ev: Unmarshaller[ResponseEntity, String]): Future[(StatusCode, String)] = {

    log.debug(s"Sending HTTP request [${method.value}] to $uri/$resource")

    val request = createRequest(uri, resource, method, body, cookies, headers)
    for {
      response <- httpSystem.singleRequest(request, getSSLContextFromURI(uri))
      status = response.status
      entity <- Unmarshal(response.entity).to[String]
    } yield (status, entity)
  }

  def doRequestUri(
                 uri: Uri,
                 method: HttpMethod = HttpMethods.GET,
                 body: Option[String] = None,
                 httpCredentials: Option[BasicHttpCredentials] = None,
                 cookies: Seq[HttpCookie] = Seq.empty[HttpCookie],
                 headers: Seq[RawHeader] = Seq.empty[RawHeader]
               )(implicit ev: Unmarshaller[ResponseEntity, String]): Future[(StatusCode, String)] = {

    log.debug(s"Sending HTTP request [${method.value}] to $uri")

    val request = createRequestUri(uri, method, body, cookies, headers).addHeader(Accept(MediaRange(MediaTypes.`application/json`)))
    val requestWithCredentials = httpCredentials.map(cred => request.addCredentials(cred)).getOrElse(request)

    for {
      response <- httpSystem.singleRequest(requestWithCredentials, getSSLContextFromURI(uri.toString))
      status = response.status
      entity <- Unmarshal(response.entity).to[String]
    } yield (status, entity)
  }

  private def createRequest(
                             url: String,
                             resource: String,
                             method: HttpMethod,
                             body: Option[String],
                             cookies: Seq[HttpCookie],
                             headers: Seq[RawHeader]
                           ): HttpRequest =
    HttpRequest(
      uri = s"$url/$resource",
      method = method,
      entity = createRequestEntityJson(body),
      headers = createHeaders(cookies) ++ headers
    )

  private def createRequestUri(
                             uri: Uri,
                             method: HttpMethod,
                             body: Option[String],
                             cookies: Seq[HttpCookie],
                             headers: Seq[RawHeader]
                           ): HttpRequest =
    HttpRequest(
      uri = uri,
      method = method,
      entity = createRequestEntityJson(body),
      headers = createHeaders(cookies) ++ headers
    )

  private def createRequestEntityJson(body: Option[String]): RequestEntity =
    body match {
      case Some(jsBody) =>
        log.trace(s"body: $jsBody")
        HttpEntity(MediaTypes.`application/json`, jsBody)
      case _ =>
        HttpEntity.Empty
    }

  private def createHeaders(cookies: Seq[HttpCookie]): List[HttpHeader] =
    cookies.map(c => headers.Cookie(c.getName, c.getValue)).toList

  private def getSSLContextFromURI(uri: String): HttpsConnectionContext = {
    if(uri.toLowerCase.contains("https"))
      sslContext
    else withoutSSLContext
  }
}