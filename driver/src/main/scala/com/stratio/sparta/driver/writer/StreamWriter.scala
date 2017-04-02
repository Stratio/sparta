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

package com.stratio.sparta.driver.writer

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.sdk.pipeline.output.Output
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream

case class StreamWriter(triggers: Seq[Trigger],
                        inputTableName: String,
                        outputs: Seq[Output]) extends TriggerWriter with SLF4JLogging {

  def write(streamData: DStream[Row], schema: StructType): Unit = {
    streamData.foreachRDD(rdd => {
      val parsedDataFrame = SparkContextFactory.sparkSessionInstance.createDataFrame(rdd, schema)

      writeTriggers(parsedDataFrame, triggers, inputTableName, outputs)
    })
  }
}
