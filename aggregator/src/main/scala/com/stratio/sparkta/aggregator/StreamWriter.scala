/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.aggregator

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.aggregator.StreamWriter._
import com.stratio.sparkta.sdk.{Output, TableSchema}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream

case class StreamWriterOptions(overLast: Option[String],
                               sparkStreamingWindow: Long,
                               initSchema: StructType)

case class StreamWriter(triggers: Seq[Trigger],
                        tableSchemas: Seq[TableSchema],
                        options: StreamWriterOptions,
                        outputs: Seq[Output]) extends TriggerWriter with SLF4JLogging {

  def write(streamData: DStream[Row]): Unit = {
    val dStream = options.overLast.fold(streamData) { over =>
      streamData.window(Seconds(over.toLong), Seconds(options.sparkStreamingWindow))
    }

    dStream.foreachRDD(rdd => {
      val parsedDataFrame = SQLContext.getOrCreate(rdd.context).createDataFrame(rdd, options.initSchema)

      writeTriggers(parsedDataFrame, triggers, StreamTableName, tableSchemas, outputs)
    })
  }
}

object StreamWriter {

  val StreamTableName = "stream"
}
