/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.receiver

import java.util.concurrent.atomic.AtomicBoolean

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.datasource.config.ConfigParameters
import org.apache.spark.streaming.datasource.config.ConfigParameters._
import org.apache.spark.streaming.datasource.config.ParametersHelper._
import org.apache.spark.streaming.datasource.models.OffsetLocation.OffsetLocation
import org.apache.spark.streaming.datasource.models.{InputSentences, OffsetConditions, OffsetLocation, StatusOffset}
import org.apache.spark.streaming.datasource.receiver.DatasourceDStream._
import org.apache.spark.streaming.datasource.storage.ZookeeperHelper
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.scheduler.rate.RateEstimator
import org.apache.spark.streaming.scheduler.{RateController, StreamInputInfo}
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

import scala.util.Try

class DatasourceDStream[C <: SparkSession](
                                            @transient val _ssc: StreamingContext,
                                            val inputSentences: InputSentences,
                                            val datasourceParams: Map[String, String],
                                            @ transient val sparkSession: C
                                          ) extends InputDStream[Row](_ssc) {

  var progressInputSentences: Option[InputSentences] = None

  private[spark] val hasPendingJob: AtomicBoolean = new AtomicBoolean(false)

  private[streaming] override def name: String = s"Datasource stream [$id]"

  lazy val storeOffsetAfterWritingOutputs: Boolean =
    Try(datasourceParams.getOrElse(ConfigParameters.StoreOffsetAfterWritingOutputs, "false").toBoolean)
      .getOrElse(false)

  storageLevel = calculateStorageLevel()

  inputSentences.initialStatements.foreach { statement =>
    if (statement.toUpperCase.startsWith("CREATE TEMPORARY TABLE") || statement.toUpperCase.startsWith("CREATE TABLE"))
      sparkSession.sql(statement)
    else log.error(s"Invalid sentence: $statement")
  }

  /**
    * Min storage level is MEMORY_ONLY, because compute function for one rdd is called more than one place
    */
  private[streaming] def calculateStorageLevel(): StorageLevel = {
    val levelFromParams = getStorageLevel(datasourceParams)
    if (levelFromParams == StorageLevel.NONE) {
      log.warn("NONE is not a valid storage level for this datasource receiver, setting it in MEMORY_ONLY")
      StorageLevel.MEMORY_ONLY
    } else levelFromParams
  }

  /**
    * Remember duration for the rdd created by this InputDStream,
    * by default DefaultMinRememberDuration = 60s * slideWindow
    */
  private val userRememberDuration = getRememberDuration(datasourceParams)
  private val offsetsLocation = getOffsetLocation(datasourceParams)
  private val zookeeperParams = getZookeeperParams(datasourceParams)
  private val ignoreStartedStatus = getIgnoreStartedStatus(datasourceParams)
  private val resetOffsetOnStart = getResetOffsetOnStart(datasourceParams)

  userRememberDuration match {
    case Some(duration) =>
      remember(Seconds(duration))
    case None =>
      val minRememberDuration = Seconds(ssc.conf.getTimeAsSeconds(
        ssc.conf.get("spark.streaming.minRememberDuration", DefaultMinRememberDuration), DefaultMinRememberDuration))
      val numBatchesToRemember = math.ceil(minRememberDuration.milliseconds / slideDuration.milliseconds).toInt

      remember(slideDuration * numBatchesToRemember)
  }

  /**
    * Calculate the max number of records that the receiver must receive and process in one batch when the
    * blackPressure is enable
    */
  private[streaming] def maxRecords(): Option[(Int, Long)] = {
    val estimatedRateLimit = rateController.map(_.getLatestRate().toInt)
    estimatedRateLimit.flatMap(estimatedRateLimit => {
      if (estimatedRateLimit > 0) {
        val recordsRateController = ((slideDuration.milliseconds.toDouble / 1000) * estimatedRateLimit).toLong

        Option((estimatedRateLimit, getMaxRecordsWithRate(recordsRateController)))
      } else None
    })
  }

  /**
    *
    * @return max number of records that the input RDD must receive in the next window
    */
  private[streaming] def getMaxRecordsWithRate(recordsRateController: Long): Long = {
    inputSentences.offsetConditions.fold(recordsRateController) { offsetConditions =>
      offsetConditions.limitRecords.fold(recordsRateController) { maxRecordsPartition =>
        Math.min(recordsRateController, maxRecordsPartition)
      }
    }
  }

  override def compute(validTime: Time): Option[RDD[Row]] =
    if (hasPendingJob.compareAndSet(false, true)) {
      val datasourceRDD = doCompute(validTime)
      datasourceRDD
    } else {
      Some(_ssc.sparkContext.emptyRDD)
    }


  private def doCompute(validTime: Time): Option[DatasourceRDD] = {
    // Report the record number and metadata of this batch interval to InputInfoTracker and calculate the maxRecords
    val maxRecordsCalculation = maxRecords().map { case (estimated, newLimitRecords) =>
      val description =
        s"LimitRecords : ${
          inputSentences.offsetConditions.fold("") {
            _.limitRecords.fold("") {
              _.toString
            }
          }
        }\t:" + s" Estimated: $estimated\t NewLimitRecords: $newLimitRecords"

      (StreamInputInfo.METADATA_KEY_DESCRIPTION -> description, newLimitRecords)
    }
    val metadata = Map("InputSentences" -> inputSentences) ++
      maxRecordsCalculation.fold(Map.empty[String, Any]) { case (description, _) => Map(description) }
    val offsetsCalculated = getOffsetsFromLocation(inputSentences, offsetsLocation, zookeeperParams)
    val inputSentencesCalculated = inputSentences.copy(offsetConditions = offsetsCalculated)
    val inputSentencesLimited = maxRecordsCalculation.fold(inputSentencesCalculated) { case (_, maxMessages) =>
      inputSentencesCalculated.copy(offsetConditions = inputSentencesCalculated.offsetConditions.map(conditions =>
        conditions.copy(limitRecords = Option(maxMessages))))
    }

    val datasourceRDD = new DatasourceRDD(
      sparkSession,
      inputSentencesLimited,
      datasourceParams,
      inputSentences.query
    )

    progressInputSentences = Some(datasourceRDD.progressInputSentences)

    if (!storeOffsetAfterWritingOutputs){
      commitOffsets()
    }

    //publish data in Spark UI
    ssc.scheduler.inputInfoTracker.reportInfo(validTime, StreamInputInfo(id, datasourceRDD.count(), metadata))

    Option(datasourceRDD)
  }

  override def start(): Unit = {
    inMemoryOffsets = None
    firstBach = true
    if (resetOffsetOnStart && ignoreStartedStatus ||
      (resetOffsetOnStart && ZookeeperHelper.getOffsets(zookeeperParams).forall(!_.started)))
      ZookeeperHelper.resetOffsets(zookeeperParams)
  }

  override def stop(): Unit = {
    ZookeeperHelper.setNotStarted(zookeeperParams)
    ZookeeperHelper.resetInstance()
  }

  def commitOffsets(): Unit = {
    progressInputSentences.foreach{ progressInputSent =>
      log.debug(s"commiting offsets: $progressInputSent")
      setIncrementalOffsets(
        progressInputSent,
        offsetsLocation,
        zookeeperParams,
        resetOffsetOnStart,
        ignoreStartedStatus
      )
    }
    hasPendingJob.lazySet(false)
  }

  /**
    * Asynchronously maintains & sends new rate limits to the receiver through the receiver tracker.
    */
  override protected[streaming] val rateController: Option[RateController] = {
    if (RateController.isBackPressureEnabled(ssc.conf))
      Option(new DatasourceRateController(id, RateEstimator.create(ssc.conf, context.graph.batchDuration)))
    else None
  }

  /**
    * A RateController to retrieve the rate from RateEstimator.
    */
  private[streaming] class DatasourceRateController(id: Int, estimator: RateEstimator)
    extends RateController(id, estimator) {

    override def publish(rate: Long): Unit = {
      ssc.scheduler.receiverTracker.sendRateUpdate(id, rate)
    }
  }

}

