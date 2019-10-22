/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models.qualityrule.resources

import com.stratio.sparta.core.models.MetadataPath.fromMetadataPathString
import com.stratio.sparta.core.models.PropertyKeyValue._
import com.stratio.sparta.core.models.ResourcePlannedQuery
import com.stratio.sparta.core.models.SpartaQualityRule._
import com.stratio.sparta.core.utils.RegexUtils._



case class JDBCResource(tableName: String,
                        databaseTableName: String,
                        connectionURI: String,
                        driver: Option[String],
                        extraConfig: Map[String, String]) extends ResourceQR {

  override def getSQLQuery(xdSessionConf: Map[String, String]) : String =
    s"""CREATE OR REPLACE TEMPORARY VIEW $tableName USING org.apache.spark.sql.jdbc OPTIONS(
       | url '$connectionURI',
       | dbtable '$databaseTableName',
       | driver '${driver.fold("org.postgresql.Driver"){ dr => dr }}'
       | ${if(extraConfig.nonEmpty) "," else ""}
       | ${extraConfig.toList.map{case(nameValue, value) => s"$nameValue '$value'"}.mkString(", ")})
     """.stripMargin.removeAllNewlines
}

object JDBCResource extends ResourceQRUtils {

  def apply(resourcePlannedQuery: ResourcePlannedQuery, changeName: Boolean = true): Option[JDBCResource] =  {
    val propertiesAsMap: Map[String,String] = resourcePlannedQuery.listProperties.seqToMap
    val resourceTableName = getResourceTableName(resourcePlannedQuery,changeName)
    val nameAndSchema : Option[String] = fromMetadataPathString(resourcePlannedQuery.metadataPath).resource
    val driverFromProperties = propertiesAsMap.get(qrDriver)
    val connectionUriFromProperties: Option[String] = propertiesAsMap.get(qrConnectionURI)
    val userOptions = getUserOptions(resourcePlannedQuery)

    for {
      connectionUri <- connectionUriFromProperties
      dbName <- getPathFromMetadata(resourcePlannedQuery)
      nameDBTable <- nameAndSchema
    } yield {
      val fullURI = connectionUri+ dbName
      JDBCResource(
        tableName = resourceTableName,
        databaseTableName = nameDBTable,
        driver = driverFromProperties,
        connectionURI = fullURI,
        extraConfig = userOptions
      )
    }
  }
}
