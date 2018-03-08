/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.ssl

import java.io.FileInputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import com.stratio.sparta.serving.api.helpers.SpartaHelper.log
import com.stratio.sparta.serving.core.config.SpartaConfig
import spray.io._

import scala.util.{Failure, Success, Try}

trait SSLSupport {

  implicit def sslContext: SSLContext = {
    val context = SSLContext.getInstance("TLS")
    if(isHttpsEnabled) {
      val keyStoreResource = SpartaConfig.apiConfig.get.getString("certificate-file")
      val password = SpartaConfig.apiConfig.get.getString("certificate-password")

      val keyStore = KeyStore.getInstance("jks")
      keyStore.load(new FileInputStream(keyStoreResource), password.toCharArray)
      val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
      keyManagerFactory.init(keyStore, password.toCharArray)
      val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
      trustManagerFactory.init(keyStore)
      context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    }
    context
  }

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
    SpartaConfig.getSprayConfig match {
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
