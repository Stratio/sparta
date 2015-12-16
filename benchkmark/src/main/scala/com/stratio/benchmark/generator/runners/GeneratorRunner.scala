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

package com.stratio.benchmark.generator.runners

import java.io.File

import com.stratio.benchmark.generator.threads.GeneratorThread
import com.stratio.benchmark.generator.constants.BenchmarkConstants
import com.stratio.kafka.benchmark.generator.kafka.KafkaProducer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.Logger

import scala.util.{Failure, Success, Try}

object GeneratorRunner {

  private val logger = Logger.getLogger(this.getClass)

  def main(args: Array[String]) {
    if (args.size == 0) {
      logger.info("Use: java -jar benchmark.jar <config file>")
      System.exit(1)
    }

    Try(ConfigFactory.parseFile(new File(args(0)))) match {
      case Success(config) =>
        startBenchmark(config)
      case Failure(exception) =>
        logger.error(exception.getLocalizedMessage, exception)
    }
  }

  def startBenchmark(config: Config): Unit = {
    val numberOfThreads = config.getInt("numberOfThreads")
    val threadTimeout = config.getLong("threadTimeout")
    val kafkaTopic = config.getString("kafkaTopic")
    val stoppedThreads = new StoppedThreads(numberOfThreads, 0)

    (1 to numberOfThreads).foreach(i =>
      new Thread(
        new GeneratorThread(KafkaProducer.getInstance(config), threadTimeout, stoppedThreads, kafkaTopic)).start()
    )

    while(stoppedThreads.numberOfThreads == numberOfThreads) {
      Thread.sleep(BenchmarkConstants.PoolingManagerGeneratorActorTimeout)
    }

    logger.info(s">> Number of events sent to Kafka: ${stoppedThreads.numberOfEvents}")
  }
}

class StoppedThreads(var numberOfThreads: Int, var numberOfEvents: BigInt) {
  def incrementNumberOfThreads: Unit = {
    this.synchronized(numberOfThreads = numberOfThreads + 1)
  }

  def incrementNumberOfEvents(offset: BigInt): Unit = {
    this.synchronized(numberOfEvents= numberOfEvents + offset)
  }
}