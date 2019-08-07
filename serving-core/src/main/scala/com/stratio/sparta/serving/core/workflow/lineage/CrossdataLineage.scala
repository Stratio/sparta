/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.workflow.lineage

import com.stratio.sparta.core.constants.SdkConstants._
import org.apache.spark.sql.crossdata.XDSession

trait CrossdataLineage {

  case class LineageTable(tableName: String, metadataPath: String )

  def getCrossdataLineageProperties(xDSession: XDSession, query: String): Map[String, Seq[String]] = {
    Map(
      ProvidedMetadatapathKey -> xDSession.getLineageSql(query).map(_.metadataPath)
    )
  }

  def getCrossdataLineageProperties: Map[String, Seq[String]] = {
    Map(
      ProvidedMetadatapathKey -> Seq.empty
    )
  }
}