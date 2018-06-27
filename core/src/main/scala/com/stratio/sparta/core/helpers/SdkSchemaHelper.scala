/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.helpers

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.models.PropertySchemasInput
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import scala.util.Try
import scala.util.matching.Regex

object SdkSchemaHelper extends SLF4JLogging{

  val reservedWords = Seq("select", "refresh", "group", "by", "having", "from", "table", "project", "table", "view",
    "exists", "if", "not", "temporary", "replace")
  val discardExtension = "_Discard"


  def isCorrectTableName(tableName: String): Boolean =
    tableName.nonEmpty && tableName != "" &&
      !reservedWords.contains(tableName.toLowerCase) &&
      new Regex("[a-zA-Z0-9_]*").findFirstIn(tableName).forall(matched => matched == tableName)

  def getSchemaFromSession(xDSession: XDSession, tableName: String): Option[StructType] = {
    Try {
      val identifier = xDSession.sessionState.sqlParser.parseTableIdentifier(tableName)
      val schema = xDSession.sessionState.catalog.getTempViewOrPermanentTableMetadata(identifier).schema

      if(schema.fields.nonEmpty) Option(schema) else None
    }.toOption.flatten
  }

  def getSchemaFromRdd(rdd: RDD[Row]): Option[StructType] = if (!rdd.isEmpty()) Option(rdd.first().schema) else None

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

  def discardTableName(name: String) : String = s"$name$discardExtension"
}
