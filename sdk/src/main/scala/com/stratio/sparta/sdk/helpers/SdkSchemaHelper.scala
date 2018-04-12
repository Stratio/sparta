/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.sdk.helpers

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.models.PropertySchemasInput
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import scala.util.Try
import scala.util.matching.Regex

object SdkSchemaHelper extends SLF4JLogging{

  val reservedWords = Seq("select", "refresh", "group", "by", "having", "from", "table", "project", "table", "view",
    "exists", "if", "not", "temporary", "replace")

  def isCorrectTableName(tableName: String): Boolean =
    tableName.nonEmpty && tableName != "" &&
      !reservedWords.contains(tableName.toLowerCase) &&
      new Regex("[a-zA-Z0-9_]*").findFirstIn(tableName).forall(matched => matched == tableName)

  def getSchemaFromSession(xDSession: XDSession, tableName: String): Option[StructType] = {
    Try {
      val identifier = xDSession.sessionState.sqlParser.parseTableIdentifier(tableName)
      xDSession.sessionState.catalog.getTempViewOrPermanentTableMetadata(identifier).schema
    }.toOption
  }

  def getInputSchemasModel(inputsModel: Option[String]): PropertySchemasInput = {
    {
      implicit val json4sJacksonFormats: Formats =
        DefaultFormats + new JsoneyStringSerializer()
      read[PropertySchemasInput](
        s"""{"inputSchemas": ${
          inputsModel.fold("[]") { values =>
            values.toString
          }
        }}"""
      )
    }
  }
}
