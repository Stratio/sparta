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

package com.stratio.sparta.plugin.helper

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.SparkConf
import org.apache.spark.sql.jdbc.SpartaJdbcUtils.log
import org.apache.spark.security.VaultHelper._

import scala.util.{Properties, Try}

object SecurityHelper {

  def dataStoreSecurityConf(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val tlsEnable = Try(configuration.getBoolean("tlsEnabled")).getOrElse(false)
    val securityOptions = getSecurityConfigurations.toSeq

    if (tlsEnable && securityOptions.nonEmpty) {
      val vaultCertPath = Properties.envOrNone("SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH").notBlank
      val vaultCertPassPath = Properties.envOrNone("SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH").notBlank
      val vaultKeyPassPath = Properties.envOrNone("SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH").notBlank
      val vaultTrustStorePath = Properties.envOrNone("SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH").notBlank
      val vaultTrustStorePassPath = Properties.envOrNone("SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH").notBlank
      val driverSecretFolder = Properties.envOrNone("SPARK_DRIVER_SECRET_FOLDER").notBlank

      (vaultCertPath, vaultCertPassPath, vaultKeyPassPath, vaultTrustStorePath, vaultTrustStorePassPath,
        driverSecretFolder) match {
        case (Some(certPath), Some(certPassPath), Some(keyPassPath),
        Some(trustStorePath), Some(trustStorePassPath), Some(secretFolder)) =>
          Seq(
            ("spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_ENABLE", "true"),
            ("spark.mesos.driverEnv.SPARK_DRIVER_SECRET_FOLDER", secretFolder),
            ("spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH", certPath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH", certPassPath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH", keyPassPath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH", trustStorePath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH", trustStorePassPath),
            ("spark.executorEnv.SPARK_SECURITY_DATASTORE_ENABLE", "true"),
            ("spark.executorEnv.SPARK_DRIVER_SECRET_FOLDER", secretFolder),
            ("spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH", certPath),
            ("spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH", certPassPath),
            ("spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH", keyPassPath),
            ("spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH", trustStorePath),
            ("spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH", trustStorePassPath)
          ) ++ securityOptions
        case _ =>
          log.warn("TLS is enabled but the properties are wrong")
          Seq.empty[(String, String)]
      }
    } else {
      log.warn("TLS is enabled but the properties are wrong")
      Seq.empty[(String, String)]
    }
  }

  def getDataStoreUri(sparkConf: Map[String, String]): String = {
    val sslCert = sparkConf.get("spark.ssl.datastore.certPem.path")
    val sslKey = sparkConf.get("spark.ssl.datastore.keyPKCS8.path")
    val sslRootCert = sparkConf.get("spark.ssl.datastore.caPem.path")

    (sslCert, sslKey, sslRootCert) match {
      case (Some(cert), Some(key), Some(rootCert)) =>
        s"&ssl=true&sslmode=verify-full&sslcert=$cert&sslrootcert=$rootCert&sslkey=$key"
      case _ => ""
    }
  }

  def getDataStoreSecurityOptions(sparkConf: Map[String, String]): Map[String, AnyRef] = {
    val prefixDataStore = "spark.ssl.datastore."

    if (sparkConf.get(prefixDataStore + "enabled").isDefined && sparkConf(prefixDataStore + "enabled") == "true") {
      val configDataStore = sparkConf.flatMap { case (key, value) =>
        if (key.startsWith(prefixDataStore))
          Option(key.replace(prefixDataStore, "") -> value)
        else None
      }

      getDataStoreKeys(configDataStore)
    } else Map.empty[String, AnyRef]
  }

  def getDataStoreSecurityOptions(sparkConf: SparkConf): Map[String, AnyRef] = {
    val prefixKafka = "spark.ssl.datastore."

    if (sparkConf.getOption(prefixKafka + "enabled").isDefined && sparkConf.get(prefixKafka + "enabled") == "true")
      getDataStoreKeys(sparkConf.getAllWithPrefix(prefixKafka).toMap)
    else Map.empty[String, AnyRef]
  }

  /* PRIVATE METHODS */

  private def getDataStoreKeys(configDataStore: Map[String, String]): Map[String, AnyRef] =
    Map(
      "security.protocol" -> "SSL",
      "ssl.key.password" -> configDataStore("keyPassword"),
      "ssl.keystore.location" -> configDataStore("keyStore"),
      "ssl.keystore.password" -> configDataStore("keyStorePassword"),
      "ssl.truststore.location" -> configDataStore("trustStore"),
      "ssl.truststore.password" -> configDataStore("trustStorePassword")
    )

  //scalastyle:off
  private def getSecurityConfigurations: Map[String, String] = {
    val useDynamicAuthentication = Try {
      Properties.envOrElse("USE_DYNAMIC_AUTHENTICATION", "false").toBoolean
    }.getOrElse(false)
    val vaultHost = Properties.envOrNone("VAULT_HOSTS").notBlank
    val vaultPort = Properties.envOrNone("VAULT_PORT").notBlank
    val vaultToken = Properties.envOrNone("VAULT_TOKEN").notBlank
    val securityProperties = (vaultHost, vaultPort) match {
      case (Some(host), Some(port)) =>
        Map(
          "spark.mesos.driverEnv.VAULT_HOSTS" -> host,
          "spark.mesos.driverEnv.VAULT_HOST" -> host,
          "spark.mesos.driverEnv.VAULT_PORT" -> port,
          "spark.mesos.driverEnv.VAULT_PROTOCOL" -> "https"
        ) ++ {
          if (vaultToken.isDefined && !useDynamicAuthentication)
            Map("spark.mesos.driverEnv.VAULT_TEMP_TOKEN" -> getTemporalToken)
          else Map.empty[String, String]
        }
      case _ =>
        Map.empty[String, String]
    }

    securityProperties
  }
}
