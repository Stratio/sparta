/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sql

import java.io.{Serializable => JSerializable}

import akka.actor.{ActorSystem, Cancellable}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.constants.SdkConstants.WorkflowIdKey
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.OneTransactionOffsetManager
import com.stratio.sparta.plugin.helper.{SchemaHelper, SecurityHelper}
import com.stratio.sparta.plugin.models.{OffsetFieldItem, SqlModel}
import com.stratio.sparta.plugin.models.SerializationImplicits._
import com.stratio.sparta.serving.core.helpers.ErrorValidationsHelper
import com.stratio.sparta.serving.core.workflow.lineage.CrossdataLineage
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.datasource.DatasourceUtils
import org.apache.spark.streaming.datasource.config.ConfigParameters
import org.apache.spark.streaming.datasource.models._
import org.apache.spark.streaming.datasource.receiver.DatasourceDStream
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted}
import org.json4s.jackson.Serialization.read

import scala.concurrent.duration._
import scala.util.{Properties, Try}

class SQLInputStepStreaming(
                             name: String,
                             outputOptions: OutputOptions,
                             ssc: Option[StreamingContext],
                             xDSession: XDSession,
                             properties: Map[String, JSerializable]
                           )
  extends SQLInputStep[DStream](name, outputOptions, ssc, xDSession, properties)
    with CrossdataLineage
    with SLF4JLogging
    with OneTransactionOffsetManager {

  var inputData: Option[DatasourceDStream[SparkSession]] = None

  lazy val finishApplicationWhenEmpty: Boolean = Try(properties.getBoolean("finishAppWhenEmpty", default = false))
    .getOrElse(false)
  lazy val limitRecords: Option[Long] = Try(properties.getLong("limitRecords", None)).getOrElse(None)
  lazy val stopContexts: Boolean = Try(properties.getBoolean("stopContexts", default = false)).getOrElse(false)
  lazy val zookeeperConnection: String = Properties.envOrElse("SPARTA_ZOOKEEPER_CONNECTION_STRING", "localhost:2181")
  lazy val zookeeperPath: String = Properties.envOrElse("SPARTA_ZOOKEEPER_PATH", "/stratio/sparta") +
    s"/crossdata/offsets/${properties.getString(WorkflowIdKey)}/${name}"
  lazy val continuousSentences: Seq[SqlModel] = {
    val sentences =
      s"""${
        properties.getString("continuousSentences", None).notBlank.fold("[]") {
          values => values.toString
        }
      }""".stripMargin
    read[Seq[SqlModel]](sentences)
  }
  private val storeOffsetAfterWritingOutputs = Try(properties.getString("storeOffsetAfterWritingOutputs", "false").toBoolean).getOrElse(false)

  lazy val offsetItems: Seq[OffsetField] = {
    val offsetFields =
      s"""${properties.getString("offsetFields", None).notBlank.fold("[]") { values => values.toString }}""".stripMargin

    read[Seq[OffsetFieldItem]](offsetFields).map(item => new OffsetField(item.offsetField,
      OffsetOperator.withName(item.offsetOperator),
      item.offsetValue.notBlank))
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    val preValidation = super.validate(options)

    val validation = ErrorValidationsHelper.validate(Seq(
      {
        val (isValid, errorString) = isValidContinuousSentences
        !isValid -> errorString
      },
      (offsetItems.nonEmpty && offsetItems.exists(offsetField => offsetField.name.isEmpty)) -> "Missing fields in offset definition"
    ), name)

    ErrorValidations(
      valid = preValidation.valid && validation.valid,
      messages = preValidation.messages ++ validation.messages
    )
  }

  override def lineageCatalogProperties(): Map[String, Seq[String]] = getCrossdataLineageProperties(xDSession, query)


  def init(): DistributedMonad[DStream] = {
    require(query.nonEmpty, "The input query cannot be empty")
    require(isValidQuery(query), "The input query is not a valid SQL")
    val (isValid, errorString) = isValidContinuousSentences
    require(isValid, errorString)
    require(offsetItems.isEmpty || offsetItems.forall(offsetField => offsetField.name.nonEmpty),
      "Missing fields in offset definition")

    val inputSentences = InputSentences(
      query = query,
      offsetConditions = OffsetConditions(
        offsetItems,
        limitRecords
      ),
      initialStatements = Seq.empty[String],
      continuousStatements = continuousSentences.map(sentence => sentence.query)
    )
    val datasourceProperties = {
      getCustomProperties ++
        properties.mapValues(value => value.toString) ++
        Map(
          ConfigParameters.ZookeeperConnection -> zookeeperConnection,
          ConfigParameters.ZookeeperPath -> zookeeperPath,
          ConfigParameters.StoreOffsetAfterWritingOutputs -> storeOffsetAfterWritingOutputs.toString,
          ConfigParameters.StorageLevelKey -> "MEMORY_ONLY_SER"
        )
      }.filter(_._2.nonEmpty)

    ssc.get.addStreamingListener(new StreamingListenerStop)
    import scala.concurrent.ExecutionContext.Implicits.global
    val schedulerSystem = ActorSystem("SchedulerSystem",
      ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on")))
    SQLInputStepStreaming.lastFinishTask = Option(schedulerSystem.scheduler.schedule(1000 milli, 1000 milli)({
      if (SQLInputStepStreaming.stopSparkContexts) {
        log.info("Stopping Spark contexts")
        ssc.get.stop(stopSparkContext = true, stopGracefully = true)
        SQLInputStepStreaming.stopSparkContexts = false
        SQLInputStepStreaming.lastFinishTask.foreach(_.cancel())
        SQLInputStepStreaming.lastFinishTask = None
      }
      if (SQLInputStepStreaming.finishApplication) {
        log.info("Finishing application")
        System.exit(0)
      }
    }))

    val datasourceDstream = DatasourceUtils.createStream(ssc.get, inputSentences, datasourceProperties, sparkSession)

    datasourceDstream match {
      case dsdstream: DatasourceDStream[SparkSession] =>
        inputData = Option(dsdstream)
    }

    datasourceDstream.transform { rdd =>
      SchemaHelper.getSchemaFromSessionOrRdd(xDSession, name, rdd)
        .foreach(schema => xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(name))
      rdd
    }
  }

  override val executeOffsetCommit: Boolean = storeOffsetAfterWritingOutputs


  override def commitOffsets(): Unit = {
    inputData.foreach { inputDStream =>
      inputDStream.foreachRDD { _ =>
        inputDStream.commitOffsets()
      }
    }
  }

  class StreamingListenerStop extends StreamingListener {

    override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
      if (batchCompleted.batchInfo.numRecords == 0) {
        if (finishApplicationWhenEmpty)
          SQLInputStepStreaming.finishApplication = true
        if (stopContexts)
          SQLInputStepStreaming.stopSparkContexts = true
      }
    }
  }

  private def isValidContinuousSentences: (Boolean, String) = {
    val (valid, queryIdx) = continuousSentences.map(_.query).zipWithIndex
      .map { case (q, idx) => (isValidQuery(q), idx + 1) }
      .foldLeft((true, Seq.empty[Int])) {
        case ((stillValid, badQueriesIdx), (isValid, idx)) if !isValid =>
          (stillValid && isValid, badQueriesIdx :+ idx)
        case ((stillValid, badQueriesIdx), _) =>
          (stillValid, badQueriesIdx)
      }
    val errorString = if (!valid) s"Invalid fixed queries nº: ${queryIdx.mkString(",")}" else ""
    (valid, errorString)
  }

}

object SQLInputStepStreaming {

  var finishApplication = false
  var stopSparkContexts = false
  var lastFinishTask: Option[Cancellable] = None

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}
