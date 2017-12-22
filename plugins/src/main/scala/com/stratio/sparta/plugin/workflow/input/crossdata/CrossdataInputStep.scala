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

package com.stratio.sparta.plugin.workflow.input.crossdata

import java.io.{Serializable => JSerializable}

import akka.actor.{ActorSystem, Cancellable}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.plugin.workflow.input.crossdata.models.OffsetFieldItem
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputOptions}
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.datasource.DatasourceUtils
import org.apache.spark.streaming.datasource.config.ConfigParameters
import org.apache.spark.streaming.datasource.models._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted}
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Properties, Try}

import DistributedMonad.Implicits._

class CrossdataInputStep(
                          name: String,
                          outputOptions: OutputOptions,
                          ssc: Option[StreamingContext],
                          xDSession: XDSession,
                          properties: Map[String, JSerializable]
                        )
  extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val query = properties.getString("query")
  lazy val finishApplicationWhenEmpty = properties.getBoolean("finishAppWhenEmpty", default = false)
  lazy val limitRecords = properties.getLong("limitRecords", None)
  lazy val stopContexts = properties.getBoolean("stopContexts", default = false)
  lazy val zookeeperPath = Properties.envOrElse("SPARTA_ZOOKEEPER_PATH", "/stratio/sparta") + {
    val path = properties.getString("zookeeperPath", "/crossdata/offsets")
    if (path.startsWith("/")) path
    else s"/$path"
  }
  lazy val initialSentence = properties.getString("initialSentence", None).notBlank.flatMap { sentence =>
    if (sentence.toUpperCase.startsWith("CREATE TEMPORARY TABLE"))
      Option(sentence)
    else {
      val message = s"Invalid sentence: $sentence"
      log.error(message)
      throw new RuntimeException(message)
    }
  }

  lazy val offsetItems: Seq[OffsetField] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    read[Seq[OffsetFieldItem]](
      s"""${properties.get("offsetFields").fold("[]") { values => values.toString }}""""
    ).map(item => new OffsetField(item.offsetField,
      OffsetOperator.withName(item.offsetOperator),
      item.offsetValue.notBlank))
  }

  def init(): DistributedMonad[DStream] = {
    val inputSentences = InputSentences(
      query,
      OffsetConditions(
        offsetItems,
        limitRecords),
      initialSentence.fold(Seq.empty[String]) { sentence => Seq(sentence) }
    )
    val datasourceProperties = {
      getCustomProperties ++
        properties.mapValues(value => value.toString) ++ Map(ConfigParameters.ZookeeperPath -> zookeeperPath)
    }.filter(_._2.nonEmpty)

    ssc.get.addStreamingListener(new StreamingListenerStop)
    import scala.concurrent.ExecutionContext.Implicits.global
    val schedulerSystem = ActorSystem("SchedulerSystem",
      ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on")))
    CrossdataInputStep.lastFinishTask = Option(schedulerSystem.scheduler.schedule(1000 milli, 1000 milli)({
      if (CrossdataInputStep.stopSparkContexts) {
        log.info("Stopping Spark contexts")
        ssc.get.stop(stopSparkContext = true, stopGracefully = true)
        CrossdataInputStep.stopSparkContexts = false
        CrossdataInputStep.lastFinishTask.foreach(_.cancel())
        CrossdataInputStep.lastFinishTask = None
      }
      if (CrossdataInputStep.finishApplication) {
        log.info("Finishing application")
        System.exit(0)
      }
    }))

    DatasourceUtils.createStream(ssc.get, inputSentences, datasourceProperties, sparkSession)
  }

  class StreamingListenerStop extends StreamingListener {

    override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
      if (batchCompleted.batchInfo.numRecords == 0) {
        if (finishApplicationWhenEmpty)
          CrossdataInputStep.finishApplication = true
        if (stopContexts)
          CrossdataInputStep.stopSparkContexts = true
      }
    }
  }
}

object CrossdataInputStep {

  var finishApplication = false
  var stopSparkContexts = false
  var lastFinishTask: Option[Cancellable] = None

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}
