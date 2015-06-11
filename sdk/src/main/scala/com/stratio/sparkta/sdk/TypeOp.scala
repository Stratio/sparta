/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.sdk

import java.sql.{Timestamp, Date}
import scala.util.Try

import org.joda.time.DateTime

object TypeOp extends Enumeration {

  type TypeOp = Value
  val BigDecimal, Long, Int, String, Double, Boolean, Binary, Date, DateTime, Timestamp, ArrayDouble,
  ArrayString = Value

  def transformValueByTypeOp[T](typeOp: TypeOp, origValue: T): T = {
    typeOp match {
      case TypeOp.String => checkStringType(origValue)
      case TypeOp.ArrayDouble => checkArrayDoubleType(origValue)
      case TypeOp.ArrayString => checkArrayStringType(origValue)
      case TypeOp.Timestamp => checkTimestampType(origValue)
      case TypeOp.Date => checkDateType(origValue)
      case TypeOp.DateTime => checkDateTimeType(origValue)
      case TypeOp.Long => checkLongType(origValue)
      case _ => origValue
    }
  }

  private def checkStringType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[String] => value
    case value if value.isInstanceOf[Seq[Any]] =>
      value.asInstanceOf[Seq[Any]].mkString(Output.Separator).asInstanceOf[T]
    case _ => origValue.toString.asInstanceOf[T]
  }

  private def checkArrayDoubleType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Seq[Double]] => value
    case value if value.isInstanceOf[Seq[Any]] =>
      Try(value.asInstanceOf[Seq[Any]].map(_.toString.toDouble)).getOrElse(Seq()).asInstanceOf[T]
    case _ => Try(Seq(origValue.toString.toDouble)).getOrElse(Seq()).asInstanceOf[T]
  }

  private def checkArrayStringType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Seq[String]] => value
    case value if value.isInstanceOf[Seq[Any]] =>
      Try(value.asInstanceOf[Seq[Any]].map(_.toString)).getOrElse(Seq()).asInstanceOf[T]
    case _ => Try(Seq(origValue.toString)).getOrElse(Seq()).asInstanceOf[T]
  }

  private def checkTimestampType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Timestamp] => value
    case value if value.isInstanceOf[Long] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[Long]).asInstanceOf[T]
    case value if value.isInstanceOf[Date] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[Date].getTime).asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[DateTime].getMillis).asInstanceOf[T]
    case _ => Try(DateOperations.millisToTimeStamp(origValue.toString.toLong))
      .getOrElse(DateOperations.millisToTimeStamp(0L)).asInstanceOf[T]
  }

  private def checkDateType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Date] => value
    case value if value.isInstanceOf[Long] => new Date(value.asInstanceOf[Long]).asInstanceOf[T]
    case value if value.isInstanceOf[Date] => value.asInstanceOf[Date].asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] => value.asInstanceOf[DateTime].toDate.asInstanceOf[T]
    case _ => Try(new Date(origValue.toString.toLong)).getOrElse(new Date(0L)).asInstanceOf[T]
  }

  private def checkDateTimeType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[DateTime] => value
    case value if value.isInstanceOf[Long] => new DateTime(value.asInstanceOf[Long]).asInstanceOf[T]
    case value if value.isInstanceOf[Date] => new DateTime(value.asInstanceOf[Date]).asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] => value.asInstanceOf[DateTime].asInstanceOf[T]
    case _ => Try(new DateTime(origValue.toString)).getOrElse(new DateTime(0L)).asInstanceOf[T]
  }

  private def checkLongType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Long] => value
    case value if value.isInstanceOf[Timestamp] => value.asInstanceOf[Timestamp].getTime.asInstanceOf[T]
    case value if value.isInstanceOf[Date] => value.asInstanceOf[Date].getTime.asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] => value.asInstanceOf[Date].getTime.asInstanceOf[T]
    case _ => Try(origValue.toString.toLong).getOrElse(0L).asInstanceOf[T]
  }

  //scalastyle:off
  def getTypeOperationByName(nameOperation: String, defaultTypeOperation: TypeOp): TypeOp =
    nameOperation.toLowerCase match {
      case name if name == "bigdecimal" => TypeOp.BigDecimal
      case name if name == "long" => TypeOp.Long
      case name if name == "int" => TypeOp.Int
      case name if name == "string" => TypeOp.String
      case name if name == "double" => TypeOp.Double
      case name if name == "boolean" => TypeOp.Boolean
      case name if name == "binary" => TypeOp.Binary
      case name if name == "date" => TypeOp.Date
      case name if name == "datetime" => TypeOp.DateTime
      case name if name == "timestamp" => TypeOp.Timestamp
      case name if name == "arraydouble" => TypeOp.ArrayDouble
      case name if name == "arraystring" => TypeOp.ArrayString
      case _ => defaultTypeOperation
    }

  //scalastyle:on
}
