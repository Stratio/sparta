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
package com.stratio.sparta.driver

import java.io._

import com.stratio.sparta.driver.cube.CubeMaker
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.driver.helper.SchemaHelper
import com.stratio.sparta.driver.helper.SchemaHelper._
import com.stratio.sparta.driver.service.RawDataStorageService
import com.stratio.sparta.driver.stage._
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.driver.writer.{StreamWriter, StreamWriterOptions}
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.models.policy._
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, PolicyUtils}
import org.apache.spark.SparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.util.{Failure, Success, Try}

class SpartaPipeline(val policy: PolicyModel) extends PolicyUtils
  with CheckpointUtils
  with InputStage
  with OutputStage
  with ParserStage
  with CubeStage
  with ZooKeeperError {

  private val ReflectionUtils = SpartaPipeline.ReflectionUtils

  def run(sc: SparkContext): StreamingContext = {
    clearError(policy.id)
    val checkpointPolicyPath = checkpointPath(policy)
    val sparkStreamingWindow = AggregationTime.parseValueToMilliSeconds(policy.sparkStreamingWindow)
    val ssc = sparkStreamingInstance(Duration(sparkStreamingWindow), checkpointPolicyPath, policy.remember)
    val parserSchemas = SchemaHelper.getSchemasFromParsers(policy.transformations, Input.InitSchema)
    val parsers = parserStage(ReflectionUtils, parserSchemas).sorted
    val schemaWithoutRaw = getSchemaWithoutRaw(parserSchemas)
    val cubes = cubeStage(ReflectionUtils, schemaWithoutRaw)
    val cubesSchemas = SchemaHelper.getSchemasFromCubes(cubes, policy.cubes)
    val cubesOutputs = outputStage(cubesSchemas, ReflectionUtils)
    val cubesTriggersSchemas = SchemaHelper.getSchemasFromCubeTrigger(policy.cubes, policy.outputs)
    val cubesTriggersOutputs = outputStage(cubesTriggersSchemas, ReflectionUtils)
    val streamTriggersSchemas = SchemaHelper.getSchemasFromTriggers(policy.streamTriggers, policy.outputs)
    val streamTriggersOutputs = outputStage(streamTriggersSchemas, ReflectionUtils)
    cubesOutputs.foreach(output => output.setup())

    val input = inputStage(ssc.get, ReflectionUtils)
    val inputDStream = inputStreamStage(ssc.get, input)

    saveRawData(policy.rawData, inputDStream)

    val parsedData = SpartaPipeline.applyParsers(inputDStream, parsers)

    triggerStage(policy.streamTriggers)
      .groupBy(trigger => (trigger.overLast, trigger.computeEvery))
      .foreach { case ((overLast, computeEvery), triggers) =>
        getStreamWriter(
          triggers,
          streamTriggersSchemas,
          overLast,
          computeEvery,
          sparkStreamingWindow,
          schemaWithoutRaw,
          streamTriggersOutputs
        ).write(parsedData)
      }

    val dataCube = CubeMaker(cubes).setUp(parsedData)
    dataCube.foreach { case (cubeName, aggregatedData) =>
      getCubeWriter(cubeName,
        cubes,
        cubesSchemas,
        cubesTriggersSchemas,
        policy.cubes,
        cubesOutputs,
        cubesTriggersOutputs
      ).write(aggregatedData)
    }
    ssc.get
  }

  def saveRawData(rawModel: RawDataModel, input: DStream[Row]): Unit =
    if (rawModel.enabled.toBoolean) {
      require(!rawModel.path.equals("default"), "The parquet path must be set")
      RawDataStorageService.save(input, rawModel.path)
    }

  def getStreamWriter(triggers: Seq[Trigger],
                      tableSchemas: Seq[SpartaSchema],
                      overLast: Option[String],
                      computeEvery: Option[String],
                      sparkStreamingWindow: Long,
                      initSchema: StructType,
                      outputs: Seq[Output]): StreamWriter = {
    val writerOp = StreamWriterOptions(overLast, computeEvery, sparkStreamingWindow, initSchema)
    StreamWriter(triggers, tableSchemas, writerOp, outputs)
  }

}

object SpartaPipeline extends PolicyUtils {

  lazy val ReflectionUtils = new ReflectionUtils

  def apply(policy: PolicyModel): SpartaPipeline = new SpartaPipeline(policy)

  def getSparkConfigs(policy: PolicyModel, methodName: String, suffix: String): Map[String, String] = {
    log.info("Initializing reflection")
    policy.outputs.flatMap(o => {
      val clazzToInstance = ReflectionUtils.getClasspathMap.getOrElse(o.`type` + suffix, o.`type` + suffix)
      val clazz = Class.forName(clazzToInstance)
      clazz.getMethods.find(p => p.getName == methodName) match {
        case Some(method) =>
          method.setAccessible(true)
          method.invoke(clazz, o.configuration.asInstanceOf[Map[String, Serializable]])
            .asInstanceOf[Seq[(String, String)]]
        case None =>
          Seq()
      }
    }).toMap
  }

  def applyParsers(input: DStream[Row], parsers: Seq[Parser]): DStream[Row] = {
    if (parsers.isEmpty) input
    else input.mapPartitions(rows => rows.flatMap(row => executeParsers(row, parsers)), preservePartitioning = true)
  }

  def executeParsers(row: Row, parsers: Seq[Parser]): Seq[Row] = {
    if (parsers.size == 1) parseEvent(row, parsers.head, removeRaw = true)
    else parseEvent(row, parsers.head).flatMap(eventParsed => executeParsers(eventParsed, parsers.drop(1)))
  }

  def parseEvent(row: Row, parser: Parser, removeRaw: Boolean = false): Seq[Row] =
    Try {
      parser.parse(row, removeRaw)
    } match {
      case Success(eventParsed) =>
        eventParsed
      case Failure(exception) =>
        val error = s"Failure[Parser]: ${row.mkString(",")} | Message: ${exception.getLocalizedMessage}" +
          s" | Parser: ${parser.getClass.getSimpleName}"
        log.error(error, exception)
        Seq.empty[Row]
    }

}
