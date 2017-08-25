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
import org.apache.spark.sql.jdbc.SpartaJdbcUtils.log
import org.apache.spark.security.VaultHelper._

import scala.util.Try

object SecurityHelper {

  def dataSourceSecurityConf(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val tlsEnable = Try(configuration.getBoolean("tlsEnable")).getOrElse(false)

    if (tlsEnable) {
      val useDynamicAuthentication = Try {
        scala.util.Properties.envOrElse("USE_DYNAMIC_AUTHENTICATION", "false").toBoolean
      }.getOrElse(false)
      val vaultHost = scala.util.Properties.envOrNone("VAULT_HOSTS").notBlank
      val vaultPort = scala.util.Properties.envOrNone("VAULT_PORT").notBlank
      val vaultToken = scala.util.Properties.envOrNone("VAULT_TOKEN").notBlank
      val appName = scala.util.Properties.envOrNone("MARATHON_APP_LABEL_DCOS_SERVICE_NAME")
        .notBlank
        .orElse(scala.util.Properties.envOrNone("TENANT_NAME").notBlank)
      val caName = configuration.getString("caName", None).notBlank

      (vaultHost, vaultPort, caName, appName) match {
        case (Some(host), Some(port), Some(ca), Some(name)) =>
          Seq(
            ("spark.mesos.driverEnv.SPARK_DATASTORE_SSL_ENABLE", "true"),
            ("spark.mesos.driverEnv.VAULT_HOST", host),
            ("spark.mesos.driverEnv.VAULT_PORT", port),
            ("spark.mesos.driverEnv.VAULT_PROTOCOL", "https"),
            ("spark.mesos.driverEnv.APP_NAME", name),
            ("spark.mesos.driverEnv.CA_NAME", ca),
            ("spark.executorEnv.SPARK_DATASTORE_SSL_ENABLE", "true"),
            ("spark.executorEnv.VAULT_HOST", host),
            ("spark.executorEnv.VAULT_PORT", port),
            ("spark.executorEnv.VAULT_PROTOCOL", "https"),
            ("spark.executorEnv.APP_NAME", name),
            ("spark.executorEnv.CA_NAME", ca),
            ("spark.secret.vault.host", host),
            ("spark.secret.vault.hosts", host),
            ("spark.secret.vault.port", port),
            ("spark.secret.vault.protocol", "https")
          ) ++ {
            if (vaultToken.isDefined && !useDynamicAuthentication) {
              val tempToken = getTemporalToken(SecurityHelper.getVaultUri(host, port), vaultToken.get)
              Seq(
                ("spark.mesos.driverEnv.VAULT_TEMP_TOKEN", tempToken)
                //("spark.secret.vault.tempToken", tempToken)
              )
            } else Seq.empty[(String, String)]
          }
        case _ =>
          log.warn("TLS is enabled but the properties are wrong")
          Seq.empty[(String, String)]
      }
    } else Seq.empty[(String, String)]
  }

  //scalastyle:off
  def kafkaSecurityConf(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val tlsEnable = Try(configuration.getBoolean("vaultTLSEnable")).getOrElse(false)

    if (tlsEnable) {
      val useDynamicAuthentication = Try {
        scala.util.Properties.envOrElse("USE_DYNAMIC_AUTHENTICATION", "false").toBoolean
      }.getOrElse(false)
      val vaultHost = scala.util.Properties.envOrNone("VAULT_HOSTS").notBlank
      val vaultPort = scala.util.Properties.envOrNone("VAULT_PORT").notBlank
      val vaultToken = scala.util.Properties.envOrNone("VAULT_TOKEN").notBlank
      val vaultRoleId = scala.util.Properties.envOrNone("VAULT_ROLE_ID").notBlank
      val vaultSecretId = scala.util.Properties.envOrNone("VAULT_SECRET_ID").notBlank
      val vaultCertPath = configuration.getString("vaultCertPath", None).notBlank
      val vaultCertPassPath = configuration.getString("vaultCertPassPath", None).notBlank
      val vaultKeyPassPath = configuration.getString("vaultKeyPassPath", None).notBlank
      val vaultTrustStorePath = configuration.getString("vaultTrustStorePath", None).notBlank
      val vaultTrustStorePassPath = configuration.getString("vaultTrustStorePassPath", None).notBlank
      val vaultRole = configuration.getString("vaultRole", None).notBlank

      token = vaultToken

      (vaultHost, vaultPort, vaultCertPath, vaultCertPassPath, vaultKeyPassPath, vaultTrustStorePath,
        vaultTrustStorePassPath) match {
        case (Some(host), Some(port), Some(certPath), Some(certPassPath), Some(keyPassPath),
        Some(trustStorePath), Some(trustStorePassPath)) =>
          Seq(
            ("spark.mesos.executor.docker.volumes",
              "/etc/pki/ca-trust/extracted/java/cacerts/:/etc/ssl/certs/java/cacerts:ro"),
            ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_ENABLE", "true"),
            ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_ENABLE", "true"),
            ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PATH", certPath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PASS_PATH", certPassPath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_KEY_PASS_PATH", keyPassPath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PATH", trustStorePath),
            ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PASS_PATH", trustStorePassPath),
            ("spark.mesos.driverEnv.VAULT_HOST", host),
            ("spark.mesos.driverEnv.VAULT_PORT", port),
            ("spark.mesos.driverEnv.VAULT_PROTOCOL", "https"),
            ("spark.executorEnv.SPARK_SECURITY_KAFKA_ENABLE", "true"),
            ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PATH", certPath),
            ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PASS_PATH", certPassPath),
            ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_KEY_PASS_PATH", keyPassPath),
            ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PATH", trustStorePath),
            ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PASS_PATH", trustStorePassPath),
            ("spark.executorEnv.VAULT_HOST", host),
            ("spark.executorEnv.VAULT_PORT", port),
            ("spark.executorEnv.VAULT_PROTOCOL", "https"),
            ("spark.secret.vault.host", host),
            ("spark.secret.vault.hosts", host),
            ("spark.secret.vault.port", port),
            ("spark.secret.vault.protocol", "https")
          ) ++ {
            val vaultUri = getVaultUri(host, port)
            if (vaultToken.isDefined && !useDynamicAuthentication) {
              val tempToken = getTemporalToken(vaultUri, vaultToken.get)
              Seq(
                ("spark.mesos.driverEnv.VAULT_TEMP_TOKEN", tempToken)
                //("spark.secret.vault.tempToken", tempToken)
              )
            } else if (vaultRole.isDefined && useDynamicAuthentication &&
              (vaultToken.isDefined || (vaultRoleId.isDefined && vaultSecretId.isDefined)))
              Seq(
                ("spark.secret.roleID", getRoleIdFromVault(vaultUri, vaultRole.get)),
                ("spark.secret.secretID", getSecretIdFromVault(vaultUri, vaultRole.get))
              )
            else Seq.empty[(String, String)]
          }
        case _ =>
          log.warn("TLS is enabled but the properties are wrong")
          Seq.empty[(String, String)]
      }
    } else Seq.empty[(String, String)]
  }

  //scalastyle:on

  def getVaultUri(host: String, port: String): String = s"https://$host:$port"
}
