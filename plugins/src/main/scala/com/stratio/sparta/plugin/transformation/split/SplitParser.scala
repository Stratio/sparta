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

package com.stratio.sparta.plugin.transformation.split

import java.io.{Serializable => JSerializable}
import java.util.regex.PatternSyntaxException

import com.stratio.sparta.plugin.transformation.split.SplitParser.SplitMethodEnum.SplitMethod
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import com.stratio.sparta.plugin.transformation.split.SplitParser._

import scala.util.{Failure, Success, Try}

class SplitParser(order: Integer,
                  inputField: Option[String],
                  outputFields: Seq[String],
                  schema: StructType,
                  properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  val splitBy: SplitMethod = SplitMethodEnum.withName(properties.getString("splitMethod", "byRegex").toUpperCase)
  val splitByPattern: Option[String] = Try(Option(properties.getString("byRegexPattern"))).getOrElse(None)
  val splitByChar: Option[String] = Try(Option(properties.getString("byCharPattern"))).getOrElse(None)
  val splitByIndexes: Option[String] = Try(Option(properties.getString("byIndexPattern")))
    .getOrElse(None)
  val discardCharAtSplitIndex: Boolean = Try(properties.getBoolean("excludeIndexes")).getOrElse(false)
  val leaveEmptySubstrings: Int = -1

  //scalastyle:off
  override def parse(row: Row): Seq[Row] = {
    val inputValue = Option(row.get(inputFieldIndex))
    val newData = Try {
      inputValue match {
        case Some(value) => {
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
                  case Failure(e: PatternSyntaxException) => returnWhenError(new IllegalStateException(s" Impossible to split with the provided regex: ${e.getMessage}"))
                  case Failure(e:Exception) => returnWhenError(e)
                }
                case None => returnWhenError(new IllegalStateException(
                  s"Impossible to split inputField ${inputField.get} by regex without providing a regular expression"))
              }
              case SplitMethodEnum.BYCHAR => splitByChar match {
                case Some(stringChar) => Try(splitByCharString(stringChar, valuesParsed, leaveEmptySubstrings)) match {
                  case Success(splitFields) => splitFields
                  case Failure(e: PatternSyntaxException) => returnWhenError(new IllegalStateException(s" Impossible to split with the provided regex: ${e.getMessage}"))
                  case Failure(e:Exception) => returnWhenError(e)
                }
                case None => returnWhenError(new IllegalStateException(
                  s"Impossible to split inputField ${inputField.get} by char without providing a char"))
              }
              case SplitMethodEnum.BYINDEX => splitByIndexes match {
                case Some(listIndexes) =>
                  Try(splitStringByIndexes(listIndexes, valuesParsed, discardCharAtSplitIndex)) match {
                    case Success(splitFields) => splitFields
                    case Failure(e: Exception) => returnWhenError(e)
                  }
                case None => returnWhenError(new IllegalStateException(
                  s"Impossible to split inputField ${inputField.get} by indexes without providing a list of indexes"))
              }
              case _ => returnWhenError(new IllegalStateException(
                s"Impossible to split inputField ${inputField.get} without specifying a splitting method"))
            }

          if (valuesSplit.size == outputFields.size) {
            val splittedWithName = valuesSplit.zip(outputFields).map(_.swap).toMap
            outputFields.map { outputField =>
              val outputSchemaValid = outputFieldsSchema.find(field => field.name == outputField)
              outputSchemaValid match {
                case Some(outSchema) =>
                  splittedWithName.get(outSchema.name) match {
                    case Some(valueParsed) =>
                      parseToOutputType(outSchema, valueParsed)
                    case None =>
                      returnWhenError(new IllegalStateException(
                        s"The values parsed don't contain the schema field: ${outSchema.name}"))
                  }
                case None =>
                  returnWhenError(new IllegalStateException(
                    s"Impossible to parse outputField: $outputField in the schema"))
              }
            }
          } else returnWhenError(new IllegalStateException(s"The number of split values (${valuesSplit.size}) is greater or lower than the output fields (${outputFields.size})"))
        }
        case None =>
          returnWhenError(new IllegalStateException(s"The input value is null or empty"))
      }
    }
    returnData(newData, removeInputField(row))
  }
}

object SplitParser{
  def splitStringByIndexes(stringIndexes: String, stringToSplit: String, discardCharAtSplitIndex: Boolean): Seq[String] = {

    def checkIfValid(indexes: Seq[Int]): Boolean = {
      def monotonicallyIncreasing: Boolean = indexes.zip(indexes.tail).forall{case (x,y) => x < y}
      def monotonicallyIncreasingAndNotUnaryIncrease: Boolean = indexes.zip(indexes.tail).forall{case (x,y) => x+1 < y}
      def restraintsOnIndexes: Boolean =
        if(!discardCharAtSplitIndex) monotonicallyIncreasing
        else monotonicallyIncreasingAndNotUnaryIncrease

      !indexes.exists(_ < 0) && restraintsOnIndexes &&
        indexes.last < stringToSplit.length
    }

    @throws(classOf[IllegalStateException])
    def createListIndexes: Seq[(Int, Int)] = {
      val splittedList= stringIndexes.split(",").map(_.toInt).toSeq
      (checkIfValid(splittedList), discardCharAtSplitIndex) match {
        case (true,false) =>
          if (splittedList.head == 0)
            splittedList.zip(splittedList.tail :+ stringToSplit.length)
          else
            (0 +: splittedList).zip(splittedList :+ stringToSplit.length)
        case (true,true)=>
          if (splittedList.head == 0)
            (splittedList.head+:splittedList.tail.map(_+1)).zip(splittedList.tail :+ stringToSplit.length)
          else
            (0 +: splittedList.map(_+1)).zip(splittedList :+ stringToSplit.length)
        case (false,_) => throw new IllegalStateException(s"The provided indexes list is not valid")
      }
    }

    val tuplesStartEndIndexes= createListIndexes
    tuplesStartEndIndexes.map { case (startIndex, endIndex) => stringToSplit.substring(startIndex, endIndex) }
  }

  @throws(classOf[PatternSyntaxException])
  def splitByCharString(charString: String, stringToSplit: String, limitEmptyField: Int): Seq[String] = {
    val charSplitter = charString.charAt(0)
    val charSlash= """\""".charAt(0)
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