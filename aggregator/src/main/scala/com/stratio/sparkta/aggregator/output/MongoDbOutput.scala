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
package com.stratio.sparkta.aggregator.output

import com.stratio.sparkta.aggregator.UpdateMetricOperation
import com.stratio.sparkta.aggregator.dao.TestMongoDao
import com.typesafe.config.Config
import org.apache.spark.streaming.dstream.DStream

/**
 * Created by ajnavarro on 15/10/14.
 */
case class MongoDbOutput(config: Config) extends AbstractOutput {

  private val dao = new TestMongoDao(config)

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = {
    stream.foreachRDD(rdd =>
      rdd.foreach(op =>
        dao.upsert(op)
      )
    )
  }

  override def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    streams.foreach(persist)
  }

}
