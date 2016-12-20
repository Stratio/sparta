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
package com.stratio.sparta.sdk

import java.sql.Timestamp
import java.util.Date

import com.github.nscala_time.time.Imports._
import org.apache.spark.sql.types._

import scala.util.Try

object TypeOp extends Enumeration {

  type TypeOp = Value
  val Number, BigDecimal, Long, Int, String, Double, Boolean, Binary, Date, DateTime, Timestamp, ArrayDouble,
  ArrayString, MapStringLong, MapStringDouble, Any = Value

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
    "mapstringany" -> TypeOp.MapStringDouble,
    "any" -> TypeOp.Any
  )

  def getTypeOperationByName(nameOperation: String, defaultTypeOperation: TypeOp): TypeOp =
    TypeOperationsNames.getOrElse(nameOperation.toLowerCase, defaultTypeOperation)

  implicit object OrderingAny extends Ordering[Any] {
    import math.Ordering
    override def compare(x: Any, y: Any): Int = (x, y) match {
      case (x: Int, y: Int) => Ordering[Int].compare(x, y)
      case (x: Double, y: Double) => Ordering[Double].compare(x, y)
      case (x: String, y: String) => Ordering[String].compare(x, y)
      case (x: Long, y: Long) => Ordering[Long].compare(x, y)
      case (x: Boolean, y: Boolean) => Ordering[Boolean].compare(x, y)
      case (x: Date, y: Date) => Ordering[Date].compare(x, y)
      case (x: Timestamp, y: Timestamp) => Ordering[Long].compare(x.getTime, y.getTime)
      case (x: DateTime, y: DateTime) => Ordering[DateTime].compare(x, y)
      case _ => throw new Exception(s"Incompatible types when comparing: ${x.toString} and ${y.toString}")
    }
  }

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
      case TypeOp.Any => origValue
      case _ => origValue
    }
  }

  def transformAnyByTypeOp(typeOp: TypeOp, origValue: Any): Any = {
    typeOp match {
      case TypeOp.String => checkAnyStringType(origValue)
      case TypeOp.Double | TypeOp.Number => checkAnyDoubleType(origValue)
      case TypeOp.Int => checkAnyIntType(origValue)
      case TypeOp.ArrayDouble => checkAnyArrayDoubleType(origValue)
      case TypeOp.ArrayString => checkAnyArrayStringType(origValue)
      case TypeOp.Timestamp => checkAnyTimestampType(origValue)
      case TypeOp.Date => checkAnyDateType(origValue)
      case TypeOp.DateTime => checkAnyDateTimeType(origValue)
      case TypeOp.Long => checkAnyLongType(origValue)
      case TypeOp.MapStringLong => checkAnyMapStringLongType(origValue)
      case TypeOp.MapStringDouble => checkAnyMapStringDoubleType(origValue)
      case TypeOp.Boolean => checkAnyBooleanType(origValue)
      case TypeOp.Any => origValue
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

  private def checkStringType[T](origValue: T): T = checkAnyStringType(origValue).asInstanceOf[T]

  private def checkAnyStringType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[String] => value
    case value if value.isInstanceOf[Seq[Any]] => value.asInstanceOf[Seq[Any]].mkString(Output.Separator)
    case _ => origValue.toString
  }

  private def checkArrayDoubleType[T](origValue: T): T = checkAnyArrayDoubleType(origValue).asInstanceOf[T]

  private def checkAnyArrayDoubleType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Seq[Any]] => value.asInstanceOf[Seq[Any]].map(_.toString.toDouble)
    case _ => Seq(origValue.toString.toDouble)
  }

  private def checkMapStringLongType[T](origValue: T): T = checkAnyMapStringLongType(origValue).asInstanceOf[T]

  private def checkAnyMapStringLongType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Map[Any, Any]] =>
      value.asInstanceOf[Map[Any, Any]].map(cast => cast._1.toString -> cast._2.toString.toLong)
    case _ =>
      origValue.asInstanceOf[Map[String, Long]]
  }

  private def checkMapStringDoubleType[T](origValue: T): T = checkAnyMapStringDoubleType(origValue).asInstanceOf[T]

  private def checkAnyMapStringDoubleType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Map[Any, Any]] =>
      value.asInstanceOf[Map[Any, Any]].map(cast => cast._1.toString -> cast._2.toString.toDouble)
    case _ =>
      origValue.asInstanceOf[Map[String, Double]]
  }

  private def checkArrayStringType[T](origValue: T): T = checkAnyArrayStringType(origValue).asInstanceOf[T]

  private def checkAnyArrayStringType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Seq[Any]] => value.asInstanceOf[Seq[Any]].map(_.toString)
    case _ => Seq(origValue.toString)
  }

  private def checkTimestampType[T](origValue: T): T = checkAnyTimestampType(origValue).asInstanceOf[T]

  private def checkAnyTimestampType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Timestamp] =>
      value
    case value if value.isInstanceOf[Date] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[Date].getTime)
    case value if value.isInstanceOf[DateTime] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[DateTime].getMillis)
    case value if value.isInstanceOf[Long] =>
      DateOperations.millisToTimeStamp(value.asInstanceOf[Long])
    case _ =>
      DateOperations.millisToTimeStamp(origValue.toString.toLong)
  }

  private def checkDateType[T](origValue: T): T = checkAnyDateType(origValue).asInstanceOf[T]

  private def checkAnyDateType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Date] => value
    case value if value.isInstanceOf[Timestamp] => new Date(value.asInstanceOf[Timestamp].getTime)
    case value if value.isInstanceOf[DateTime] => new Date(value.asInstanceOf[DateTime].getMillis)
    case value if value.isInstanceOf[Long] => new Date(value.asInstanceOf[Long])
    case _ => new Date(origValue.toString.toLong)
  }

  private def checkDateTimeType[T](origValue: T): T = checkAnyDateTimeType(origValue).asInstanceOf[T]

  private def checkAnyDateTimeType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[DateTime] => value
    case value if value.isInstanceOf[Timestamp] => new DateTime(value.asInstanceOf[Timestamp].getTime)
    case value if value.isInstanceOf[Date] => new DateTime(value.asInstanceOf[Date].getTime)
    case value if value.isInstanceOf[Long] => new DateTime(value.asInstanceOf[Long])
    case _ => new DateTime(origValue.toString)
  }

  private def checkLongType[T](origValue: T): T = checkAnyLongType(origValue).asInstanceOf[T]

  //scalastyle:off
  private def checkAnyLongType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Long] => value
    case value if value.isInstanceOf[Double] => origValue.asInstanceOf[Double].toLong
    case value if value.isInstanceOf[Short] => origValue.asInstanceOf[Short].toLong
    case value if value.isInstanceOf[Float] => origValue.asInstanceOf[Float].toLong
    case value if value.isInstanceOf[Int] => origValue.asInstanceOf[Int].toLong
    case value if value.isInstanceOf[Number] => origValue.asInstanceOf[Number].longValue()
    case value if value.isInstanceOf[DateTime] => origValue.asInstanceOf[DateTime].getMillis
    case value if value.isInstanceOf[Timestamp] => origValue.asInstanceOf[Timestamp].getTime
    case value if value.isInstanceOf[Date] => origValue.asInstanceOf[Date].getTime
    case _ => origValue.toString.toLong
  }

  private def checkDoubleType[T](origValue: T): T = checkAnyDoubleType(origValue).asInstanceOf[T]

  private def checkAnyDoubleType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Double] => value
    case value if value.isInstanceOf[Int] => origValue.asInstanceOf[Int].toDouble
    case value if value.isInstanceOf[Short] => origValue.asInstanceOf[Short].toDouble
    case value if value.isInstanceOf[Float] => origValue.asInstanceOf[Float].toDouble
    case value if value.isInstanceOf[Long] => origValue.asInstanceOf[Long].toDouble
    case value if value.isInstanceOf[Number] => origValue.asInstanceOf[Number].doubleValue()
    case _ => origValue.toString.toDouble
  }

  private def checkIntType[T](origValue: T): T = checkAnyIntType(origValue).asInstanceOf[T]

  private def checkAnyIntType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Int] => value
    case value if value.isInstanceOf[Double] => origValue.asInstanceOf[Double].toInt
    case value if value.isInstanceOf[Short] => origValue.asInstanceOf[Short].toInt
    case value if value.isInstanceOf[Float] => origValue.asInstanceOf[Float].toInt
    case value if value.isInstanceOf[Long] => origValue.asInstanceOf[Long].toInt
    case value if value.isInstanceOf[Number] => origValue.asInstanceOf[Number].intValue()
    case _ => origValue.toString.toInt
  }
  //scalastyle:on

  private def checkBooleanType[T](origValue: T): T = checkAnyBooleanType(origValue).asInstanceOf[T]

  private def checkAnyBooleanType(origValue: Any): Any = origValue match {
    case value if value.isInstanceOf[Boolean] => value
    case _ => origValue.toString.toBoolean
  }
}
