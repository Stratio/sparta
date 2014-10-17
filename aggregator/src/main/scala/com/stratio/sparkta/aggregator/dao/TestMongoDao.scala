/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.aggregator.dao

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.stratio.sparkta.aggregator.UpdateMetricOperation
import com.typesafe.config.Config

/**
 * Created by ajnavarro on 15/10/14.
 */
case class TestMongoDao(private val config: Config) extends AbstractMongoDAO(config) {
  override def KEYSPACE: String = "SPARKTA"

  def upsert(metricOp: UpdateMetricOperation): Unit = {
    val collection = db.getCollection(metricOp.keyString)
    metricOp.aggregations.foreach(field => {
      val builder = MongoDBObject.newBuilder
      metricOp.rollupKey.foreach(k => builder += k._1.name -> k._3.head)
      builder += "_id" -> metricOp.rollupKey.flatMap(_._3).mkString("__")
      collection.update(
        builder.result,
        $inc(field._1 -> field._2),
        true, false)
    })
  }
}
