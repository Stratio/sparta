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

import java.sql.Timestamp
import java.util.Date
import scala.util.Try

import com.github.nscala_time.time.Imports._

object TypeOp extends Enumeration {

  type TypeOp = Value
  val Number, BigDecimal, Long, Int, String, Double, Boolean, Binary, Date, DateTime, Timestamp, ArrayDouble,
  ArrayString, MapStringLong = Value

  final val TypeOperationsNames = Map(
    "number" -> TypeOp.Number,
    "bigdecimal" -> TypeOp.BigDecimal,
    "long" -> TypeOp.Long,
    "int" -> TypeOp.Int,
    "string" -> TypeOp.String,
    "double" -> TypeOp.Double,
    "boolean" -> TypeOp.Boolean,
    "binary" -> TypeOp.Binary,
    "date" -> TypeOp.Date,
    "datetime" -> TypeOp.DateTime,
    "timestamp" -> TypeOp.Timestamp,
    "arraydouble" -> TypeOp.ArrayDouble,
    "arraystring" -> TypeOp.ArrayString,
    "mapstringlong" -> TypeOp.MapStringLong
  )

  //scalastyle:off
  def transformValueByTypeOp[T](typeOp: TypeOp, origValue: T): T = {
    typeOp match {
      case TypeOp.String => checkStringType(origValue)
      case TypeOp.Double | TypeOp.Number => checkDoubleType(origValue)
      case TypeOp.Int => checkIntType(origValue)
      case TypeOp.ArrayDouble => checkArrayDoubleType(origValue)
      case TypeOp.ArrayString => checkArrayStringType(origValue)
      case TypeOp.Timestamp => checkTimestampType(origValue)
      case TypeOp.Date => checkDateType(origValue)
      case TypeOp.DateTime => checkDateTimeType(origValue)
      case TypeOp.Long => checkLongType(origValue)
      case TypeOp.MapStringLong => checkMapStringLongType(origValue)
      case _ => origValue
    }
  }

  //scalastyle:on

  private def checkStringType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[String] => value
    case value if value.isInstanceOf[Seq[Any]] =>
      value.asInstanceOf[Seq[Any]].mkString(Output.Separator).asInstanceOf[T]
    case _ => origValue.toString.asInstanceOf[T]
  }

  private def checkArrayDoubleType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Seq[Any]] =>
      Try(value.asInstanceOf[Seq[Any]].map(_.toString.toDouble)).getOrElse(Seq()).asInstanceOf[T]
    case _ => Try(Seq(origValue.toString.toDouble)).getOrElse(Seq()).asInstanceOf[T]
  }

  private def checkMapStringLongType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Map[Any, Any]] =>
      Try(value.asInstanceOf[Map[Any, Any]].map( cast => cast._1.toString -> cast._2.toString.toLong))
        .getOrElse(Map()).asInstanceOf[T]
    case _ => Try(origValue.asInstanceOf[Map[String, Long]]).getOrElse(Map()).asInstanceOf[T]
  }

  private def checkArrayStringType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Seq[Any]] =>
      Try(value.asInstanceOf[Seq[Any]].map(_.toString)).getOrElse(Seq()).asInstanceOf[T]
    case _ => Try(Seq(origValue.toString)).getOrElse(Seq()).asInstanceOf[T]
  }

  private def checkTimestampType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Timestamp] => value
    case value if value.isInstanceOf[Date] =>
      Try(DateOperations.millisToTimeStamp(value.asInstanceOf[Date].getTime))
        .getOrElse(DateOperations.millisToTimeStamp(0L)).asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] =>
      Try(DateOperations.millisToTimeStamp(value.asInstanceOf[DateTime].getMillis))
        .getOrElse(DateOperations.millisToTimeStamp(0L)).asInstanceOf[T]
    case _ => Try(DateOperations.millisToTimeStamp(origValue.toString.toLong))
      .getOrElse(DateOperations.millisToTimeStamp(0L)).asInstanceOf[T]
  }

  private def checkDateType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Date] => value
    case value if value.isInstanceOf[Timestamp] =>
      Try(new Date(value.asInstanceOf[Timestamp].getTime)).getOrElse(new Date(0L)).asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] =>
      Try(new Date(value.asInstanceOf[DateTime].getMillis)).getOrElse(new Date(0L)).asInstanceOf[T]
    case _ => Try(new Date(origValue.toString.toLong)).getOrElse(new Date(0L)).asInstanceOf[T]
  }

  private def checkDateTimeType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[DateTime] => value
    case value if value.isInstanceOf[Timestamp] =>
      Try(new DateTime(value.asInstanceOf[Timestamp].getTime)).getOrElse(new Date(0L)).asInstanceOf[T]
    case value if value.isInstanceOf[Date] =>
      Try(new DateTime(value.asInstanceOf[Date].getTime)).getOrElse(new Date(0L)).asInstanceOf[T]
    case _ => Try(new DateTime(origValue.toString)).getOrElse(new DateTime(0L)).asInstanceOf[T]
  }

  private def checkLongType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Long] => value
    case value if value.isInstanceOf[Double] => Try(origValue.asInstanceOf[Double].toLong).getOrElse(0L).asInstanceOf[T]
    case value if value.isInstanceOf[Int] => Try(origValue.asInstanceOf[Int].toLong).getOrElse(0L).asInstanceOf[T]
    case _ => Try(origValue.toString.toLong).getOrElse(0L).asInstanceOf[T]
  }

  private def checkDoubleType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Double] => value
    case value if value.isInstanceOf[Int] => Try(origValue.asInstanceOf[Int].toDouble).getOrElse(0d).asInstanceOf[T]
    case value if value.isInstanceOf[Long] => Try(origValue.asInstanceOf[Long].toDouble).getOrElse(0d).asInstanceOf[T]
    case _ => Try(origValue.toString.toDouble).getOrElse(0d).asInstanceOf[T]
  }

  private def checkIntType[T](origValue : T) : T = origValue match {
    case value if value.isInstanceOf[Int] => value
    case value if value.isInstanceOf[Double] => Try(origValue.asInstanceOf[Double].toInt).getOrElse(0).asInstanceOf[T]
    case value if value.isInstanceOf[Long] => Try(origValue.asInstanceOf[Long].toInt).getOrElse(0).asInstanceOf[T]
    case _ => Try(origValue.toString.toInt).getOrElse(0).asInstanceOf[T]
  }

  def getTypeOperationByName(nameOperation: String, defaultTypeOperation: TypeOp): TypeOp =
    TypeOperationsNames.getOrElse(nameOperation.toLowerCase, defaultTypeOperation)

}
