/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.rest

import com.stratio.sparta.plugin.common.rest.BodyFormat.BodyFormat
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.json.RowJsonHelper
import org.apache.spark.sql.types._

import scala.util.matching.Regex

object RestUtils {

  val CustomJsonSuffix: String = "_jsoney"
  val ReplaceableFiledRegex: Regex = regexEnvelope("[a-zA-Z0-9_]+")

  type WithPreprocessing = Boolean
  case class ReplaceableFields(uri: Map[String, WithPreprocessing], body: Map[String, WithPreprocessing])
  object ReplaceableFields{
    val empty = ReplaceableFields(Map.empty, Map.empty)
  }

  private def regexEnvelope(internalPattern: String): Regex = ("\\$\\{" + internalPattern + "\\}").r


  private[plugin] def preProcessingInputFields( uri: String,
                                              body: Option[String],
                                              bodyFormatOpt: Option[BodyFormat],
                                              schema: StructType
                                            ): ReplaceableFields = {



    def isBasicType(schema: DataType): Boolean = schema match {
      case _: BooleanType => true
      case _: NumericType => true
      case _: NullType => true
      case _ => false
    }

    val uriFields = findReplaceableFields(uri)
    val bodyFields = body.map(findReplaceableFields).getOrElse(Set.empty)

    val preprocessingRequired = bodyFormatOpt.collect{ case BodyFormat.JSON => bodyFields.nonEmpty}.getOrElse(false)

    if (preprocessingRequired) {
      val (fieldsWithPreprocessing, fieldsWithoutPreprocessing) =
        schema
          .fields
          .filter(schemaF => bodyFields.contains(schemaF.name))
          .partition(schemaF => !isBasicType(schemaF.dataType)) // TODO nullable false

      val fieldWithPreprocessingNames = fieldsWithPreprocessing.map(_.name).map(_ -> true).toMap
      val fieldWithoutPreprocessingNames = fieldsWithoutPreprocessing.map(_.name).map(_ -> false).toMap

      ReplaceableFields(uriFields.map(_ -> false).toMap, fieldWithPreprocessingNames ++ fieldWithoutPreprocessingNames )
    } else {
      ReplaceableFields(uriFields.map(_ -> false).toMap, bodyFields.map(_ -> false).toMap)
    }
  }

  private[plugin] def replaceInputFields(
                                        inputRow: Row, replaceableFields: Map[String,WithPreprocessing], text: String
                                      ): String = {

    def toJson(name: String, withPreprocessing: WithPreprocessing): String =
      if (withPreprocessing) {
        val fieldIndex = inputRow.fieldIndex(name)

        val newRow = new GenericRowWithSchema(
          Array(inputRow.get(fieldIndex)),
          StructType(inputRow.schema.fields(fieldIndex) :: Nil)
        )
        RowJsonHelper.toValueAsJSON(newRow, Map.empty) // TODO generic

      } else {
        inputRow.get(inputRow.fieldIndex(name)).toString
      }

    (text /: replaceableFields) { case (formattedText, (fieldName, withPreprocessing)) =>
      regexEnvelope(fieldName).replaceAllIn(formattedText, toJson(fieldName, withPreprocessing))
    }
  }

  private[plugin] def findReplaceableFields(text: String): Set[String] = {
    val matches = ReplaceableFiledRegex.findAllIn(text).toSet
    matches.map(str => str.substring(2, str.length - 1))
  }
}