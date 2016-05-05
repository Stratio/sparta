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
package com.stratio.benchmark.generator.runners

import java.io.File
import java.util.UUID

import com.stratio.benchmark.generator.constants.BenchmarkConstants
import com.stratio.benchmark.generator.models.{AvgStatisticalModel, StatisticalElementModel}
import com.stratio.benchmark.generator.threads.GeneratorThread
import com.stratio.benchmark.generator.utils.HttpUtil
import com.stratio.kafka.benchmark.generator.kafka.KafkaProducer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.json4s.native.Serialization.{read, writePretty}

import scala.io.Source
import scala.util.{Failure, Success, Try}

import org.json4s.DefaultFormats

object GeneratorRunner extends HttpUtil {

  private val logger = Logger.getLogger(this.getClass)

  implicit val formats = DefaultFormats

  /**
   * Entry point of the application.
   * @param args where you must pass the path of the config file.
   */
  def main(args: Array[String]) {


    if (args.size == 0) {
      logger.info("Use: java -jar benchmark.jar <config file>")
      System.exit(1)
    }

    Try(ConfigFactory.parseFile(new File(args(0)))) match {
      case Success(config) =>
        preCleanup(config)
        val policyId = generatePost(config)
        generateEvents(config)
        generateReports(config)
        postCleanup(policyId, config)
      case Failure(exception) =>
        logger.error(exception.getLocalizedMessage, exception)
    }
  }

  /**
   * Deletes the spark metrics path
   * @param config with the needed parameters.
   */
  def preCleanup(config: Config): Unit = {
    val sparkMetricsPath = config.getString("sparkMetricsPath")
    val resultMetricsPath = config.getString("resultMetricsPath")

    val sparkMetricsFile = new File(sparkMetricsPath)
    val resultMetricsFile = new File(resultMetricsPath)

    if(!sparkMetricsFile.exists()) {
      sparkMetricsFile.mkdir()
    }

    if(!resultMetricsFile.exists()) {
      resultMetricsFile.mkdir()
    }

    FileUtils.cleanDirectory(new File(sparkMetricsPath))
  }

  /**
   * Stops and deletes the policy. Also it deletes the spark metrics path
   * @param id of the policy.
   * @param config with the needed parameters.
   */
  def postCleanup(id: String, config: Config): Unit = {
    val spartaEndpoint = config.getString("spartaEndpoint")
    val sparkMetricsPath = config.getString("sparkMetricsPath")

    stopPolicy(id, spartaEndpoint)
    deletePolicy(id, spartaEndpoint)
    FileUtils.cleanDirectory(new File(sparkMetricsPath))
  }

  /**
   * Looks for a policy and send a post to Sparta.
   * @param config with the needed parameters.
   */
  def generatePost(config: Config): String = {
    val spartaEndpoint = config.getString("spartaEndpoint")
    val postTimeout = config.getLong("postTimeout")
    val policyPath = config.getString("policyPath")

    val policyContent = Source.fromFile(policyPath).getLines().mkString

    logger.info(s">> Sparta benchmark started.")
    logger.info(s"   Step 1/6 Sending policy $policyPath to $spartaEndpoint")

    val policyId = createPolicy(policyContent, s"$spartaEndpoint")

    logger.info(s"   Step 2/6 Waiting $postTimeout milliseconds to run the policy.")
    Thread.sleep(postTimeout)

    policyId
  }

  /**
   * Wakes up n threads and it starts to queue events in Kafka.
   * @param config with the needed parameters.
   */
  def generateEvents(config: Config): Unit = {
    val numberOfThreads = config.getInt("numberOfThreads")
    val threadTimeout = config.getLong("threadTimeout")
    val kafkaTopic = config.getString("kafkaTopic")
    val stoppedThreads = new StoppedThreads(numberOfThreads, 0)

    logger.info(s"   Step 3/6 Event generator started. Number of threads: $numberOfThreads($threadTimeout " +
      s"milliseconds)")

    (1 to numberOfThreads).foreach(i =>
      new Thread(
        new GeneratorThread(KafkaProducer.getInstance(config), threadTimeout, stoppedThreads, kafkaTopic)).start()
    )

    while(stoppedThreads.numberOfThreads == numberOfThreads) {
      Thread.sleep(BenchmarkConstants.PoolingManagerGeneratorActorTimeout)
    }

    logger.info(s"   Step 3/6 Event generator finished. Number of generated events: ${stoppedThreads.numberOfEvents}")
  }

  /**
   * It generates two reports:
   * [timestamp]-fullReports.json: a report that contains information per executed spark's batch.
   * averageReport.json: a report that contains global averages of the previous report.
   * @param config with the needed parameters.
   */
  def generateReports(config: Config): Unit = {
    val sparkMetricsPath = config.getString("sparkMetricsPath")
    val resultMetricsPath = config.getString("resultMetricsPath")
    val metricsTimeout = config.getLong("metricsTimeout")

    logger.info(s"   Step 4/6 Waiting $metricsTimeout  to generate spark's reports.")

    val statisticalElementModels = StatisticalElementModel.parsePathToStatisticalElementModels(sparkMetricsPath)
    val avgStatisticalModel = StatisticalElementModel.parseTotals(statisticalElementModels)

    val fileFullReport = new File(s"$resultMetricsPath/${UUID.randomUUID().toString}-fullReport.json")

    logger.info(s"   Step 5/6 Generating full report: ${fileFullReport.getAbsolutePath}")
    FileUtils.writeStringToFile((fileFullReport), writePretty(statisticalElementModels))

    val fileAverageReport = new File(s"$resultMetricsPath/averageReport.json")

    val avgStatisticalModels  = if(fileAverageReport.exists) {
      (read[Seq[AvgStatisticalModel]](Source.fromFile(fileAverageReport).mkString)) :+ avgStatisticalModel
    } else {
      Seq(avgStatisticalModel)
    }

    logger.info(s"   Step 6/6 Generating summary report: ${fileAverageReport.getAbsolutePath}")
    FileUtils.writeStringToFile((fileAverageReport), writePretty(avgStatisticalModels))

    logger.info(s"")
    logger.info(s">> Sparta summary results:")

    avgStatisticalModels.foreach(element => {
      logger.info(s"   MetricName        Type               Value")
      logger.info(s"")
      logger.info(s"   Id                Id                 ${element.id}")
      logger.info(s"   ReceivedRecords   Accumulate         ${element.receivedRecords}")
      logger.info(s"   CompletedBatches  Accumulate         ${element.completedBatches}")
      logger.info(s"   ProcessedRecords  Accumulate         ${element.processedRecords}")
      logger.info(s"   ProcessingTime    Average            ${element.processingTime}")
      logger.info(s"   SchedulingDelay   Average            ${element.schedulingDelay}")
      logger.info(s"   ProcessingDelay   Average            ${element.processingDelay}")
      logger.info(s"   TotalDelay        Average            ${element.totalDelay}")
      logger.info("")
    })

    logger.info(s">>Sparta benchmark ended.")
  }
}

/**
 * Class used to know the state of the executed threads.
 * @param numberOfThreads with information about number of stopped threads.
 * @param numberOfEvents with information about number of processed events.
 */
class StoppedThreads(var numberOfThreads: Int, var numberOfEvents: BigInt) {

  def incrementNumberOfThreads: Unit = {
    this.synchronized(numberOfThreads = numberOfThreads + 1)
  }

  def incrementNumberOfEvents(offset: BigInt): Unit = {
    this.synchronized(numberOfEvents= numberOfEvents + offset)
  }
}