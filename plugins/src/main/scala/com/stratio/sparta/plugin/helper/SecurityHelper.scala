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

object SecurityHelper {

  def kafkaSparkSubmitSecurityConf(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val vaultHost = scala.util.Properties.envOrNone("VAULT_HOST")
    val vaultToken = scala.util.Properties.envOrNone("VAULT_TOKEN")
    val vaultCertPath = configuration.getString("vaultCertPath", None)
    val vaultCertPassPath = configuration.getString("vaultCertPassPath", None)
    val vaultKeyPassPath = configuration.getString("vaultKeyPassPath", None)

    (vaultHost, vaultToken, vaultCertPath, vaultCertPassPath, vaultKeyPassPath) match {
      case (Some(host), Some(token), Some(certPath), Some(certPassPath), Some(keyPassPath)) =>
        Seq(
          ("spark.secret.kafka.security.protocol", "SSL"),
          ("spark.mesos.executor.docker.volumes",
            "/etc/pki/ca-trust/extracted/java/cacerts/:/etc/ssl/certs/java/cacerts:ro"),
          ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_ENABLE", "true"),
          ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PATH", certPath),
          ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PASS_PATH", certPassPath),
          ("spark.mesos.driverEnv.SPARK_SECURITY_KAFKA_VAULT_KEY_PASS_PATH", keyPassPath),
          ("spark.executorEnv.VAULT_HOST", host),
          ("spark.executorEnv.SPARK_SECURITY_KAFKA_ENABLE", "true"),
          ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PATH", certPath),
          ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_CERT_PASS_PATH", certPassPath),
          ("spark.executorEnv.SPARK_SECURITY_KAFKA_VAULT_KEY_PASS_PATH", keyPassPath),
          ("spark.secret.vault.host", host),
          ("spark.secret.vault.tempToken", VaultHelper.getTemporalToken(host, token))
        )
      case _ => Seq.empty[(String, String)]
    }
  }
}
