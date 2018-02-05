/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.plugin.workflow.transformation.split

import java.io.{Serializable => JSerializable}
import java.util.regex.PatternSyntaxException

import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy.{apply => _}
import com.stratio.sparta.plugin.enumerations.SchemaInputMode._
import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.workflow.transformation.split.SplitTransformStep.SplitMethodEnum.SplitMethod
import com.stratio.sparta.plugin.workflow.transformation.split.SplitTransformStep.{SplitMethodEnum, _}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

abstract class SplitTransformStep[Underlying[Row]](
                                                    name: String,
                                                    outputOptions: OutputOptions,
                                                    transformationStepsManagement: TransformationStepManagement,
                                                    ssc: Option[StreamingContext],
                                                    xDSession: XDSession,
                                                    properties: Map[String, JSerializable]
                                                  )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val splitBy: SplitMethod = SplitMethodEnum.withName(properties.getString("splitMethod", "byRegex").toUpperCase)
  lazy val splitByPattern: Option[String] = Try(Option(properties.getString("byRegexPattern"))).getOrElse(None)
  lazy val splitByChar: Option[String] = Try(Option(properties.getString("byCharPattern"))).getOrElse(None)
  lazy val splitByIndexes: Option[String] = Try(Option(properties.getString("byIndexPattern")))
    .getOrElse(None)
  lazy val discardCharAtSplitIndex: Boolean = Try(properties.getBoolean("excludeIndexes")).getOrElse(false)
  lazy val leaveEmptySubstrings: Int = -1

  lazy val schemaInputMode = SchemaInputMode.withName(properties.getString("schema.inputMode", "FIELDS"))
  lazy val sparkSchema = properties.getString("schema.sparkSchema", None)
  lazy val fieldsModel = properties.getPropertiesFields("schema.fields")
  lazy val inputField = Try(properties.getString("inputField"))
    .getOrElse(throw new IllegalArgumentException("The inputField is mandatory"))
  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)

  lazy val providedSchema: Seq[StructField] =
    (schemaInputMode, sparkSchema, fieldsModel) match {
      case (SPARKFORMAT, Some(schema), _) =>
        schemaFromString(schema).asInstanceOf[StructType].fields.toSeq
      case (FIELDS, _, inputFields) if inputFields.fields.nonEmpty =>
        inputFields.fields.map { fieldModel =>
          val outputType = fieldModel.`type`.notBlank.getOrElse("string")
          StructField(
            name = fieldModel.name,
            dataType = SparkTypes.get(outputType) match {
              case Some(sparkType) => sparkType
              case None => schemaFromString(outputType)
            },
            nullable = fieldModel.nullable.getOrElse(true)
          )
        }
      case _ => throw new Exception("Incorrect schema arguments")
    }

  override def transform(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying] =
    applyHeadTransform(inputData) { (_, inputStream) =>
      inputStream.flatMap(data => parse(data))
    }

  //scalastyle:off
  def parse(row: Row): Seq[Row] =
    returnSeqDataFromRow {
      val inputSchema = row.schema
      val inputValue = Option(row.get(inputSchema.fieldIndex(inputField)))
      val outputSchema = getNewOutputSchema(inputSchema, preservationPolicy, providedSchema, inputField)
      val newData =
        inputValue match {
          case Some(value) =>
            val valuesParsed = {
              value match {
                case valueCast: Array[Byte] => new Predef.String(valueCast)
                case valueCast: String => valueCast
                case _ => value.toString
              }
            }
            val valuesSplit =
              splitBy match {
                case SplitMethodEnum.BYREGEX => splitByPattern match {
                  case Some(pattern) => Try(valuesParsed.split(pattern, leaveEmptySubstrings).toSeq) match {
                    case Success(splitFields) => splitFields
                    case Failure(e: PatternSyntaxException) => returnWhenFieldError(new IllegalStateException(s" Impossible to split with the provided regex: ${e.getMessage}"))
                    case Failure(e: Exception) => returnWhenFieldError(e)
                  }
                  case None =>
                    throw new IllegalStateException(s"Impossible to split inputField $inputField by regex without providing a regular expression")
                }
                case SplitMethodEnum.BYCHAR => splitByChar match {
                  case Some(stringChar) => Try(splitByCharString(stringChar, valuesParsed, leaveEmptySubstrings)) match {
                    case Success(splitFields) => splitFields
                    case Failure(e: PatternSyntaxException) => returnWhenFieldError(new IllegalStateException(s" Impossible to split with the provided regex: ${e.getMessage}"))
                    case Failure(e: Exception) => returnWhenFieldError(e)
                  }
                  case None =>
                    throw new IllegalStateException(s"Impossible to split inputField $inputField by char without providing a char")
                }
                case SplitMethodEnum.BYINDEX => splitByIndexes match {
                  case Some(listIndexes) =>
                    Try(splitStringByIndexes(listIndexes, valuesParsed, discardCharAtSplitIndex)) match {
                      case Success(splitFields) => splitFields
                      case Failure(e: Exception) => returnWhenFieldError(e)
                    }
                  case None =>
                    throw new IllegalStateException(s"Impossible to split inputField $inputField by indexes without providing a list of indexes")
                }
                case _ =>
                  throw new IllegalStateException(s"Impossible to split inputField $inputField without specifying a splitting method")
              }

            if (valuesSplit.length == providedSchema.size) {
              val splitWithName = providedSchema.map(_.name).zip(valuesSplit).toMap

              outputSchema.map { outputField =>
                splitWithName.get(outputField.name) match {
                  case Some(valueParsed) => if (valueParsed != null)
                    castingToOutputSchema(outputField, valueParsed)
                  case None =>
                    Try(row.get(inputSchema.fieldIndex(outputField.name))).getOrElse(
                      returnWhenFieldError(new IllegalStateException(
                        s"The values parsed don't contain the schema field: ${outputField.name}")))
                }
              }
            }
            else throw new IllegalStateException(s"The number of split values (${valuesSplit.size}) is greater or lower than the output fields (${providedSchema.size})")

          case None =>
            throw new IllegalStateException(s"The input value is null or empty")
        }
      new GenericRowWithSchema(newData.toArray, outputSchema)
    }
}

