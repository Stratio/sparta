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
package com.stratio.sparkta.driver.factory

import java.io.File

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.Config
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._

/**
 * Created by ajnavarro on 14/10/14.
 */
object SparkContextFactory extends SLF4JLogging {

  private var sc : Option[SparkContext] = None
  private var sqlContext : Option[SQLContext] = None
  private var ssc : Option[StreamingContext] = None

  def sparkSqlContextInstance : Option[SQLContext] = {
    synchronized {
      if ((sc.isDefined) && (sqlContext.isEmpty)) {
        sqlContext = Some(new SQLContext(sc.get))
      }
      sqlContext
    }
  }

  def sparkStreamingInstance(batchDuration : Duration) : Option[StreamingContext] = {
    synchronized {
      if ((sc.isDefined) && (ssc.isEmpty)) {
        ssc = Some(new StreamingContext(sc.get, batchDuration))
      }
      ssc
    }
  }

  def sparkContextInstance(generalConfig: Config, jars: Seq[File]): SparkContext = {
    synchronized {
      if (sc.isEmpty) {
        sc = Some(new SparkContext(configToSparkConf(generalConfig)))
        jars.foreach(f => sc.get.addJar(f.getAbsolutePath))
      }
      sc.get
    }
  }

  private def configToSparkConf(generalConfig: Config): SparkConf = {
    val c = generalConfig.getConfig("spark")
    val properties = c.entrySet()
    val conf = new SparkConf()

    properties.foreach(e => conf.set(e.getKey, c.getString(e.getKey)))

    conf.setIfMissing("spark.streaming.concurrentJobs", "20")

    //TODO add in output
    conf.setIfMissing("spark.cassandra.connection.host", "127.0.0.1")
    conf.setIfMissing("spark.sql.parquet.binaryAsString","true")

    conf
  }
}
