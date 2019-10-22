/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models.qualityrule.resources

import com.stratio.sparta.core.models.ResourcePlannedQuery

case class CrossdataResource(collectionName: String) extends ResourceQR{
  override def getSQLQuery(xdSessionConf: Map[String, String]) : String =
    s"REFRESH EXTENDED COLLECTION $collectionName"
}

object CrossdataResource extends ResourceQRUtils {
  def apply(resourcePlannedQuery: ResourcePlannedQuery, changeName: Boolean ): Option[CrossdataResource] = {
    val collectionNameFromMetadata = getPathFromMetadata(resourcePlannedQuery)
    collectionNameFromMetadata.map( collectionName => CrossdataResource(collectionName))
  }
}