object SplitTransformStep {

  def splitStringByIndexes(stringIndexes: String, stringToSplit: String, discardCharAtSplitIndex: Boolean): Seq[String] = {

    def checkIfValid(indexes: Seq[Int]): Boolean = {
      def monotonicallyIncreasing: Boolean = indexes.zip(indexes.tail).forall { case (x, y) => x < y }

      def monotonicallyIncreasingAndNotUnaryIncrease: Boolean = indexes.zip(indexes.tail).forall { case (x, y) => x + 1 < y }

      def restraintsOnIndexes: Boolean =
        if (!discardCharAtSplitIndex) monotonicallyIncreasing
        else monotonicallyIncreasingAndNotUnaryIncrease

      !indexes.exists(_ < 0) && restraintsOnIndexes &&
        indexes.last < stringToSplit.length
    }

    @throws(classOf[IllegalStateException])
    def createListIndexes: Seq[(Int, Int)] = {
      val splitList = stringIndexes.split(",").map(_.toInt).toSeq
      (checkIfValid(splitList), discardCharAtSplitIndex) match {
        case (true, false) =>
          if (splitList.head == 0)
            splitList.zip(splitList.tail :+ stringToSplit.length)
          else
            (0 +: splitList).zip(splitList :+ stringToSplit.length)
        case (true, true) =>
          if (splitList.head == 0)
            (splitList.head +: splitList.tail.map(_ + 1)).zip(splitList.tail :+ stringToSplit.length)
          else
            (0 +: splitList.map(_ + 1)).zip(splitList :+ stringToSplit.length)
        case (false, _) => throw new IllegalStateException(s"The provided indexes list is not valid")
      }
    }

    val tuplesStartEndIndexes = createListIndexes
    tuplesStartEndIndexes.map { case (startIndex, endIndex) => stringToSplit.substring(startIndex, endIndex) }
  }

  @throws(classOf[PatternSyntaxException])
  def splitByCharString(charString: String, stringToSplit: String, limitEmptyField: Int): Seq[String] = {
    val charSplitter = charString.charAt(0)
    val charSlash = """\""".charAt(0)
    charSplitter match {
      case c if c.equals(charSlash) => stringToSplit.split("""[\\]""", limitEmptyField).toSeq
      case c => stringToSplit.split(s"""[$c]""", limitEmptyField).toSeq
    }

  }

  object SplitMethodEnum extends Enumeration {
    type SplitMethod = Value
    val BYINDEX, BYREGEX, BYCHAR = Value
  }

}

//scalastyle:on