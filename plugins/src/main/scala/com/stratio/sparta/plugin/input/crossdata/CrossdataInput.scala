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

package com.stratio.sparta.plugin.input.crossdata

import java.io.{Serializable => JSerializable}

import akka.actor.{ActorSystem, Cancellable}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.OutputFormatEnum
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.datasource.DatasourceUtils
import org.apache.spark.streaming.datasource.config.ConfigParameters
import org.apache.spark.streaming.datasource.models._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted}

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.parsing.json.JSONObject
import scala.util.{Properties, Try}

class CrossdataInput(
                      name: String,
                      ssc: StreamingContext,
                      sparkSession: XDSession,
                      properties: Map[String, JSerializable]
                    ) extends Input(name, ssc, sparkSession, properties) with SLF4JLogging {

  private val query = properties.getString("query")
  private val offsetField = properties.getString("offsetField")
  private val offsetOperator = properties.getString("offsetOperator", None)
    .map(operator => OffsetOperator.withName(operator))
  private val offsetValue = properties.getString("offsetValue", None)
  private val finishApplicationWhenEmpty = Try(properties.getBoolean("finishAppWhenEmpty")).getOrElse(false)
  private val fromBeginning = Try(properties.getBoolean("fromBeginning")).toOption.getOrElse(false)
  private val forcedBeginning = Try(properties.getBoolean("forcedBeginning")).toOption
  private val limitRecords = Try(properties.getString("limitRecords", None).map(_.toLong)).getOrElse(None)
  private val rowSeparator = properties.getString("rowSeparator", ",")
  private val outputFormat = OutputFormatEnum.withName(properties.getString("outputFormat", "JSON").toUpperCase)
  private val stopContexts = Try(properties.getBoolean("stopContexts")).getOrElse(false)
  private val stopGracefully = Try(properties.getBoolean("stopGracefully")).getOrElse(true)
  private val zookeeperPath = Properties.envOrElse("SPARTA_ZOOKEEPER_PATH", "/stratio/sparta") + {
    val path = properties.getString("zookeeperPath", "/crossdata/offsets")
    if (path.startsWith("/"))
      path
    else s"/$path"
  }
  private val initialSentence = properties.getString("initialSentence", None).flatMap { sentence =>
    if (sentence.toUpperCase.startsWith("CREATE TEMPORARY TABLE"))
      Option(sentence)
    else {
      val message = s"Invalid sentence: $sentence"
      log.error(message)
      throw new RuntimeException(message)
    }
  }

  def initStream: DStream[Row] = {
    val inputSentences = InputSentences(
      query,
      OffsetConditions(
        OffsetField(offsetField, offsetOperator, offsetValue), fromBeginning, forcedBeginning, limitRecords),
      initialSentence.fold(Seq.empty[String]) { sentence => Seq(sentence) }
    )
    val datasourceProperties = {
      getCustomProperties ++
        properties.mapValues(value => value.toString) ++ Map(ConfigParameters.ZookeeperPath -> zookeeperPath)
    }.filter(_._2.nonEmpty)

    ssc.addStreamingListener(new StreamingListenerStop)
    import scala.concurrent.ExecutionContext.Implicits.global
    val schedulerSystem = ActorSystem("SchedulerSystem",
      ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on")))
    CrossdataInput.lastFinishTask = Option(schedulerSystem.scheduler.schedule(1000 milli, 1000 milli)({
      if (CrossdataInput.stopSparkContexts) {
        log.info("Stopping Spark contexts")
        ssc.stop(stopSparkContext = true, stopGracefully)
        CrossdataInput.stopSparkContexts = false
        CrossdataInput.lastFinishTask.foreach(_.cancel())
        CrossdataInput.lastFinishTask = None
      }
      if (CrossdataInput.finishApplication) {
        log.info("Finishing application")
        System.exit(0)
      }
    }))

    DatasourceUtils.createStream(ssc, inputSentences, datasourceProperties, sparkSession).map { row =>
      if (outputFormat == OutputFormatEnum.JSON)
        Row(convertRowToJSON(row))
      else Row(row.mkString(rowSeparator))
    }
  }

  class StreamingListenerStop extends StreamingListener {

    override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
      if (batchCompleted.batchInfo.numRecords == 0) {
        if (finishApplicationWhenEmpty)
          CrossdataInput.finishApplication = true
        if (stopContexts)
          CrossdataInput.stopSparkContexts = true
      }
    }
  }

  private def convertRowToJSON(row: Row): String = {
    val m = row.getValuesMap(row.schema.fieldNames)
    JSONObject(m).toString()
  }
}

object CrossdataInput {

  var finishApplication = false
  var stopSparkContexts = false
  var lastFinishTask: Option[Cancellable] = None

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataSourceSecurityConf(configuration)
  }
}