private[streaming] object DatasourceDStream {

  /**
    * Control the new calculated offsets
    */
  var inMemoryOffsets: Option[OffsetConditions] = None
  var firstBach = true

  def getOffsetsFromLocation(
                              inputSentences: InputSentences,
                              offsetsLocation: OffsetLocation,
                              zookeeperParams: Map[String, String]
                            ): Option[OffsetConditions] = {
    val newOffsets = offsetsLocation match {
      case OffsetLocation.ZOOKEEPER =>
        val zkStatusOffset = ZookeeperHelper.getOffsets(zookeeperParams)
        if (firstBach) {
          firstBach = false
          zkStatusOffset.map(_.offsetConditions)
        } else {
          firstBach = false
          inMemoryOffsets
        }
      case _ =>
        inMemoryOffsets
    }

    newOffsets.orElse(inputSentences.offsetConditions)
  }

  def setIncrementalOffsets(
                             inputSentences: InputSentences,
                             offsetsLocation: OffsetLocation,
                             zookeeperParams: Map[String, String],
                             resetOffsetOnStart: Boolean,
                             ignoreStartedStatus: Boolean
                           ): Unit = {
    inMemoryOffsets = inputSentences.offsetConditions

    if (offsetsLocation == OffsetLocation.ZOOKEEPER)
      inputSentences.offsetConditions.foreach { conditions =>
        ZookeeperHelper.setOffsets(zookeeperParams, StatusOffset(started = true, conditions))
      }
  }
}