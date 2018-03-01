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
import com.stratio.sparta.plugin.workflow.input.crossdata.models.OffsetFieldItem
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, InputStep, OutputOptions}
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

class CrossdataInputStepStreaming(
                                   name: String,
                                   outputOptions: OutputOptions,
                                   ssc: Option[StreamingContext],
                                   xDSession: XDSession,
                                   properties: Map[String, JSerializable]
                                 )
  extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val query = properties.getString("query", "").trim
  lazy val finishApplicationWhenEmpty = Try(properties.getBoolean("finishAppWhenEmpty", default = false))
    .getOrElse(false)
  lazy val limitRecords = Try(properties.getLong("limitRecords", None)).getOrElse(None)
  lazy val stopContexts = Try(properties.getBoolean("stopContexts", default = false)).getOrElse(false)
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
  lazy val offsetItems = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val offsetFields =
      s"""${properties.getString("offsetFields", None).notBlank.fold("[]") { values => values.toString}}""".stripMargin

    read[Seq[OffsetFieldItem]](offsetFields).map(item => new OffsetField(item.offsetField,
      OffsetOperator.withName(item.offsetOperator),
      item.offsetValue.notBlank))
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (query.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name input query can not be empty"
      )
    if (offsetItems.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name the offsets fields can not be empty"
      )

    validation
  }

  def init(): DistributedMonad[DStream] = {
    require(query.nonEmpty, "The input query can not be empty")

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
    CrossdataInputStepStreaming.lastFinishTask = Option(schedulerSystem.scheduler.schedule(1000 milli, 1000 milli)({
      if (CrossdataInputStepStreaming.stopSparkContexts) {
        log.info("Stopping Spark contexts")
        ssc.get.stop(stopSparkContext = true, stopGracefully = true)
        CrossdataInputStepStreaming.stopSparkContexts = false
        CrossdataInputStepStreaming.lastFinishTask.foreach(_.cancel())
        CrossdataInputStepStreaming.lastFinishTask = None
      }
      if (CrossdataInputStepStreaming.finishApplication) {
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
          CrossdataInputStepStreaming.finishApplication = true
        if (stopContexts)
          CrossdataInputStepStreaming.stopSparkContexts = true
      }
    }
  }

}

object CrossdataInputStepStreaming {

  var finishApplication = false
  var stopSparkContexts = false
  var lastFinishTask: Option[Cancellable] = None

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}
