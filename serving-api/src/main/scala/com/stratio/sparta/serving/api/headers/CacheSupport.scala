/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.headers

import spray.http.HttpHeaders._
import spray.http._
import spray.routing._

trait CacheSupport {

  this: HttpService =>

  private val CacheControlHeader = `Cache-Control`(CacheDirectives.`no-cache`)

  def noCache[T]: Directive0 = mapRequestContext {
    ctx => ctx.withHttpResponseHeadersMapped { headers =>
      CacheControlHeader :: headers
    }
  }
}