/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.workflow.lineage

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.helpers.StringHelper._
import com.stratio.sparta.serving.core.services.CustomPostgresService
import com.typesafe.config.{Config, ConfigFactory}


import scala.util.{Failure, Properties, Success, Try}


trait JdbcLineage extends SLF4JLogging {

  val lineageResource: String
  val lineageUri: String

  lazy val PostgresPrefix = "jdbc:postgresql://"
  lazy val SqlServerPrefix = "jdbc:sqlserver://"
  lazy val VipSuffix = ".marathon.l4lb.thisdcos.directory"
  lazy val DomainSuffix = "." + Properties.envOrElse("EOS_INTERNAL_DOMAIN", "paas.labs.stratio.com")
  lazy val MesosDNSSuffix = ".mesos"
  lazy val PoolMesosDNSSuffix = ".marathon.mesos"
  lazy val OracleName = "oracle"
  lazy val SqlServerName = "sqlserver"
  lazy val DbServiceProperty = "stratio.serviceid"

  lazy val config = getDBConfig
  lazy val basicPgService = new CustomPostgresService(lineageUri,config)
  lazy val showServiceSql = s"SHOW $DbServiceProperty;"
  lazy val showCurrentSchemaSql = "select current_schema();"
  lazy val PublicSchema = "public"

  def getJdbcLineageProperties(stepType: String): Map[String, String] = {
    if (
      lineageUri.containsIgnoreCase("postgres") ||
        lineageUri.containsIgnoreCase("oracle") ||
        lineageUri.containsIgnoreCase("sqlserver")
    ) {
      (getJdbcServiceName(lineageUri), getJdbcDatabase(lineageUri)) match {
        case (Some(serviceName), Some(path)) =>
          Map(
            ServiceKey -> serviceName,
            PathKey -> s"/$path",
            ResourceKey -> getLineageResource(stepType),
            SourceKey -> lineageUri,
            DefaultSchemaKey -> getDefaultSchema
          )
        case _ =>
          Map.empty[String, String]
      }
    } else Map.empty[String, String]
  }

  private def getDefaultSchema: String = {
    Try(basicPgService.executeMetadataSql(showCurrentSchemaSql)) match {
      case Success(response) =>
        response.headOption.getOrElse(PublicSchema)
      case Failure(e) =>
        log.warn(s"Error reading current schema in database.", e)
        PublicSchema
    }
  }

  private def getLineageResource(stepType: String): String = {
    if(stepType.equals(InputStep.StepType) && !lineageResource.contains(".")) {
      s"$getDefaultSchema.$lineageResource"
    } else lineageResource
  }

  //scalastyle:off
  private def getJdbcServiceName(url: String): Option[String] = {
    Try {
      Try(basicPgService.executeMetadataSql(showServiceSql)) match {
        case Success(response) =>
          response.headOption
        case Failure(e) =>
          log.warn(s"Error reading property $DbServiceProperty in database.", e)

          if (url.contains(PostgresPrefix)) {
            val stripUrl = url.stripPrefixWithIgnoreCase(PostgresPrefix).split("/").headOption.flatMap(_.split(":")
              .headOption).getOrElse("")

            if (stripUrl.endsWith(VipSuffix)) {
              val parsedVip = stripUrl.stripSuffix(VipSuffix)
              if (parsedVip.split("\\.").length > 1)
                parsedVip.split("\\.", 2).lastOption
              else
                Option(parsedVip)
            }
            else if (stripUrl.endsWith(DomainSuffix)) {
              val parsedVip = stripUrl.stripSuffix(DomainSuffix)
              if (parsedVip.split("\\.").length > 1)
                parsedVip.split("\\.", 2).lastOption
              else
                Option(parsedVip)
            }
            else if (stripUrl.endsWith(PoolMesosDNSSuffix))
              Option(stripUrl.stripSuffix(PoolMesosDNSSuffix))
            else if (stripUrl.endsWith(MesosDNSSuffix))
              stripUrl.stripSuffix(MesosDNSSuffix).split("\\.", 2).lastOption
            else
              None
          }
          else {
            if (url.containsIgnoreCase(OracleName))
              url.split("@").lastOption.flatMap(_.stripPrefix("//").split(":", 2).headOption)
            else if (url.containsIgnoreCase(SqlServerName))
              url.stripPrefixWithIgnoreCase(SqlServerPrefix).split(";", 2).headOption.flatMap(_.split(":").headOption)
            else
              url.split("//").lastOption.flatMap(_.split("/").headOption.flatMap(_.split(":").headOption))
          }
      }
    } match {
      case Success(serviceName) =>
        serviceName
      case Failure(e) =>
        log.warn(s"Error extracting lineage jdbc service name from uri $url with message ${e.getLocalizedMessage}")
        None
    }
  }

  private def getJdbcDatabase(url: String): Option[String] = {
    Try {
      if (url.containsIgnoreCase(SqlServerName))
        url.split(";", 2).lastOption.flatMap(_.split("=", 2).lastOption.flatMap(_.split(";").headOption))
      else if (url.containsIgnoreCase(OracleName))
        url.split("//").lastOption.flatMap(_.split("/").lastOption)
      else
        url.stripPrefixWithIgnoreCase(PostgresPrefix).split("/", 2).lastOption.flatMap(_.split("\\?").headOption)
    } match {
      case Success(database) =>
        database
      case Failure(e) =>
        log.warn(s"Error extracting lineage jdbc database name from uri $url with message ${e.getLocalizedMessage}")
        None
    }
  }

  private def getDBConfig:Config = {
    val hostName = s""""${lineageUri.stripPrefixWithIgnoreCase(PostgresPrefix).split("/").headOption.getOrElse("")}""""
    val customConfig = ConfigFactory.parseString(s"host = $hostName\n")
    val mainConfig = SpartaConfig.getPostgresConfig().get

    customConfig.withFallback(mainConfig)
  }
}