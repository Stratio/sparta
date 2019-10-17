/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.crossdata

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper.HasError
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.workflow.lineage.CrossdataLineage
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization._

import scala.util.Try

abstract class CrossdataInputStep[Underlying[Row]](
                                                    name: String,
                                                    outputOptions: OutputOptions,
                                                    ssc: Option[StreamingContext],
                                                    xDSession: XDSession,
                                                    properties: Map[String, JSerializable]
                                                  )
  extends InputStep[Underlying](name, outputOptions, ssc, xDSession, properties)
    with CrossdataLineage
    with SpartaSerializer
    with SLF4JLogging {

  lazy val maybeCrossdataInfo: Option[Map[String, String]] =
    Try(read[Map[String, String]](properties.getString("crossdata", ""))).toOption

  lazy val query: String = maybeCrossdataInfo match {
    case Some(crossdataInfo) => s"select * from ${crossdataInfo("database")}.${crossdataInfo("table")}"
    case None => throw new RuntimeException("Could not extract Crossdata database and table.")
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    val validationSeq = Seq[(HasError, String)](
      maybeCrossdataInfo.isEmpty -> "You must select a database and a table"
    )
    ErrorValidationsHelper.validate(validationSeq, name)
  }

  override def lineageCatalogProperties(): Map[String, Seq[String]] = getCrossdataLineageProperties(xDSession, query)

}
