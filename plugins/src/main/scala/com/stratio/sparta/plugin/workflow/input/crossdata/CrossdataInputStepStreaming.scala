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
import com.stratio.sparta.plugin.models.{OffsetFieldItem, SqlModel}
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.InputStep
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

//scalastyle:off
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
  lazy val continuousSentences = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val sentences =
      s"""${properties.getString("continuousSentences", None).notBlank.fold("[]") { values => values.toString }}""".stripMargin

    read[Seq[SqlModel]](sentences)
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

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

    if (query.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the input sql query cannot be empty", name)
      )

    if (query.nonEmpty && !validateSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the input sql query is invalid", name)
      )

    if (initialSentence.nonEmpty && !validateInputSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the initial sql query is invalid", name)
      )

    if (continuousSentences.nonEmpty && !validateContinuousSql)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the continuous sql queries are invalid", name)
      )

    if (offsetItems.nonEmpty && offsetItems.exists(offsetField => offsetField.name.isEmpty))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"there are offset items with an incorrect definition", name)
      )

    if(debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    validation
  }

  def init(): DistributedMonad[DStream] = {
    require(query.nonEmpty, "The input query cannot be empty")
    require(validateSql, "The input query is invalid")
    require(initialSentence.nonEmpty || validateInputSql, "The initial sql is invalid")
    require(continuousSentences.nonEmpty || validateContinuousSql, "The continuous sql is invalid")
    require(offsetItems.isEmpty || offsetItems.forall(offsetField => offsetField.name.nonEmpty),
      "There are offset items with an incorrect definition")

    val inputSentences = InputSentences(
      query = query,
      offsetConditions = OffsetConditions(
        offsetItems,
        limitRecords
      ),
      initialStatements = initialSentence.fold(Seq.empty[String]) { sentence => Seq(sentence) },
      continuousStatements = continuousSentences.map(sentence => sentence.query)
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

  def validateContinuousSql: Boolean =
    Try(continuousSentences.foreach(sql => xDSession.sessionState.sqlParser.parsePlan(sql.query))) match {
      case Success(_) =>
        true
      case Failure(e) =>
        log.error(s"$name invalid continuous sql", e)
        false
    }

  def validateInputSql: Boolean = initialSentence match {
    case Some(sql) =>
      Try(xDSession.sessionState.sqlParser.parsePlan(sql)) match {
        case Success(_) =>
          true
        case Failure(e) =>
          log.error(s"$name invalid sql", e)
          false
      }
    case None =>
      true
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
