/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.lineage

import com.stratio.sparta.core.constants.SdkConstants.{PathKey, ResourceKey, ServiceKey}

trait JdbcLineage {

  val lineageResource: String
  val lineageUri : String

  def getJdbcLineageProperties : Map[String, String] = {
    if(lineageUri.contains("postgres")) {
      Map(
        ServiceKey -> getPostgresServiceName(lineageUri).getOrElse(""),
        PathKey -> getPostgresDatabase(lineageUri).getOrElse(""),
        ResourceKey -> lineageResource)
    } else Map.empty[String, String]
  }

  private def getPostgresServiceName(url: String): Option[String] =
    url.toLowerCase.stripPrefix("jdbc:postgresql://").split("/").headOption.flatMap{ head =>
      val splitPath = head.split("\\.")
      if (splitPath(0).startsWith("pg"))
        splitPath.lift(1)
      else
        splitPath.lift(0)
    }

  private def getPostgresDatabase(url: String): Option[String] = {
    url.toLowerCase.stripPrefix("jdbc:postgresql://").split("/").lift(1).flatMap(_.split("\\?").headOption)
  }
}
