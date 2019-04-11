/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.spark.fs.s3a.auth

import java.net.URI
import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.amazonaws.auth.{BasicAWSCredentials, _}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.securitytoken.{AWSSecurityTokenService, AWSSecurityTokenServiceClientBuilder}
import com.amazonaws.{ClientConfiguration, ClientConfigurationFactory, Protocol}
import com.stratio.sparta.core.utils.Utils
import com.stratio.sparta.serving.core.constants.MarathonConstant
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.s3a.Constants
import org.apache.spark.security.{ConfigSecurity, HTTPHelper}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}


object VaultAWSCredentialsProvider extends SLF4JLogging {

  lazy val S3A_PREFIX: String = "fs.s3a"
  lazy val S3A_SECRET_KEY_SUFFIX: String = "assumedrole.credentials.vault.path"
  lazy val S3A_ROLE_SUFFIX: String = "assumedrole.role.arn.vault.path"

  lazy val STS_SESSION_DURATION_SUFFIX: String = "assumedrole.session.duration"
  lazy val STS_REGION_SUFFIX: String = "assumedrole.sts.region"
  lazy val STS_ENDPOINT_SUFFIX: String = "assumedrole.sts.endpoint"

  lazy val STS_DEFAULT_SESSION_DURATION: Int = 900
  lazy val STS_DEFAULT_REGION: Regions = Regions.EU_WEST_1

  lazy val STS_PROXY_HOST: String = "fs.s3a.sts.proxy.host"
  lazy val STS_PROXY_PORT: String = "fs.s3a.sts.proxy.port"
  lazy val STS_PROXY_PASS_SUFFIX: String = "fs.s3a.sts.proxy.credentials.vault.path"
  lazy val STS_PROXY_SSL_ENABLED: String = "fs.s3a.sts.proxy.ssl.enabled"

  private def propertyWithBucketPattern(optionSuffix: String, bucket: String): String =
    s"$S3A_PREFIX.bucket.$bucket.$optionSuffix"

  private def resolvePropValue(propertyName: String, conf: Configuration, bucket: Option[String] = None): Option[String] = {
    val suffixPropertyName = propertyName.replaceFirst(S3A_PREFIX, "")
    bucket.map(b => propertyWithBucketPattern(suffixPropertyName, b))
      .flatMap(bucketProp => Option(conf.get(bucketProp)))
      .orElse(Option(conf.get(s"$S3A_PREFIX.$suffixPropertyName")))
  }

  private def resolveIntPropValue(propertyName: String, conf: Configuration, bucket: Option[String] = None): Option[Int] =
    resolvePropValue(propertyName, conf, bucket).flatMap(v => Try(v.toInt).toOption)


  def loadCredentials(name: URI, conf: Configuration): STSAssumeRoleSessionCredentialsProvider = {

    log.debug(s"Loading AWS credentials from uri ($name) and configuration")

    val bucket = Option(name).map(_.getHost)
    val region: Regions =
      resolvePropValue(STS_REGION_SUFFIX, conf, bucket).map(Regions.fromName).getOrElse(STS_DEFAULT_REGION)

    val stsEndpoint: Option[String] = resolvePropValue(STS_ENDPOINT_SUFFIX, conf, bucket)

    val roleSessionDuration: Int =
      resolveIntPropValue(STS_SESSION_DURATION_SUFFIX, conf, bucket)
        .getOrElse(STS_DEFAULT_SESSION_DURATION)

    val roleSessionName =
      sys.env
        .get(MarathonConstant.ExecutionIdEnv)
        .orElse(sys.env.get(MarathonConstant.MesosTaskId).map(_.replaceAll("_", "-")))
        .getOrElse(UUID.randomUUID().toString)

    val secretKeyVaultPath: Option[String] = resolvePropValue(S3A_SECRET_KEY_SUFFIX, conf, bucket)
    val roleVaultPath: Option[String] = resolvePropValue(S3A_ROLE_SUFFIX, conf, bucket)

    log.debug(s"Secret key vault path: $secretKeyVaultPath")
    log.debug(s"Role ARN vault path: $roleVaultPath")

    require(secretKeyVaultPath.isDefined, s"$S3A_SECRET_KEY_SUFFIX is required")
    require(roleVaultPath.isDefined, s"$S3A_ROLE_SUFFIX is required")

    val stsCredentialsProvider: Try[STSAssumeRoleSessionCredentialsProvider] =
      loadLongLivedCredentialsFromVault(secretKeyVaultPath.get)
        .flatMap{ basicCredentials => getUserPassFromVault(roleVaultPath.get)
          .map{ case (_, roleARN) =>

            log.debug(s"Creating Assume role credentials provider from roleARN $roleARN")

            createAssumeRoleCredentialsProvider(
              createSTSClient(createAwsConfig(conf), basicCredentials, stsEndpoint, region),
              roleSessionName,
              roleARN,
              roleSessionDuration
            )
          }
        }

    stsCredentialsProvider.recoverWith{
      case NonFatal(exception) =>
        Failure(new Exception(s"Error loading STS credentials for S3 with error ${exception.getLocalizedMessage}", exception))
    }.get
  }


