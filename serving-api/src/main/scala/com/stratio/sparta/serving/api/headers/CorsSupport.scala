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
package com.stratio.sparta.serving.api.headers

import spray.http.HttpHeaders._
import spray.http.HttpMethods._
import spray.http.{AllOrigins, HttpMethod, HttpMethods, HttpResponse}
import spray.routing._

trait CorsSupport {

  this: HttpService =>

  private final val MaxAge = 1728000

  private val AllowOriginHeader = `Access-Control-Allow-Origin`(AllOrigins)
  private val OptionsCorsHeaders = List(
    `Access-Control-Allow-Headers`(
      "Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent"),
    `Access-Control-Max-Age`(MaxAge))

  def cors[T]: Directive0 = mapRequestContext {
    ctx => ctx.withRouteResponseHandling({
      case Rejected(x) if (ctx.request.method.equals(HttpMethods.OPTIONS) &&
        !x.filter(_.isInstanceOf[MethodRejection]).isEmpty) => {
          val allowedMethods: List[HttpMethod] = x.filter(_.isInstanceOf[MethodRejection]).map(rejection => {
            rejection.asInstanceOf[MethodRejection].supported
          })
          ctx.complete(HttpResponse().withHeaders(
            `Access-Control-Allow-Methods`(OPTIONS, allowedMethods: _*) :: AllowOriginHeader :: OptionsCorsHeaders
          ))
        }
      }).withHttpResponseHeadersMapped { headers =>
      AllowOriginHeader :: headers
    }
  }
}
