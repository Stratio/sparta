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

package com.stratio.benchmark.generator.runners

import java.util.UUID
import java.io.File

import com.stratio.benchmark.generator.constants.BenchmarkConstants
import com.stratio.benchmark.generator.models.{AvgStatisticalModel, StatisticalElementModel}
import com.stratio.benchmark.generator.threads.GeneratorThread
import com.stratio.benchmark.generator.utils.HttpUtil
import com.stratio.kafka.benchmark.generator.kafka.KafkaProducer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.Logger
import org.apache.commons.io.FileUtils
import org.json4s._
import org.json4s.native.Serialization.{read, writePretty}

import scala.io.Source
import scala.util.{Failure, Success, Try}

object GeneratorRunner {

  private val logger = Logger.getLogger(this.getClass)
  implicit val formats = DefaultFormats

  /**
    * Entry point of the application.
    *
    * @param args where you must pass the path of the config file.
    */
  def main(args: Array[String]) {
    if (args.size == 0) {
      logger.info("Use: java -jar benchmark.jar <config file>")
      System.exit(1)
    }

    Try(ConfigFactory.parseFile(new File(args(0)))) match {
      case Success(config) =>
        val policyContent: String = Source.fromFile(config.getString("policyPath"))
          .getLines().mkString
        val policyName: String = HttpUtil.extractFromJson(policyContent, "name")
        val checkpointPath: String = HttpUtil.extractFromJson(policyContent, "checkpointPath")
        logger.info(s"Extracted from JSON : (policyName, checkpointPath) = ($policyName, $checkpointPath)")
        initializeBenchmark(config, policyName, checkpointPath)
        generatePost(config, policyContent)
        generateEvents(config)
        generateReports(config)
        cleanupBenchmark(config, policyName)
      case Failure(exception) =>
        logger.error(exception.getLocalizedMessage, exception)
    }
  }

  /**
    * Initialize the benchmark:
    * - if police with same name exists it will be deleted,
    * - the .csv files will be deleted from metrics-folder directory
    * - and checkpoint directory will be cleaned.
    *
    * @param config with the needed parameters.
    * @param policyName name of policy.
    * @param checkpointPath base path for checkpointing.
    */
  def initializeBenchmark(config: Config, policyName: String, checkpointPath: String): Unit = {
    logger.info(s">> initializing benchmark...")
    val sparkMetricsPath = config.getString("sparkMetricsPath")
    val sparktaEndPoint = config.getString("sparktaEndPoint")
    HttpUtil.delete(policyName, sparktaEndPoint)
    logger.info(s">> deleting policy $policyName in $sparktaEndPoint")
    deleteFiles(s"$sparkMetricsPath", ".csv")
    cleanDirectory(s"$checkpointPath/$policyName")
  }

  /**
    * Looks for a policy and send a post to Sparkta.
    *
    * @param config with the needed parameters.
    * @param policyContent content of policy definition file.
    */
  def generatePost(config: Config, policyContent: String): Unit = {
    logger.info(s">> Sparkta benchmark started.")
    val sparktaEndPoint = config.getString("sparktaEndPoint")
    val postTimeout = config.getLong("postTimeout")
    logger.info(s"   Step 1/6 Sending policy to $sparktaEndPoint")
    HttpUtil.post(policyContent, sparktaEndPoint)
    logger.info(s"   Step 2/6 Waiting $postTimeout milliseconds to run the policy.")
    Thread.sleep(postTimeout)
  }

  /**
    * Wakes up n threads and it starts to queue events in Kafka.
    *
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

    while (stoppedThreads.numberOfThreads == numberOfThreads) {
      Thread.sleep(BenchmarkConstants.PoolingManagerGeneratorActorTimeout)
    }

    logger.info(s"   Step 3/6 Event generator finished. Number of generated events: ${stoppedThreads.numberOfEvents}")
  }

  /**
    * It generates two reports:
    * [timestamp]-fullReports.json: a report that contains information per executed spark's batch.
    * averageReport.json: a report that contains global averages of the previous report.
    *
    * @param config with the needed parameters.
    */
  def generateReports(config: Config): Unit = {
    val sparkMetricsPath = config.getString("sparkMetricsPath")
    val resultMetricsPath = config.getString("resultMetricsPath")
    val metricsTimeout = config.getLong("metricsTimeout")

    logger.info(s"   Step 4/6 Waiting $metricsTimeout  to generate spark's reports.")

    val statisticalElementModels = StatisticalElementModel.parsePathToStatisticalElementModels(s"$sparkMetricsPath")
    val avgStatisticalModel = StatisticalElementModel.parseTotals(statisticalElementModels)

    val fileFullReport = new File(s"$resultMetricsPath/${UUID.randomUUID().toString}-fullReport.json")

    logger.info(s"   Step 5/6 Generating full report: ${fileFullReport.getAbsolutePath}")
    FileUtils.writeStringToFile((fileFullReport), writePretty(statisticalElementModels))

    val fileAverageReport = new File(s"$resultMetricsPath/averageReport.json")

    val avgStatisticalModels = if (fileAverageReport.exists) {
      (read[Seq[AvgStatisticalModel]](Source.fromFile(fileAverageReport).mkString)) :+ avgStatisticalModel
    } else {
      Seq(avgStatisticalModel)
    }

    logger.info(s"   Step 6/6 Generating summary report: ${fileAverageReport.getAbsolutePath}")
    FileUtils.writeStringToFile((fileAverageReport), writePretty(avgStatisticalModels))

    logger.info(s"")
    logger.info(s">> Sparkta summary results:")
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
    logger.info(s">>Sparkta benchmark ended.")
  }

  /**
    * Stop the policy.
    *
    * @param config with the needed parameters.
    * @param policyName name of policy.
    */
  def cleanupBenchmark(config: Config, policyName: String): Unit = {
    val sparkMetricsPath = config.getString("sparkMetricsPath")
    val sparktaEndPoint = config.getString("sparktaEndPoint")
    logger.info(s">> cleaning benchmark.")
    logger.info(s">> stopping policy $policyName in endpoint $sparktaEndPoint")
    HttpUtil.stop(policyName, sparktaEndPoint)
  }

  /**
    * Copy files with specific ending from one directory to other one.
    *
    * @param originDirectory original directory.
    * @param destinationDirectory destination directory.
    * @param ending of file.
    */
  def copyFiles(originDirectory: String, destinationDirectory: String, ending: String): Unit = {
    val filesToDelete = new java.io.File(s"$originDirectory").listFiles.filter(_.getName.endsWith(ending))
      .foreach(file => FileUtils.copyFile(file, new File(s"$destinationDirectory/${file.getName}")))
  }

  /**
    * Delete files with specific ending from one directory.
    *
    * @param directory where files to be deleted resides.
    * @param ending of file to be deleted.
    */
  def deleteFiles(directory: String, ending: String): Unit = {
    val filesToDelete = new File(s"$directory").listFiles
      .filter(_.getName.endsWith(ending))
      .foreach(file => file.delete)
  }

  /**
    * Clean content of one directory.
    *
    * @param directory to be cleaned.
    */
  def cleanDirectory(directory: String): Unit = {
    val fileDir = new File(directory)
    if (fileDir.exists) {
      FileUtils.cleanDirectory(fileDir)
    }
  }

}

/**
  * Class used to know the state of the executed threads.
  *
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