  private def loadLongLivedCredentialsFromVault(secretKeyVaultPath: String): Try[BasicAWSCredentials] =
    getUserPassFromVault(secretKeyVaultPath)
      .map{ case (aKey, sKey) =>
        log.debug(s"Creating Basic AWS credentials from Vault path $secretKeyVaultPath")

        new BasicAWSCredentials(aKey, sKey)
      }



  private def createSTSClient( awsConf: ClientConfiguration,
                               longLivedCredentials: AWSCredentials,
                               stsEndpoint: Option[String] = None,
                               stsRegion: Regions
                             ): AWSSecurityTokenService = {
    log.debug(s"Creating STS client with sts endpoint $stsEndpoint and stsRegion $stsRegion")

    val stsClientBuilder = AWSSecurityTokenServiceClientBuilder
      .standard()
      .withClientConfiguration(awsConf)
      .withCredentials(new AWSStaticCredentialsProvider(longLivedCredentials))

    val aWSSecurityTokenService = stsEndpoint.map(endpoint =>
      stsClientBuilder.withEndpointConfiguration(new EndpointConfiguration(endpoint, stsRegion.getName))
    ).getOrElse(
      stsClientBuilder.withRegion(stsRegion)
    ).build

    log.debug("Created STS client successfully")

    aWSSecurityTokenService
  }


  private def createAssumeRoleCredentialsProvider(
                                                   stsClient: AWSSecurityTokenService,
                                                   roleSessionName: String,
                                                   roleARN: String,
                                                   roleSessionDurationSeconds: Int
                                                 ): STSAssumeRoleSessionCredentialsProvider = {
    log.debug(s"Creating assume role credential provider with sessionName $roleSessionName," +
      s" roleARN $roleARN and duration $roleSessionDurationSeconds")

    val assumeRoleCredentialsProvider = new STSAssumeRoleSessionCredentialsProvider.Builder(roleARN, roleSessionName)
      .withRoleSessionDurationSeconds(roleSessionDurationSeconds)
      .withStsClient(stsClient)
      .build()

    log.debug("Created assume role credential provider successfully")

    assumeRoleCredentialsProvider
  }

  private def getUserPassFromVault(vaultPath: String): Try[(String,String)] = {
    val requestUrl = s"${ConfigSecurity.vaultURI.get}/$vaultPath"
    HTTPHelper.executeGet(
      requestUrl,
      "data",
      Some(Seq(("X-Vault-Token", ConfigSecurity.vaultToken.get)))
    ).recoverWith {
      case e: Exception =>
        Failure(new Exception("Error retrieving password from vault", e))
    }.flatMap { vaultResponse =>
      val userPass =
        for {
          user <- vaultResponse.get("user").map(_.toString).filter(_.nonEmpty)
          pass <- vaultResponse.get("pass").map(_.toString).filter(_.nonEmpty)
        } yield (user, pass)

      userPass
        .map(Success(_))
        .getOrElse(Failure(new RuntimeException("User or pass not found within Vault response")))
    }
  }


