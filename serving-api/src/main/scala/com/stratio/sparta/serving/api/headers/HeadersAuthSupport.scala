/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.headers


import com.stratio.sparta.serving.core.config.{AuthViaHeadersConfig, SpartaConfig}
import com.stratio.sparta.serving.core.models.authorization.HeaderAuthUser
import spray.http._
import spray.routing._

trait HeadersAuthSupport {

  this: HttpService =>

  lazy val headersAuthConfig: Option[AuthViaHeadersConfig] = SpartaConfig.getAuthViaHeadersConfig()

  private lazy val userHeaderName = headersAuthConfig.get.user
  private lazy val groupHeaderName = headersAuthConfig.get.group

  private val extractHeaders: Directive1[Option[HeaderAuthUser]] =  extract{ ctxt => // TODO use cookie?
    val headers = ctxt.request.headers

    for {
      user <- headers.collectFirst(optionalValue(userHeaderName.toLowerCase))
      group <- headers.collectFirst(optionalValue(groupHeaderName.toLowerCase)) // TODO authorize/validate group?
    } yield HeaderAuthUser(user, group)
  }

  val authorizeHeaders: Directive1[HeaderAuthUser] =
    extractHeaders.flatMap { headersInfo =>
      headersInfo.map(hinfo =>
        provide(hinfo)
      ).getOrElse(complete(HeadersAuthSupport.UnauthorizedTemplate))
    }

  private def optionalValue(header: String): PartialFunction[HttpHeader, String] = {
    case HttpHeader(`header`, value) ⇒ value
  }

}

object HeadersAuthSupport {

  val UnauthorizedTemplate: HttpResponse =
    HttpResponse(
      StatusCodes.Unauthorized,
      HttpEntity(
        new ContentType(MediaTypes.`text/html`, Some(HttpCharsets.`UTF-8`)),
        """|<html lang="en">
           |      <head></head>
           |      <body>
           |        <h1>Unauthorized</h1>
           |        <p> You don't have permission to access this server.</p>
           |      </body>
           |</html>""".stripMargin
      )
    )

}