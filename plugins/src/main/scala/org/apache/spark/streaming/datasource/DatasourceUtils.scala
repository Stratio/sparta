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

package org.apache.spark.streaming.datasource

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.datasource.models.InputSentences
import org.apache.spark.streaming.datasource.receiver.DatasourceDStream
import org.apache.spark.streaming.dstream.InputDStream


object DatasourceUtils {

  /**
    * Create an input stream that receives messages from a Datasource.
    * The result DStream is mapped to the type Row with the results of the incremental queries.
    *
    * @param ssc              StreamingContext object
    * @param inputSentences   Object that can contains the initial query, the offset options for incremental queries
    *                         and monitoring tables and the stop conditions for the streaming process
    *                         tuple of queue, exchange, routing key and hosts can be one Datasource independent
    * @param datasourceParams Datasource params with spark options for pass to Spark SQL Context
    * @return The new DStream with the rows extracted from the Datasources
    */
  def createStream(
                    ssc: StreamingContext,
                    inputSentences: InputSentences,
                    datasourceParams: Map[String, String]
                  ): InputDStream[Row] = {

    val sparkSession = SparkSession.builder().config(ssc.sparkContext.getConf).getOrCreate()

    new DatasourceDStream(ssc, inputSentences, datasourceParams, sparkSession)
  }

  /**
    * Create an input stream that receives messages from a Datasource.
    * The result DStream is mapped to the type Row with the results of the incremental queries.
    *
    * @param ssc              StreamingContext object
    * @param inputSentences   Object that can contains the initial query, the offset options for incremental queries
    *                         and monitoring tables and the stop conditions for the streaming process
    *                         tuple of queue, exchange, routing key and hosts can be one Datasource independent
    * @param datasourceParams Datasource params with spark options for pass to Spark SQL Context
    * @param sparkSession     Generic Context for execute the SQL queries, that must extends Spark SQLContext
    * @return The new DStream with the rows extracted from the Datasources
    */
  def createStream[C <: SparkSession](
                                       ssc: StreamingContext,
                                       inputSentences: InputSentences,
                                       datasourceParams: Map[String, String],
                                       sparkSession: C
                                     ): InputDStream[Row] = {

    new DatasourceDStream(ssc, inputSentences, datasourceParams, sparkSession)
  }
}
