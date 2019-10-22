/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models.qualityrule.resources

import com.stratio.sparta.core.models.ResourcePlannedQuery
import com.stratio.sparta.core.utils.RegexUtils._

case class AvroResource(tableName: String, path:String, extraConfig: Map[String, String]) extends ResourceQR {

  override def getSQLQuery(xdSessionConf: Map[String, String]) : String =
    s"""CREATE OR REPLACE TEMPORARY VIEW $tableName USING com.databricks.spark.avro OPTIONS (
       | path '$path'
       | ${if (extraConfig.nonEmpty) "," else ""}
       | ${extraConfig.toList.map { case (nameValue, value) => s"$nameValue '$value'" }.mkString(", ")})""".stripMargin.removeAllNewlines
}

object AvroResource extends ResourceQRUtils{
  def apply(resourcePlannedQuery: ResourcePlannedQuery, changeName: Boolean = true): Option[AvroResource] = {

    val userOption = getUserOptions(resourcePlannedQuery)
    val resourceTableName = getResourceTableName(resourcePlannedQuery,changeName)

    for {
      pathProperty <- getPathFromMetadata(resourcePlannedQuery)
    } yield {
      AvroResource(
        tableName = resourceTableName,
        path = pathProperty,
        extraConfig = userOption
      )
    }
  }
}
