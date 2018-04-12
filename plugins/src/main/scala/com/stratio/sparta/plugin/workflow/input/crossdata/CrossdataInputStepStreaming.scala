/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.crossdata

import java.io.{Serializable => JSerializable}

import akka.actor.{ActorSystem, Cancellable}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.{SchemaHelper, SecurityHelper}
import com.stratio.sparta.plugin.models.OffsetFieldItem
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
import scala.util.{Failure, Properties, Success, Try}

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
      s"""${properties.getString("offsetFields", None).notBlank.fold("[]") { values => values.toString }}""".stripMargin

    read[Seq[OffsetFieldItem]](offsetFields).map(item => new OffsetField(item.offsetField,
      OffsetOperator.withName(item.offsetOperator),
      item.offsetValue.notBlank))
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (query.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input sql query cannot be empty"
      )

    if (query.nonEmpty && !validateSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input sql query is invalid"
      )

    if (offsetItems.nonEmpty && offsetItems.exists(offsetField => offsetField.name.isEmpty))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: there are offset items with an incorrect definition"
      )

    validation
  }

  def init(): DistributedMonad[DStream] = {
    require(query.nonEmpty, "The input query can not be empty")
    require(validateSql, "The input query is invalid")
    require(offsetItems.isEmpty || offsetItems.forall(offsetField => offsetField.name.nonEmpty),
      "There are offset items with an incorrect definition")

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

    DatasourceUtils.createStream(ssc.get, inputSentences, datasourceProperties, sparkSession).transform{ rdd =>
      SchemaHelper.getSchemaFromSessionOrRdd(xDSession, name, rdd)
        .foreach(schema => xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(name))
      rdd
    }
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

  def validateSql: Boolean =
    Try(xDSession.sessionState.sqlParser.parsePlan(query)) match {
      case Success(_) =>
        true
      case Failure(e) =>
        log.error(s"$name invalid sql", e)
        false
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
