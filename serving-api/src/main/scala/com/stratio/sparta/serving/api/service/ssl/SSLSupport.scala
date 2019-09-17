/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.ssl

import akka.event.slf4j.SLF4JLogging
import javax.net.ssl.SSLContext
import com.stratio.sparta.core.helpers.SSLHelper
import com.stratio.sparta.serving.core.config.SpartaConfig
import spray.io._

import scala.util.{Failure, Success, Try}

trait SSLSupport extends SLF4JLogging{

  implicit def sslContext: SSLContext = SSLHelper.getSSLContext(isHttpsEnabled)

  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(Array(
        "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "TLS_RSA_WITH_AES_256_CBC_SHA256",
        "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"))
      engine.setEnabledProtocols(Array( "TLSv1.2" ))
      engine
    }
  }

  def isHttpsEnabled: Boolean =
    SpartaConfig.getSprayConfig() match {
      case Some(config) =>
        Try(config.getValue("ssl-encryption")) match {
          case Success(value) =>
            "on".equals(value.unwrapped())
          case Failure(e) =>
            log.error("Incorrect value in ssl-encryption option, setting https disabled", e)
            false
        }
      case None =>
        log.warn("Impossible to get spray config, setting https disabled")
        false
    }
}
