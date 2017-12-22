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

package com.stratio.sparta.sdk.utils

import java.nio.charset.Charset
import java.sql.Timestamp
import java.util
import java.util.Date

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.sdk.utils.AggregationTimeUtils._
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StructType, _}
import org.json4s.JDecimal
import org.json4s.JsonAST.{JBool, JDouble, JInt, JString}

import scala.collection.JavaConverters._

//scalastyle:off
object CastingUtils {

  /**
    * TODO Refactor all functions names, change to privates and build tests
    * check pattern matching without 'value if value.isInstanceOf[X]' and 'value : X'
    * check if we can change [_, _] to [Any, Any]
    */

  def castingToSchemaType(dataTypeToCast: DataType, origValue: Any): Any = {
    dataTypeToCast match {
      case IntegerType => checkIntType(origValue)
      case LongType => checkLongType(origValue)
      case DoubleType => checkDoubleType(origValue)
      case ByteType => checkByteType(origValue)
      case ShortType => checkShortType(origValue)
      case FloatType => checkFloatType(origValue)
      case DateType => checkDateType(origValue)
      case TimestampType => checkTimestampType(origValue)
      case BinaryType => checkArrayByteType(origValue)
      case StringType => checkStringType(origValue)
      case BooleanType => checkBooleanType(origValue)
      case ArrayType(_: DoubleType, _) => checkArrayDoubleType(origValue)
      case ArrayType(_: StringType, _) => checkArrayStringType(origValue)
      case ArrayType(_: LongType, _) => checkArrayLongType(origValue)
      case ArrayType(_: IntegerType, _) => checkArrayIntType(origValue)
      case ArrayType(elemType: MapType, _)
        if elemType.keyType.isInstanceOf[StringType] & elemType.valueType.isInstanceOf[StringType] =>
        checkArrayMapStringStringType(origValue)
      case ArrayType(_: StructType, _) => checkArrayStructType(origValue)
      case MapType(_: StringType, _: StringType, _) => checkMapStringStringType(origValue)
      case MapType(_: StringType, _: LongType, _) => checkMapStringLongType(origValue)
      case MapType(_: StringType, _: IntegerType, _) => checkMapStringIntType(origValue)
      case MapType(_: StringType, _: DoubleType, _) => checkMapStringDoubleType(origValue)
      case MapType(_: StringType, _: StructType, _) => checkMapStringStructType(origValue)
      case _: StructType => checkStructType(origValue)
      case _ => origValue
    }
  }

  /**
    * Obtains the Spark SQL type based into the input value. The map types are passed to GenericRowWithSchema-StructType
    *
    * @param valueToCheck value to check
    * @return The Spark type
    */
  def valueToSparkType(valueToCheck: Any): DataType =
    valueToCheck match {
      case _: Int => IntegerType
      case _: Long => LongType
      case _: Double => DoubleType
      case _: Byte => ByteType
      case _: Short => ShortType
      case _: Float => FloatType
      case _: java.sql.Timestamp => TimestampType
      case _: java.sql.Date => DateType
      case _: Date => DateType
      case _: Array[Byte] => BinaryType
      case _: String => StringType
      case _: Boolean => BooleanType
      case value: Seq[_] => arrayValuesToSparkType(value)
      case value: Map[_, _] => mapValuesToStructType(value)
      case value: GenericRowWithSchema => value.schema
      case _ => StringType
    }

  /**
    * Obtains the ArrayType associated to one array value
    *
    * @param valueToCheck The array value to check
    * @return The ArrayType with the fields type
    */
  def arrayValuesToSparkType(valueToCheck: Seq[_]): ArrayType = {
    valueToCheck.headOption match {
      case Some(firstElement) => ArrayType(valueToSparkType(firstElement))
      case None => ArrayType(StringType)
    }
  }

  /**
    * Obtains the StructType associated to one Map value
    *
    * @param valueToCheck The map value to check
    * @return The StructType with the fields
    */
  def mapValuesToStructType(valueToCheck: Map[_, _]): StructType =
    StructType(valueToCheck.map(value => StructField(value._1.toString, valueToSparkType(value._2))).toSeq)

  /* PRIVATE METHODS */

  /* STRUCT TYPE -> GENERIC ROW WITH SCHEMA */

  def checkStructType(origValue: Any): Any =
    origValue match {
      case value: util.Map[_, _] =>
        val inputValue = value.asScala.toMap.mapValues(checkStructType)
        new GenericRowWithSchema(inputValue.values.toArray, mapValuesToStructType(inputValue))
      case value: Map[_, _] =>
        val inputValue = value.mapValues(checkStructType)
        new GenericRowWithSchema(inputValue.values.toArray, mapValuesToStructType(inputValue))
      case value: GenericRowWithSchema => value
      case value if value == null => null
      case _ => origValue
    }

