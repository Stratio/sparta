/**
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
package com.stratio.sparta.sdk

import java.sql.Timestamp
import java.util.Date

import com.github.nscala_time.time.Imports._
import org.apache.spark.sql.types._

import scala.util.Try

object TypeOp extends Enumeration {

  type TypeOp = Value
  val Number, BigDecimal, Long, Int, String, Double, Boolean, Binary, Date, DateTime, Timestamp, ArrayDouble,
  ArrayString, MapStringLong, MapStringDouble = Value

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
    "mapstringlong" -> TypeOp.MapStringLong,
    "mapstringany" -> TypeOp.MapStringDouble
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
      case TypeOp.MapStringDouble => checkMapStringDoubleType(origValue)
      case TypeOp.Boolean => checkBooleanType(origValue)
      case _ => origValue
    }
  }

  def transformValueByTypeOp[T](typeOp: DataType, origValue: T): T = {
    typeOp match {
      case StringType => checkStringType(origValue)
      case DoubleType => checkDoubleType(origValue)
      case IntegerType => checkIntType(origValue)
      case TimestampType => checkTimestampType(origValue)
      case DateType => checkDateType(origValue)
      case LongType => checkLongType(origValue)
      case BooleanType => checkBooleanType(origValue)
      case _ => origValue
    }
  }

  //scalastyle:on

  private def checkStringType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[String] =>
      value
    case value if value.isInstanceOf[Seq[Any]] =>
      value.asInstanceOf[Seq[Any]].mkString(Output.Separator).asInstanceOf[T]
    case _ =>
      origValue.toString.asInstanceOf[T]
  }

  private def checkArrayDoubleType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Seq[Any]] =>
      value.asInstanceOf[Seq[Any]].map(_.toString.toDouble).asInstanceOf[T]
    case _ =>
      Seq(origValue.toString.toDouble).asInstanceOf[T]
  }

  private def checkMapStringLongType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Map[Any, Any]] =>
      value.asInstanceOf[Map[Any, Any]].map(cast => cast._1.toString -> cast._2.toString.toLong).asInstanceOf[T]
    case _ =>
      origValue.asInstanceOf[Map[String, Long]].asInstanceOf[T]
  }

  private def checkMapStringDoubleType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Map[Any, Any]] =>
      value.asInstanceOf[Map[Any, Any]].map(cast => cast._1.toString -> cast._2.toString.toDouble).asInstanceOf[T]
    case _ =>
      origValue.asInstanceOf[Map[String, Double]].asInstanceOf[T]
  }

  private def checkArrayStringType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Seq[Any]] =>
      value.asInstanceOf[Seq[Any]].map(_.toString).asInstanceOf[T]
    case _ =>
      Seq(origValue.toString).asInstanceOf[T]
  }

  private def checkTimestampType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Timestamp] =>
      value
    case value if value.isInstanceOf[Date] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[Date].getTime).asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[DateTime].getMillis).asInstanceOf[T]
    case value if value.isInstanceOf[Long] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[Long]).asInstanceOf[T]
    case _ =>
      DateOperations.millisToTimeStamp(origValue.toString.toLong).asInstanceOf[T]
  }

  private def checkDateType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Date] =>
      value
    case value if value.isInstanceOf[Timestamp] =>
      new Date(value.asInstanceOf[Timestamp].getTime).asInstanceOf[T]
    case value if value.isInstanceOf[DateTime] =>
      new Date(value.asInstanceOf[DateTime].getMillis).asInstanceOf[T]
    case value if value.isInstanceOf[Long] =>
      new Date(value.asInstanceOf[Long]).asInstanceOf[T]
    case _ =>
      new Date(origValue.toString.toLong).asInstanceOf[T]
  }

  private def checkDateTimeType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[DateTime] =>
      value
    case value if value.isInstanceOf[Timestamp] =>
      new DateTime(value.asInstanceOf[Timestamp].getTime).asInstanceOf[T]
    case value if value.isInstanceOf[Date] =>
      new DateTime(value.asInstanceOf[Date].getTime).asInstanceOf[T]
    case value if value.isInstanceOf[Long] =>
      new DateTime(value.asInstanceOf[Long]).asInstanceOf[T]
    case _ =>
      new DateTime(origValue.toString).asInstanceOf[T]
  }

  //scalastyle:off
  private def checkLongType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Long] =>
      value
    case value if value.isInstanceOf[Double] =>
      origValue.asInstanceOf[Double].toLong.asInstanceOf[T]
    case value if value.isInstanceOf[Short] =>
      origValue.asInstanceOf[Short].toLong.asInstanceOf[T]
    case value if value.isInstanceOf[Float] =>
      origValue.asInstanceOf[Float].toLong.asInstanceOf[T]
    case value if value.isInstanceOf[Int] =>
      origValue.asInstanceOf[Int].toLong.asInstanceOf[T]
    case value if value.isInstanceOf[Number] =>
      origValue.asInstanceOf[Number].longValue().asInstanceOf[T]
    case _ =>
      origValue.toString.toLong.asInstanceOf[T]
  }

  private def checkDoubleType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Double] =>
      value
    case value if value.isInstanceOf[Int] =>
      origValue.asInstanceOf[Int].toDouble.asInstanceOf[T]
    case value if value.isInstanceOf[Short] =>
      origValue.asInstanceOf[Short].toDouble.asInstanceOf[T]
    case value if value.isInstanceOf[Float] =>
      origValue.asInstanceOf[Float].toDouble.asInstanceOf[T]
    case value if value.isInstanceOf[Long] =>
      origValue.asInstanceOf[Long].toDouble.asInstanceOf[T]
    case value if value.isInstanceOf[Number] =>
      origValue.asInstanceOf[Number].doubleValue().asInstanceOf[T]
    case _ =>
      origValue.toString.toDouble.asInstanceOf[T]
  }

  private def checkIntType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Int] =>
      value
    case value if value.isInstanceOf[Double] =>
      origValue.asInstanceOf[Double].toInt.asInstanceOf[T]
    case value if value.isInstanceOf[Short] =>
      origValue.asInstanceOf[Short].toInt.asInstanceOf[T]
    case value if value.isInstanceOf[Float] =>
      origValue.asInstanceOf[Float].toInt.asInstanceOf[T]
    case value if value.isInstanceOf[Long] =>
      origValue.asInstanceOf[Long].toInt.asInstanceOf[T]
    case value if value.isInstanceOf[Number] =>
      origValue.asInstanceOf[Number].intValue().asInstanceOf[T]
    case _ =>
      origValue.toString.toInt.asInstanceOf[T]
  }
  //scalastyle:on

  private def checkBooleanType[T](origValue: T): T = origValue match {
    case value if value.isInstanceOf[Boolean] =>
      value
    case _ =>
      origValue.toString.toBoolean.asInstanceOf[T]
  }

  def getTypeOperationByName(nameOperation: String, defaultTypeOperation: TypeOp): TypeOp =
    TypeOperationsNames.getOrElse(nameOperation.toLowerCase, defaultTypeOperation)
}
