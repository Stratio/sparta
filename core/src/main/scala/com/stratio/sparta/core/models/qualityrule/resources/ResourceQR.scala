/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models.qualityrule.resources

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.models.MetadataPath.fromMetadataPathString
import com.stratio.sparta.core.models.ResourcePlannedQuery
import com.stratio.sparta.core.models.PropertyKeyValue._
import com.stratio.sparta.core.models.SpartaQualityRule.filterQRMandatoryOptions
import com.stratio.sparta.core.utils.RegexUtils._


trait ResourceQR {
  def getSQLQuery(xdSessionConf: Map[String, String]) : String
}

trait ResourceQRUtils{
  def getResourceTableName(resourcePlannedQuery: ResourcePlannedQuery, changeName: Boolean = true): String =
    if(changeName) resourceUniqueTableName(resourcePlannedQuery) else resourcePlannedQuery.resource

  def getUserOptions(resourcePlannedQuery: ResourcePlannedQuery): Map[String, String] =
    filterQRMandatoryOptions(resourcePlannedQuery.listProperties).seqToMap

  def getSchemaAndTableName(resourcePlannedQuery: ResourcePlannedQuery) : Option[String] =
    fromMetadataPathString(resourcePlannedQuery.metadataPath).resource

  def getPathFromMetadata(resourcePlannedQuery: ResourcePlannedQuery): Option[String] =
    resourcePlannedQuery.typeResource match {
      case QualityRuleResourceTypeEnum.HDFS =>
        fromMetadataPathString(resourcePlannedQuery.metadataPath).path
      case _ =>
        fromMetadataPathString(resourcePlannedQuery.metadataPath).path.map(_.stripPrefixWithIgnoreCase("/"))
    }


}
