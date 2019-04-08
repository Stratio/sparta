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
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.stratio.sparta.serving.core.constants.MarathonConstant
import org.apache.hadoop.conf.Configuration
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


  private def propertyWithBucketPattern(optionSuffix: String, bucket: String): String =
    s"$S3A_PREFIX.bucket.$bucket.$optionSuffix"

  private def resolvePropValue(propertyName: String, conf: Configuration, bucket: Option[String]): Option[String] = {
    val suffixPropertyName = propertyName.replaceFirst(S3A_PREFIX, "")
    bucket.map(b => propertyWithBucketPattern(suffixPropertyName, b))
      .flatMap(bucketProp => Option(conf.get(bucketProp)))
      .orElse(Option(conf.get(s"$S3A_PREFIX.$suffixPropertyName")))
  }

  def loadCredentials(name: URI, conf: Configuration): STSAssumeRoleSessionCredentialsProvider = {
    val bucket = Option(name).map(_.getHost)
    val region: Regions =
      resolvePropValue(STS_REGION_SUFFIX, conf, bucket).map(Regions.fromName).getOrElse(STS_DEFAULT_REGION)

    val stsEndpoint: Option[String] = resolvePropValue(STS_ENDPOINT_SUFFIX, conf, bucket)

    val roleSessionDuration: Int =
      resolvePropValue(STS_SESSION_DURATION_SUFFIX, conf, bucket)
        .flatMap(v => Try(v.toInt).toOption)
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
            createAssumeRoleCredentialsProvider(
              basicCredentials,
              roleSessionName,
              roleARN,
              roleSessionDuration,
              region,
              stsEndpoint
            )
          }
        }

    stsCredentialsProvider.recoverWith{
      case NonFatal(exception) =>
        Failure(new Exception("Error loading STS credentials for S3", exception))
    }.get
  }


  private def loadLongLivedCredentialsFromVault(secretKeyVaultPath: String): Try[BasicAWSCredentials] =
    getUserPassFromVault(secretKeyVaultPath)
      .map{ case (aKey, sKey) => new BasicAWSCredentials(aKey, sKey)}


  private def createAssumeRoleCredentialsProvider(
                                                   longLivedCredentials: AWSCredentials,
                                                   roleSessionName: String,
                                                   roleARN: String,
                                                   roleSessionDurationSeconds: Int,
                                                   stsRegion: Regions,
                                                   stsEndpoint: Option[String] = None
                                                 ): STSAssumeRoleSessionCredentialsProvider = {

    val stsClientBuilder = AWSSecurityTokenServiceClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(longLivedCredentials))

    val stsClient =
      stsEndpoint.map(endpoint =>
        stsClientBuilder.withEndpointConfiguration(new EndpointConfiguration(endpoint, stsRegion.getName))
      ).getOrElse(
        stsClientBuilder.withRegion(stsRegion)
      ).build

    new STSAssumeRoleSessionCredentialsProvider.Builder(roleARN, roleSessionName)
      .withRoleSessionDurationSeconds(roleSessionDurationSeconds)
      .withStsClient(stsClient)
      .build()
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

  def getCredentials: AWSCredentials =
    stsCredsProvider.getCredentials

  override def toString: String = getClass.getSimpleName

  override def close(): Unit =
  Try(stsCredsProvider.close())
    .recover{
      case NonFatal(exception) => log.warn("Exception closing STS provider", exception);
    }

}
