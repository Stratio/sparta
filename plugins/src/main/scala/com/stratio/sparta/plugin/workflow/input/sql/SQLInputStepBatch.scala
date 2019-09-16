/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sql

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.serving.core.workflow.lineage.CrossdataLineage
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

class SQLInputStepBatch(
                         name: String,
                         outputOptions: OutputOptions,
                         ssc: Option[StreamingContext],
                         xDSession: XDSession,
                         properties: Map[String, JSerializable]
                       )
  extends SQLInput[RDD](name, outputOptions, ssc, xDSession, properties)
    with CrossdataLineage
    with SLF4JLogging {

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {
    require(query.nonEmpty, "The input query cannot be empty")
    require(isValidQuery(query), "The input query is not a valid SQL")

    val df = xDSession.sql(query)

    (df.rdd, Option(df.schema))
  }

  override def lineageCatalogProperties(): Map[String, Seq[String]] = getCrossdataLineageProperties(xDSession, query)
}

object SQLInputStepBatch {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }

}