  /* ARRAY TYPES */

  def checkArrayStructType(origValue: Any): Any =
    origValue match {
      case value: util.List[util.Map[_, _]] => value.asScala.map(mapValues => checkStructType(mapValues))
      case value: Seq[_] => value.headOption match {
        case Some(_: Map[_, _]) => value.map(mapValues => checkStructType(mapValues))
        case Some(_: GenericRowWithSchema) => value
        case _ => throw new Exception("Impossible to casting Array of StructTypes")
      }
      case value if value == null => null
      case _ => origValue.asInstanceOf[Seq[GenericRowWithSchema]]
    }

  def checkArrayStringType(origValue: Any): Any =
    origValue match {
      case value: util.List[_] => value.asScala.map(value => if (value == null) null else value.toString)
      case value: Seq[_] => value.map(value => if (value == null) null else value.toString)
      case value if value == null => null
      case _ => Seq(origValue.toString)
    }

  def checkArrayIntType(origValue: Any): Any =
    origValue match {
      case value: util.List[_] => value.asScala.map(value => if (value == null) null else value.toString.toInt)
      case value: Seq[_] => value.map(value => if (value == null) null else value.toString.toInt)
      case value if value == null => null
      case _ => Seq(origValue.toString.toInt)
    }

  def checkArrayLongType(origValue: Any): Any =
    origValue match {
      case value: util.List[_] => value.asScala.map(value => if (value == null) null else value.toString.toLong)
      case value: Seq[_] => value.map(value => if (value == null) null else value.toString.toLong)
      case value if value == null => null
      case _ => Seq(origValue.toString.toLong)
    }

  def checkArrayDoubleType(origValue: Any): Any =
    origValue match {
      case value: util.List[_] => value.asScala.map(value => if (value == null) null else value.toString.toDouble)
      case value: Seq[_] => value.map(value => if (value == null) null else value.toString.toDouble)
      case value if value == null => null
      case _ => Seq(origValue.toString.toDouble)
    }

  def checkArrayMapStringStringType(origValue: Any): Any =
    origValue match {
      case value: util.List[util.Map[_, _]] =>
        value.asScala.map { value =>
          value.asScala.map { cast =>
            cast._1.toString -> {
              if (cast._2 == null) null else cast._2.toString
            }
          }.toMap
        }
      case value: Seq[_] => value.headOption match {
        case Some(_: Map[_, _]) => value.asInstanceOf[Seq[Map[_, _]]].map(_.map(cast =>
          cast._1.toString -> {
            if (cast._2 == null) null else cast._2.toString
          }))
        case Some(_: GenericRowWithSchema) =>
          value.asInstanceOf[Seq[GenericRowWithSchema]].map(row => row.getValuesMap[String](row.schema.fieldNames))
        case _ => throw new Exception("Impossible to casting Array of Strings")
      }
      case value if value == null => null
      case _ => origValue.asInstanceOf[Seq[Map[String, String]]]
    }

  /* MAP TYPES */

  def checkMapStringStructType(origValue: Any): Any =
    origValue match {
      case value: util.Map[_, util.Map[_, _]] => value.asScala.map(values => values._1.toString -> checkStructType(values._2))
      case value: Map[_, _] => value.headOption match {
        case Some((_, _: Map[_, _])) => value.map(values => values._1.toString -> checkStructType(values._2))
        case Some((_, _: GenericRowWithSchema)) => value
        case _ => throw new Exception("Impossible to casting Map of StructType")
      }
      case value if value == null => null
      case _ => origValue.asInstanceOf[Map[String, GenericRowWithSchema]]
    }