  private[auth] def createAwsConfig(conf: Configuration): ClientConfiguration = {

    import Constants._
    import Utils.optionImplicits._

    log.debug("Creating AWS configuration")

    val awsConfig = new ClientConfigurationFactory().getConfig
    val isSecured = resolvePropValue(SECURE_CONNECTIONS, conf).map(_.equalsIgnoreCase("true")).getOrElse(true)

    // connection props
    awsConfig.setMaxConnections(
      resolveIntPropValue(MAXIMUM_CONNECTIONS, conf).getOrElse(DEFAULT_MAXIMUM_CONNECTIONS)
    )
    awsConfig.setProtocol(if (isSecured) Protocol.HTTPS else Protocol.HTTP)

    awsConfig.setMaxErrorRetry(
      resolveIntPropValue(MAX_ERROR_RETRIES, conf).getOrElse(DEFAULT_MAX_ERROR_RETRIES)
    )
    awsConfig.setConnectionTimeout(
      resolveIntPropValue(ESTABLISH_TIMEOUT, conf).getOrElse(DEFAULT_ESTABLISH_TIMEOUT)
    )
    awsConfig.setSocketTimeout(
      resolveIntPropValue(SOCKET_TIMEOUT, conf).getOrElse(DEFAULT_SOCKET_TIMEOUT)
    )

    // proxy props
    resolvePropValue(STS_PROXY_HOST, conf).foreach { proxyHost =>

      log.debug(s"Using proxy in AWS connection with host $proxyHost")

      awsConfig.setProxyHost(proxyHost)
      awsConfig.setProxyPort(
        resolveIntPropValue(STS_PROXY_PORT, conf).getOrElse(if (isSecured) 443 else 80)
      )

      val proxySSLEnabled = resolvePropValue(STS_PROXY_SSL_ENABLED, conf).map(_.equalsIgnoreCase("true")).getOrElse(false)
      awsConfig.setProxyProtocol(if (proxySSLEnabled) Protocol.HTTPS else Protocol.HTTP)

      val proxyPasswordVaultPath: String =
        resolvePropValue(STS_PROXY_PASS_SUFFIX, conf)
          .getOrThrown(s"$STS_PROXY_PASS_SUFFIX is required when using a proxy")

      val (proxyUser, proxyPass) = getUserPassFromVault(proxyPasswordVaultPath).recoverWith {
        case NonFatal(exception) =>
          Failure(new Exception(s"Error loading credentials for STS proxy with error ${exception.getLocalizedMessage}", exception))
      }.get

      log.debug(s"Set proxy username@password to: $proxyUser@$proxyPass")

      awsConfig.setProxyUsername(proxyUser)
      awsConfig.setProxyPassword(proxyPass)
    }

    log.debug("Created AWS configuration successfully")

    awsConfig
  }

}


class VaultAWSCredentialsProvider private(private val stsCredsProvider: STSAssumeRoleSessionCredentialsProvider)
  extends AWSCredentialsProvider
    with SLF4JLogging
    with java.io.Closeable {

  def this(name: URI, conf: Configuration){
    this(VaultAWSCredentialsProvider.loadCredentials(name, conf))
  }

  def refresh(): Unit = {
    log.info("Refreshing AWS credentials")
    stsCredsProvider.refresh()
  }

  def getCredentials: AWSCredentials = {

    log.debug("Obtaining AWS credentials in vault credentials provider")

    stsCredsProvider.getCredentials
  }

  override def toString: String = getClass.getSimpleName

  override def close(): Unit =
    Try(stsCredsProvider.close())
      .recover{
        case NonFatal(exception) => log.warn(s"Exception closing STS provider with error ${exception.getLocalizedMessage}", exception);
      }

}
