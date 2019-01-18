/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.lineage

import com.stratio.sparta.core.constants.SdkConstants.{PathKey, ResourceKey, ServiceKey}
import scala.util.Properties


trait JdbcLineage {

  val lineageResource: String
  val lineageUri : String

  lazy val PostgresPrefix = "jdbc:postgresql://"
  lazy val SqlServerPrefix = "jdbc:sqlserver://"
  lazy val VipSuffix = ".marathon.l4lb.thisdcos.directory"
  lazy val DomainSuffix = "." + Properties.envOrElse("EOS_INTERNAL_DOMAIN", "paas.labs.stratio.com")
  lazy val MesosDNSSuffix = ".mesos"
  lazy val PoolMesosDNSSuffix = ".marathon.mesos"
  lazy val OracleName = "oracle"
  lazy val SqlServerName = "sqlserver"

  def getJdbcLineageProperties : Map[String, String] = {
    if(lineageUri.contains("postgres")) {
      Map(
        ServiceKey -> getJdbcServiceName(lineageUri).getOrElse(""),
        PathKey -> s"/${getJdbcDatabase(lineageUri).getOrElse("")}",
        ResourceKey -> lineageResource)
    } else Map.empty[String, String]
  }

  private def getJdbcServiceName(url: String): Option[String] = {
    if (url.contains(PostgresPrefix)) {
      val stripUrl = url.toLowerCase.stripPrefix(PostgresPrefix).split("/").headOption.flatMap(_.split(":")
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
      if(url.toLowerCase.contains(OracleName))
        url.toLowerCase.split("@").lastOption.flatMap(_.stripPrefix("//").split(":",2).headOption)
      else if (url.toLowerCase.contains(SqlServerName))
        url.toLowerCase.stripPrefix(SqlServerPrefix).split(";",2).headOption.flatMap(_.split(":").headOption)
        else
        url.toLowerCase.split("//").lastOption.flatMap(_.split("/").headOption.flatMap(_.split(":").headOption))
    }
  }

  private def getJdbcDatabase(url: String): Option[String] = {
    if(url.toLowerCase.contains(SqlServerName))
      url.toLowerCase.split(";",2).lastOption.flatMap(_.split("=",2).lastOption.flatMap(_.split(";").headOption))
    else if (url.toLowerCase.contains(OracleName))
      url.split("//").lastOption.flatMap(_.split("/").lastOption)
    else
      url.toLowerCase.stripPrefix(PostgresPrefix).split("/",2).lastOption.flatMap(_.split("\\?").headOption)
  }
}