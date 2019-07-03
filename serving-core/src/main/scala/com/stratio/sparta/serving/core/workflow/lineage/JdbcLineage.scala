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
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.StringHelper._
import com.stratio.sparta.serving.core.services.CustomPostgresService
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Properties, Success, Try}


trait JdbcLineage extends SLF4JLogging {

  val lineageResource: String
  val lineageUri: String
  val tlsEnable: Boolean

  lazy val PostgresPrefix = "jdbc:postgresql://"
  lazy val SqlServerPrefix = "jdbc:sqlserver://"
  lazy val VipSuffix = ".marathon.l4lb.thisdcos.directory"
  lazy val MesosDNSSuffix = ".mesos"
  lazy val PoolMesosDNSSuffix = ".marathon.mesos"
  lazy val OracleName = "oracle"
  lazy val SqlServerName = "sqlserver"
  val PostgresName = "postgres"
  lazy val DbServiceProperty = "stratio.serviceid"
  val AllowedServices: Seq[String] = Seq(OracleName, SqlServerName, PostgresName)

  lazy val instancePostgresConfig = SpartaConfig.getPostgresConfig().get
  lazy val config = getDBConfigOrUseDefault(instancePostgresConfig)
  lazy val basicPgService = new CustomPostgresService(lineageUri, config)
  lazy val showServiceSql = s"SHOW $DbServiceProperty;"
  lazy val showCurrentSchemaSql = "select current_schema();"
  lazy val PublicSchema = "public"


  def getJdbcLineageProperties(stepType: String): Map[String, String] = {

    val typeJdbcService = lineageUri match {
      case uri if uri.containsIgnoreCase(PostgresName) => PostgresName
      case uri if uri.containsIgnoreCase(OracleName) => OracleName
      case uri if uri.containsIgnoreCase(SqlServerName) => SqlServerName
      case _ => "Unknown"
    }

    if (AllowedServices.contains(typeJdbcService)) {
      (getJdbcServiceName(typeJdbcService, lineageUri), getJdbcDatabase(lineageUri)) match {
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
  private def getJdbcServiceName(serviceType: String, url: String): Option[String] = {
    Try {
      serviceType match {
        case PostgresName =>
          Try(basicPgService.executeMetadataSql(showServiceSql)) match {
            case Success(response) =>
              response.headOption
            case Failure(e) =>
              log.warn(s"Error reading property $DbServiceProperty in database.", e)
              val stripUrl = url.stripPrefixWithIgnoreCase(PostgresPrefix).split("/").headOption.flatMap(_.split(":")
                .headOption).getOrElse("")
              if (stripUrl.endsWith(VipSuffix)) {
                val parsedVip = stripUrl.stripSuffix(VipSuffix)
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
        case OracleName =>
          url.split("@").lastOption.flatMap(_.stripPrefix("//").split(":", 2).headOption)
        case SqlServerName =>
          url.stripPrefixWithIgnoreCase(SqlServerPrefix).split(";", 2).headOption.flatMap(_.split(":").headOption)
        case _ =>
          url.split("//").lastOption.flatMap(_.split("/").headOption.flatMap(_.split(":").headOption))
      }
    }
    match {
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

  private def getDBConfigOrUseDefault(spartaInstanceConfig : Config): Config = {

    import java.net.URI
    import scala.collection.JavaConversions.mapAsJavaMap

    val formatToURI = lineageUri.stripPrefixWithIgnoreCase("jdbc:")

    Try {
      val parsedURI = new URI(formatToURI)
      val extraParams = parsedURI.getQuery
      val userRegex = "(?<=(user=))([\\w-]+)(?=\\&)".r
      val user: Option[String] = userRegex.findFirstIn(extraParams)
      val extraParamsWithoutUser =
        if(extraParams.containsIgnoreCase("user="))
          extraParams.replaceFirst("(user=[\\w-]+\\&)","")
        else extraParams

      val tlsConfigSparta =
        Map(
          "sslcert" -> Try(spartaInstanceConfig.getString("sslcert")).toOption,
          "sslkey" -> Try(spartaInstanceConfig.getString("sslkey")).toOption,
          "sslrootcert" -> Try(spartaInstanceConfig.getString("sslrootcert")).toOption,
          "user" -> Try(spartaInstanceConfig.getString("user")).toOption.orElse(Option(AppConstant.spartaTenant))
        ).filter{ case (_, v) => v.isDefined}.map{case (k, v) => (k, v.get)}

      val hostAndDatabase = Map(
        "host" -> s"${parsedURI.getHost}:${parsedURI.getPort}",
        "database" -> s"${parsedURI.getPath.stripPrefix("/")}"
      )

      val commonPostgresConfig = Map(
        "driver" -> Try(spartaInstanceConfig.getString("driver")).toOption,
        "numThreads" -> Option(2),
        "queueSize" -> Try(spartaInstanceConfig.getLong("queueSize")).toOption,
        "executionContext.parallelism" -> Option(4),
        "keepAliveConnection" -> Try(spartaInstanceConfig.getBoolean("keepAliveConnection")).toOption,
        "initializationFailFast" -> Try(spartaInstanceConfig.getBoolean("initializationFailFast")).toOption,
        "leakDetectionThreshold" -> Try(spartaInstanceConfig.getLong("leakDetectionThreshold")).toOption,
        "maxConnections" -> Option(2),
        "minConnections" -> Option(2),
        "connectionTimeout" -> Try(spartaInstanceConfig.getLong("connectionTimeout")).toOption,
        "validationTimeout" -> Try(spartaInstanceConfig.getLong("validationTimeout")).toOption
      ).filter{ case (_, v) => v.isDefined}.map{case (k, v) => (k, v.get)}

      val tlsOptionsAndExtraParams =
        Map("sslenabled" -> tlsEnable) ++ {
        if(tlsEnable) Map("extraParams"->s"$extraParamsWithoutUser") ++ tlsConfigSparta
        else
          user.fold(
            Map("extraParams" -> s"$extraParams")) { u =>
            Map("user" -> s"$u", "extraParams" -> s"$extraParamsWithoutUser")
          }
        }

      val customConfigMap = hostAndDatabase ++ tlsOptionsAndExtraParams ++ commonPostgresConfig
      ConfigFactory.parseMap(mapAsJavaMap(customConfigMap)).resolve
    }
  }.getOrElse(instancePostgresConfig)
}