  def checkMapStringStringType(origValue: Any): Any =
    origValue match {
      case value: util.Map[_, _] => value.asScala.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString
        }).toMap
      case value: Map[_, _] => value.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString
        })
      case value: GenericRowWithSchema => value.getValuesMap[String](value.schema.fieldNames)
      case value if value == null => null
      case _ => origValue.asInstanceOf[Map[String, String]]
    }

  def checkMapStringIntType(origValue: Any): Any =
    origValue match {
      case value: util.Map[_, _] => value.asScala.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString.toInt
        }).toMap
      case value: Map[_, _] => value.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString.toInt
        })
      case value: GenericRowWithSchema => value.getValuesMap[Int](value.schema.fieldNames)
      case value if value == null => null
      case _ => origValue.asInstanceOf[Map[String, Int]]
    }

  def checkMapStringLongType(origValue: Any): Any =
    origValue match {
      case value: util.Map[_, _] => value.asScala.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString.toLong
        }).toMap
      case value: Map[_, _] => value.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString.toLong
        })
      case value: GenericRowWithSchema => value.getValuesMap[Long](value.schema.fieldNames)
      case value if value == null => null
      case _ => origValue.asInstanceOf[Map[String, Long]]
    }

  def checkMapStringDoubleType(origValue: Any): Any =
    origValue match {
      case value: util.Map[_, _] => value.asScala.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString.toDouble
        }).toMap
      case value: Map[_, _] => value.map(cast =>
        cast._1.toString -> {
          if (cast._2 == null) null else cast._2.toString.toDouble
        })
      case value: GenericRowWithSchema => value.getValuesMap[Double](value.schema.fieldNames)
      case value if value == null => null
      case _ => origValue.asInstanceOf[Map[String, Double]]
    }

  /* PRIMITIVE TYPES */

  //TODO Change all if value.isInstanceOf[_] in primitive types

  def checkArrayByteType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[String] => value.asInstanceOf[String].getBytes(Charset.forName("UTF-8"))
      case value if value.isInstanceOf[Array[Byte]] => value
      case value if value.isInstanceOf[JString] => value.asInstanceOf[JString].s.getBytes(Charset.forName("UTF-8"))
      case value if value.isInstanceOf[Seq[Any]] => value.asInstanceOf[Seq[Any]].mkString(",").getBytes(Charset.forName("UTF-8"))
      case value if value == null => null
      case _ => origValue.toString.getBytes(Charset.forName("UTF-8"))
    }

  def checkStringType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[String] => value
      case value if value.isInstanceOf[Array[Byte]] => new Predef.String(value.asInstanceOf[Array[Byte]])
      case value if value.isInstanceOf[JString] => value.asInstanceOf[JString].s
      case value if value.isInstanceOf[Seq[Any]] => value.asInstanceOf[Seq[Any]].mkString(",")
      case value if value == null => null
      case _ => origValue.toString
    }

  def checkTimestampType(origValue: Any): Any =
    origValue match {
      case value: Timestamp => value
      case value: Date => millisToTimeStamp(value.getTime)
      case value: java.sql.Date => millisToTimeStamp(value.getTime)
      case value: DateTime => millisToTimeStamp(value.getMillis)
      case value: Long => millisToTimeStamp(value)
      case value: JString => millisToTimeStamp(value.s.toLong)
      case value: Array[Byte] => millisToTimeStamp(new Predef.String(value).toLong)
      case value if value == null => null
      case _ => millisToTimeStamp(origValue.toString.toLong)
    }

  def checkDateType(origValue: Any): Any =
    origValue match {
      case value: java.sql.Date => value
      case value: Date => new java.sql.Date(value.getTime)
      case value: Timestamp => new java.sql.Date(value.getTime)
      case value: DateTime => new java.sql.Date(value.getMillis)
      case value: Long => new java.sql.Date(value)
      case value: JString => new java.sql.Date(new Date(value.s).getTime)
      case value: Array[Byte] => new java.sql.Date(new Date(new Predef.String(value)).getTime)
      case value if value == null => null
      case _ => new java.sql.Date(origValue.toString.toLong)
    }

  def checkLongType(origValue: Any): Any =
    origValue match {
      case value: Long => value
      case value: JInt => value.num.longValue()
      case value: JDouble => value.num.longValue()
      case value: JDecimal => value.num.longValue()
      case value: Double => value.toLong
      case value: Short => value.toLong
      case value: Float => value.toLong
      case value: Int => value.toLong
      case value: Number => value.longValue()
      case value: DateTime => value.getMillis
      case value: Timestamp => value.getTime
      case value: Date => value.getTime
      case value: java.sql.Date => value.getTime
      case value: JString => value.s.toLong
      case value: Array[Byte] => new Predef.String(value).toLong
      case value if value == null => null
      case _ => origValue.toString.toLong
    }

  def checkDoubleType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[Double] => value
      case value if value.isInstanceOf[JDouble] => value.asInstanceOf[JDouble].num
      case value if value.isInstanceOf[JInt] => value.asInstanceOf[JInt].num.doubleValue()
      case value if value.isInstanceOf[JDecimal] => value.asInstanceOf[JDecimal].num.doubleValue()
      case value if value.isInstanceOf[Int] => origValue.asInstanceOf[Int].toDouble
      case value if value.isInstanceOf[Short] => origValue.asInstanceOf[Short].toDouble
      case value if value.isInstanceOf[Float] => origValue.asInstanceOf[Float].toDouble
      case value if value.isInstanceOf[Long] => origValue.asInstanceOf[Long].toDouble
      case value if value.isInstanceOf[Number] => origValue.asInstanceOf[Number].doubleValue()
      case value if value.isInstanceOf[JString] => origValue.asInstanceOf[JString].s.toDouble
      case value if value.isInstanceOf[Array[Byte]] => new Predef.String(origValue.asInstanceOf[Array[Byte]]).toDouble
      case value if value == null => null
      case _ => origValue.toString.toDouble
    }

  def checkShortType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[Short] => value
      case value if value.isInstanceOf[Double] => origValue.asInstanceOf[Double].toShort
      case value if value.isInstanceOf[JDouble] => value.asInstanceOf[JDouble].num.shortValue()
      case value if value.isInstanceOf[JInt] => value.asInstanceOf[JInt].num.shortValue()
      case value if value.isInstanceOf[JDecimal] => value.asInstanceOf[JDecimal].num.shortValue()
      case value if value.isInstanceOf[Int] => origValue.asInstanceOf[Int].toShort
      case value if value.isInstanceOf[Float] => origValue.asInstanceOf[Float].toShort
      case value if value.isInstanceOf[Long] => origValue.asInstanceOf[Long].toShort
      case value if value.isInstanceOf[Number] => origValue.asInstanceOf[Number].shortValue()
      case value if value.isInstanceOf[JString] => origValue.asInstanceOf[JString].s.toShort
      case value if value.isInstanceOf[Array[Byte]] => new Predef.String(origValue.asInstanceOf[Array[Byte]]).toShort
      case value if value == null => null
      case _ => origValue.toString.toShort
    }

  def checkFloatType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[Short] => origValue.asInstanceOf[Short].toFloat
      case value if value.isInstanceOf[Double] => origValue.asInstanceOf[Double].toFloat
      case value if value.isInstanceOf[JDouble] => value.asInstanceOf[JDouble].num.floatValue()
      case value if value.isInstanceOf[JInt] => value.asInstanceOf[JInt].num.floatValue()
      case value if value.isInstanceOf[JDecimal] => value.asInstanceOf[JDecimal].num.floatValue()
      case value if value.isInstanceOf[Int] => origValue.asInstanceOf[Int].toFloat
      case value if value.isInstanceOf[Float] => value
      case value if value.isInstanceOf[Long] => origValue.asInstanceOf[Long].toFloat
      case value if value.isInstanceOf[Number] => origValue.asInstanceOf[Number].floatValue()
      case value if value.isInstanceOf[JString] => origValue.asInstanceOf[JString].s.toFloat
      case value if value.isInstanceOf[Array[Byte]] => new Predef.String(origValue.asInstanceOf[Array[Byte]]).toFloat
      case value if value == null => null
      case _ => origValue.toString.toFloat
    }

  def checkIntType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[Int] => value
      case value if value.isInstanceOf[JInt] => value.asInstanceOf[JInt].num.intValue()
      case value if value.isInstanceOf[JDouble] => value.asInstanceOf[JDouble].num.intValue()
      case value if value.isInstanceOf[JDecimal] => value.asInstanceOf[JDecimal].num.intValue()
      case value if value.isInstanceOf[Double] => origValue.asInstanceOf[Double].toInt
      case value if value.isInstanceOf[Short] => origValue.asInstanceOf[Short].toInt
      case value if value.isInstanceOf[Float] => origValue.asInstanceOf[Float].toInt
      case value if value.isInstanceOf[Long] => origValue.asInstanceOf[Long].toInt
      case value if value.isInstanceOf[Number] => origValue.asInstanceOf[Number].intValue()
      case value if value.isInstanceOf[JString] => origValue.asInstanceOf[JString].s.toInt
      case value if value.isInstanceOf[Array[Byte]] => new Predef.String(origValue.asInstanceOf[Array[Byte]]).toInt
      case value if value == null => null
      case _ => origValue.toString.toInt
    }

  def checkByteType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[Byte] => value
      case value if value == null => null
      case _ => origValue.toString.toByte
    }

  def checkBooleanType(origValue: Any): Any =
    origValue match {
      case value if value.isInstanceOf[Boolean] => value
      case value if value.isInstanceOf[JBool] => value.asInstanceOf[JBool].value
      case value if value.isInstanceOf[JString] => origValue.asInstanceOf[JString].s.toBoolean
      case value if value.isInstanceOf[Array[Byte]] => new Predef.String(origValue.asInstanceOf[Array[Byte]]).toBoolean
      case value if value == null => null
      case _ => origValue.toString.toBoolean
    }
}

//scalastyle:on
