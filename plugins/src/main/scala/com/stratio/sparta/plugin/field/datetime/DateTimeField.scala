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
package com.stratio.sparta.plugin.field.datetime

import java.io.{Serializable => JSerializable}
import java.util.Date

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.{TypeOp, _}
import org.joda.time.DateTime
import DateTimeField._

case class DateTimeField(props: Map[String, JSerializable], override val defaultTypeOperation : TypeOp)
  extends DimensionType with JSerializable with SLF4JLogging {

  def this(defaultTypeOperation : TypeOp) {
    this(Map.empty[String, JSerializable], defaultTypeOperation)
  }

  def this(props: Map[String, JSerializable]) {
    this(props,  TypeOp.Timestamp)
  }

  def this() {
    this(Map.empty[String, JSerializable], TypeOp.Timestamp)
  }

  override val operationProps: Map[String, JSerializable] = props

  override val properties: Map[String, JSerializable] = props ++ {
    if (!props.contains(AggregationTime.GranularityPropertyName))
      Map(AggregationTime.GranularityPropertyName -> AggregationTime.DefaultGranularity)
    else Map.empty[String, JSerializable]
  }

  override def precision(keyName: String): Precision = {
    if (AggregationTime.precisionsMatches(keyName).nonEmpty) getPrecision(keyName, getTypeOperation(keyName))
    else TimestampPrecision
  }

  @throws(classOf[ClassCastException])
  override def precisionValue(keyName: String, value: Any): (Precision, Any) =
    try {
      val precisionKey = precision(keyName)
      (precisionKey, getPrecision(TypeOp.transformValueByTypeOp(TypeOp.Date, value).asInstanceOf[Date],
        precisionKey, properties))
    }
    catch {
      case cce: ClassCastException =>
        log.error("Error parsing " + value + " .")
        throw cce
    }

  private def getPrecision(value: Date, precision: Precision, properties: Map[String, JSerializable]): Any = {
    TypeOp.transformValueByTypeOp(precision.typeOp,
      AggregationTime.truncateDate(new DateTime(value), precision match {
        case t if t == TimestampPrecision => if (properties.contains(AggregationTime.GranularityPropertyName))
          properties.get(AggregationTime.GranularityPropertyName).get.toString
        else AggregationTime.DefaultGranularity
        case _ => precision.id
      })).asInstanceOf[Any]
  }
}

object DateTimeField {

  final val TimestampPrecision = DimensionType.getTimestamp(Some(TypeOp.Timestamp), TypeOp.Timestamp)

}
