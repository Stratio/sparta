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

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.{TypeOp, _}

trait ElasticSearchDAO {

  final val TimestampPattern = "@timestamp:"
  final val DefaultIndexType = "sparkta"
  final val DefaultNode = "localhost"
  final val DefaultPort = "9200"

  val dateTypeMap = Map("timestamp" -> TypeOp.Timestamp, "date" -> TypeOp.Date, "datetime" -> TypeOp.DateTime)

  def nodes: Seq[(String, Int)]

  def idField: Option[String] = None

  def defaultIndexMapping: Option[String] = None

  def mappingType: Option[String] = None

  def getSparkConfig(timeName: String, idProvided: Boolean): Map[String, String] = {
    if (idProvided)
      Map("es.mapping.id" -> idField.getOrElse(Output.Id))
    else
      Map()
  } ++
    Map("es.nodes" -> nodes(0)._1, "es.port" -> nodes(0)._2.toString, "es.index.auto.create" -> "no") ++ {
    if (timeName.isEmpty) Map()
    else Map("es.mapping.names" -> s"$timeName:@timestamp")
  }

  def getDateTimeType(dateType: Option[String]): TypeOp = {
    dateType match {
      case None => TypeOp.String
      case Some(date) => dateTypeMap.get(date.toLowerCase).getOrElse(TypeOp.String)
    }
  }
}
