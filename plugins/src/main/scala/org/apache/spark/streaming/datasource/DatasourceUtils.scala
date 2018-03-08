/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
