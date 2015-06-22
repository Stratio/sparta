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

import com.stratio.sparkta.sdk.TypeOp
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk._

trait ElasticSearchDAO extends Closeable {

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
  final val DEFAULT_PORT = "9300"

  def nodes: String

  def defaultPort: String

  def defaultAnalyzerType: Option[String]

  def idField: Option[String] = None

  def defaultIndexMapping: Option[String] = None

  def mappingType: Option[String] = None

  def getSparkConfig(timeName: String, idProvided: Boolean): Map[String, String] = {
    {
      if (idProvided)
        Map("es.mapping.id" -> idField.getOrElse(Output.Id))
      else
        Map("" -> "")
    } ++
      Map("es.nodes" -> nodes, "es.port" -> defaultPort, "es.index.auto.create" -> "no") ++ {
      defaultAnalyzerType match {
        case Some(analyzer) => Map("es.index.analysis.analyzer.default.type" -> analyzer)
        case None => Map("" -> "")
      }
    } ++ {
      if (timeName.isEmpty) Map("" -> "") else Map("es.mapping.names" -> s"$timeName:@timestamp")
    }
  }

  def getDateTimeType(dateType: Option[String]): TypeOp = {
    dateType match {
      case None => TypeOp.String
      case Some(date) => date.toLowerCase match {
        case "timestamp" => TypeOp.Timestamp
        case "date" => TypeOp.Date
        case "datetime" => TypeOp.DateTime
        case _ => TypeOp.String
      }
    }
  }

  protected def getDateFromType(indexType: String): Option[String] = {
    indexType.toLowerCase match {
      case "second" | "minute" | "hour" | "day" | "month" | "year" =>
        Some(new SimpleDateFormat(getGranularityPattern(indexType).getOrElse(DEFAULT_DATE_FORMAT))
          .format(new Date(DateOperations.dateFromGranularity(DateTime.now(), indexType))))
      case _ => Some(indexType)
    }
  }

  protected def getGranularityPattern(indexType: String): Option[String] = {
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

  def close(): Unit = {}
}

