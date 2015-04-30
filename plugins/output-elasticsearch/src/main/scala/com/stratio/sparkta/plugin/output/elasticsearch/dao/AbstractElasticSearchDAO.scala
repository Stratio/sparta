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

package com.stratio.sparkta.plugin.output.elasticsearch.dao

import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.Date

import org.joda.time.DateTime

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.{Output, TypeOp}

trait AbstractElasticSearchDAO extends Closeable {

  def nodes: String

  def defaultPort: String

  def idField: Option[String] = None

  def defaultIndexMapping: Option[String] = None

  def indexMapping: Option[String] = None

  def getSparkConfig(timeBucket: Option[String]): Map[String, String] = {
    Map("es.mapping.id" -> idField.getOrElse(Output.ID),
      "spark.es.nodes" -> nodes,
      "spark.es.port" -> defaultPort) ++ {
      timeBucket match {
        case Some(tbucket) => Map("es.mapping.names" -> s"$tbucket:@timestamp")
        case None => Map("" -> "")
      }
    }
  }

  def close(): Unit = {}
}

object AbstractElasticSearchDAO {

  final val TIMESTAMP_PATTERN = "@timestamp:"
  final val DEFAULT_DATE_FORMAT = "YYYY.MM.dd"
  final val YEAR = "YYYY"
  final val MONTH = "MM"
  final val DAY = "dd"
  final val HOUR = "HH"
  final val MINUTE = "mm"
  final val SECOND = "ss"
  final val DEFAULT_INDEX_TYPE = "sparkta"
  final val DEFAULT_NODE = "localhost"
  final val DEFAULT_PORT = "9200"

  def getDateTimeType(dateType: Option[String]): TypeOp = {
    dateType match {
      case None => TypeOp.Timestamp
      case Some(date) => date.toLowerCase match {
        case "timestamp" => TypeOp.Timestamp
        case _ => TypeOp.String
      }
    }
  }

  def getIndexType(defaultIndexType: Option[String]): Option[String] = {
    defaultIndexType match {
      case None => defaultIndexType
      case Some(indexType) => getDateFromType(indexType)
    }
  }

  def getDateFromType(indexType: String): Option[String] = {
    indexType.toLowerCase match {
      case "second" | "minute" | "hour" | "day" | "month" | "year" =>
        Some(new SimpleDateFormat(getGranularityPattern(indexType).getOrElse(DEFAULT_DATE_FORMAT))
          .format(new Date(Output.dateFromGranularity(DateTime.now(), indexType).getTime)))
      case _ => Some(indexType)
    }
  }

  def getIndexTypePattern(indexType: String): Option[String] = {
    getGranularityPattern(indexType) match {
      case None => Some(indexType)
      case Some(pattern) => Some(s"{$TIMESTAMP_PATTERN$pattern}")
    }
  }

  def getGranularityPattern(indexType: String): Option[String] = {
    indexType.toLowerCase match {
      case "second" => Some(s"$YEAR.$MONTH.$DAY $HOUR:$MINUTE:$SECOND")
      case "minute" => Some(s"$YEAR.$MONTH.$DAY $HOUR:$MINUTE")
      case "hour" => Some(s"$YEAR.$MONTH.$DAY $HOUR")
      case "day" => Some(s"$YEAR.$MONTH.$DAY")
      case "month" => Some(s"$YEAR.$MONTH")
      case "year" => Some(s"$YEAR")
      case _ => None
    }
  }
}

