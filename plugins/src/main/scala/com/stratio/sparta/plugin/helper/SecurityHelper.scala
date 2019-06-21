/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.helper

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import org.apache.spark.SparkConf
import org.apache.spark.security.VaultHelper._

import scala.util.{Failure, Properties, Success, Try}

object SecurityHelper extends SLF4JLogging {

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

  def addUserToConnectionURI(user: String, connectionURI: String) : String = {
    if (connectionURI.contains("user=")) connectionURI
    else {
      if (connectionURI.contains("?"))
        connectionURI + s"${if (!connectionURI.endsWith("&")) "&" else ""}user=$user"
      else
        connectionURI + s"?user=$user"
    }
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
            getTemporalToken match {
              case Success(token) =>
                Map("spark.mesos.driverEnv.VAULT_TEMP_TOKEN" -> token)
              case Failure(x) =>
                log.error("The temporal token could not be retrieved")
                Map.empty[String, String]
            }
          else Map.empty[String, String]
        }
      case _ =>
        Map.empty[String, String]
    }

    securityProperties
  }

  def elasticSecurityOptions(sparkConf: Map[String, String]): Map[String, String] = {
    val prefixSparkElastic = "spark.ssl.datastore."
    val prefixElasticSecurity = "es.net.ssl"

    if (sparkConf.get(prefixSparkElastic + "enabled").isDefined &&
      sparkConf(prefixSparkElastic + "enabled") == "true") {

      val configElastic = sparkConf.flatMap { case (key, value) =>
        if (key.startsWith(prefixSparkElastic))
          Option(key.replace(prefixSparkElastic, "") -> value)
        else None
      }

      val mappedProps = Map(
        s"$prefixElasticSecurity" -> configElastic("enabled"),
        s"$prefixElasticSecurity.keystore.pass" -> configElastic("keyStorePassword"),
        s"$prefixElasticSecurity.keystore.location" -> s"file:${configElastic("keyStore")}",
        s"$prefixElasticSecurity.truststore.location" -> s"file:${configElastic("trustStore")}",
        s"$prefixElasticSecurity.truststore.pass" -> configElastic("trustStorePassword")
      )

      mappedProps
    } else Map.empty[String, String]
  }
}
