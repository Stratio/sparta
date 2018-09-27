/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.helpers

import java.io.FileInputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.ConfigFactory
import org.apache.http.ssl.SSLContextBuilder

import scala.util.{Properties, Try}

object SSLHelper extends SLF4JLogging {

  def getSSLContext(withHttps: Boolean): SSLContext = {
    val context = SSLContext.getInstance("TLS")
    if (withHttps) {

      val (keyStoreFile, keyStorePassword) = getKeyStoreFileAndPassword

      val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
      keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray)

      val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
      keyManagerFactory.init(keyStore, keyStorePassword.toCharArray)

      val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
      trustManagerFactory.init(keyStore)

      context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    }
    context
  }

  def getSSLContextV2(withHttps: Boolean): SSLContext = {
    if (withHttps) {

      val (keyStoreFile, keyStorePassword) = getKeyStoreFileAndPassword

      val (trustStoreFile, trustStorePassword) = getTrustStoreFileAndPassword

      val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
      keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray)

      val trustStore = KeyStore.getInstance(KeyStore.getDefaultType)
      trustStore.load(new FileInputStream(trustStoreFile), trustStorePassword.toCharArray)

      new SSLContextBuilder()
        .useProtocol("TLSv1.2")
        .loadTrustMaterial(trustStore, null)
        .loadKeyMaterial(keyStore, keyStorePassword.toCharArray)
        .build()
    } else SSLContext.getInstance("TLS")
  }

  def getKeyStoreFileAndPassword: (String, String) = {
    val config = ConfigFactory.load()

    val keyStoreFile = Try(config.getString("sparta.ssl.keystore.location")).recover { case _ =>
      log.debug("Impossible to obtain keystore file from configuration")
      Properties.envOrNone("SPARTA_TLS_KEYSTORE_LOCATION").get
    }.getOrElse(throw new Exception("Impossible to obtain keystore file"))

    val keyStorePassword = Try(config.getString("sparta.ssl.keystore.password")).recover { case _ =>
      log.debug("Impossible to obtain keystore password from configuration")
      Properties.envOrNone("SPARTA_TLS_KEYSTORE_PASSWORD").get
    }.getOrElse(throw new Exception("Impossible to obtain keystore password"))

    (keyStoreFile, keyStorePassword)
  }

  def getTrustStoreFileAndPassword: (String, String) = {
    val config = ConfigFactory.load()

    val trustStoreFile = Try(config.getString("sparta.ssl.truststore.location")).recover { case _ =>
      log.debug("Impossible to obtain truststore file from configuration")
      Properties.envOrNone("SPARTA_TLS_TRUSTSTORE_LOCATION").get
    }.getOrElse(throw new Exception("Impossible to obtain truststore file"))

    val trustStorePassword = Try(config.getString("sparta.ssl.truststore.password")).recover { case _ =>
      log.debug("Impossible to obtain truststore password from configuration")
      Properties.envOrNone("SPARTA_TLS_TRUSTSTORE_PASSWORD").get
    }.getOrElse(throw new Exception("Impossible to obtain truststore password"))

    (trustStoreFile, trustStorePassword)
  }
}
