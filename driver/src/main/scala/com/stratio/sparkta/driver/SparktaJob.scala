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

package com.stratio.sparkta.driver

import java.io._

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.aggregator.{Cube, CubeMaker, CubeWriter, WriterOptions}
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.helper.SchemaHelper
import com.stratio.sparkta.driver.helper.SchemaHelper._
import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.driver.util.ReflectionUtils
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, CubeModel, OperatorModel}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.util._

object SparktaJob extends SLF4JLogging {

  def run(sc: SparkContext, apConfig: AggregationPoliciesModel): StreamingContext = {
    val checkpointPolicyPath = apConfig.checkpointPath.concat(File.separator).concat(apConfig.name)
    val reflectionUtils = new ReflectionUtils
    val ssc = SparkContextFactory.sparkStreamingInstance(
      new Duration(apConfig.sparkStreamingWindow), checkpointPolicyPath)
    val parsers = SparktaJob.getParsers(apConfig, reflectionUtils).sortWith((parser1, parser2) =>
      parser1.getOrder < parser2.getOrder)
    val cubes = SparktaJob.getCubes(apConfig, reflectionUtils)
    val cubeSchemas = SchemaHelper.getSchemasFromCubes(cubes, apConfig.cubes, apConfig.outputs)
    val outputs = SparktaJob.getOutputs(apConfig, cubeSchemas, reflectionUtils)
    outputs.foreach(output => output.setup())
    val inputDStream = SparktaJob.getInput(apConfig, ssc.get, reflectionUtils)
    SparktaJob.saveRawData(apConfig, inputDStream)
    val dataParsed = applyParsers(inputDStream, parsers)
    val dataCube = CubeMaker(cubes).setUp(dataParsed)

    dataCube.foreach { case (cubeName, aggregatedData) =>
      getCubeWriter(cubeName, cubes, cubeSchemas, apConfig.cubes, outputs).write(aggregatedData)
    }
    ssc.get
  }

  def getInput(apConfig: AggregationPoliciesModel, ssc: StreamingContext, refUtils: ReflectionUtils): DStream[Event] = {
    val inputInstance = refUtils.tryToInstantiate[Input](apConfig.input.get.`type` + Input.ClassSuffix, (c) =>
      refUtils.instantiateParameterizable[Input](c, apConfig.input.get.configuration))
    inputInstance.setUp(ssc, apConfig.storageLevel.get)
  }

  def getParsers(apConfig: AggregationPoliciesModel, refUtils: ReflectionUtils): Seq[Parser] =
    apConfig.transformations.map(parser =>
      refUtils.tryToInstantiate[Parser](parser.`type` + Parser.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Integer],
          classOf[String],
          classOf[Seq[String]],
          classOf[Map[String, Serializable]])
          .newInstance(parser.name, parser.order, parser.inputField, parser.outputFields, parser.configuration)
          .asInstanceOf[Parser]))

  def applyParsers(input: DStream[Event], parsers: Seq[Parser]): DStream[Event] = {
    if (parsers.isEmpty)
      input
    else {
      input.flatMap(event => executeParsers(event, parsers))
    }
  }

  def executeParsers(event: Event, parsers: Seq[Parser]): Option[Event] =
    parsers.headOption.fold(Option(event)) { parser => {
      parseEvent(event, parser).flatMap(eventParsed => executeParsers(eventParsed, parsers.drop(1)))
    }
    }

  def parseEvent(event: Event, parser: Parser): Option[Event] = {

    Try(parser.parse(event)) match {
      case Success(okEvent) => Some(okEvent)
      case Failure(exception) =>
        val error = s"Failure[Parser]: ${event.toString} | Message: ${exception.getLocalizedMessage}" +
          s" | Parser: ${parser.getClass.getSimpleName}"
        log.error(error, exception)
        None
    }
  }

  def getOperators(operatorsModel: Seq[OperatorModel], refUtils: ReflectionUtils): Seq[Operator] =
    operatorsModel.map(operator => createOperator(operator, refUtils))

  private def createOperator(operatorModel: OperatorModel, refUtils: ReflectionUtils): Operator =
    refUtils.tryToInstantiate[Operator](operatorModel.`type` + Operator.ClassSuffix, (c) =>
      c.getDeclaredConstructor(
        classOf[String],
        classOf[Map[String, Serializable]]
      ).newInstance(operatorModel.name, operatorModel.configuration).asInstanceOf[Operator])

  def getOutputs(apConfig: AggregationPoliciesModel,
                 cubesOperatorsSchema: Seq[TableSchema],
                 refUtils: ReflectionUtils): Seq[Output] = {
    apConfig.outputs.map(o => {
      val schemasAssociated = cubesOperatorsSchema.filter(tableSchema => tableSchema.outputs.contains(o.name))
      refUtils.tryToInstantiate[Output](o.`type` + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Option[Int]],
          classOf[Map[String, Serializable]],
          classOf[Seq[TableSchema]])
          .newInstance(o.name, apConfig.version, o.configuration, schemasAssociated)
          .asInstanceOf[Output])
    })
  }

  def getCubes(apConfig: AggregationPoliciesModel, refUtils: ReflectionUtils): Seq[Cube] =
    apConfig.cubes.map(cube => {
      val name = cube.name
      val dimensions = cube.dimensions.map(dimensionDto => {
        new Dimension(dimensionDto.name,
          dimensionDto.field,
          dimensionDto.precision,
          instantiateDimensionType(dimensionDto.`type`, dimensionDto.configuration, refUtils))
      })
      val operators = SparktaJob.getOperators(cube.operators, refUtils)
      val expiringDataConfig = SchemaHelper.getExpiringData(cube.checkpointConfig)

      Cube(name, dimensions, operators, cube.checkpointConfig.interval, expiringDataConfig)
    })

  private def instantiateDimensionType(dimensionType: String, configuration: Option[Map[String, String]],
                                       refUtils: ReflectionUtils): DimensionType =
    refUtils.tryToInstantiate[DimensionType](dimensionType + Dimension.FieldClassSuffix, (c) => {
      configuration match {
        case Some(conf) => c.getDeclaredConstructor(classOf[Map[String, Serializable]])
          .newInstance(conf).asInstanceOf[DimensionType]
        case None => c.getDeclaredConstructor().newInstance().asInstanceOf[DimensionType]
      }
    })

  def saveRawData(apConfig: AggregationPoliciesModel, input: DStream[Event], sqc: Option[SQLContext] = None): Unit =
    if (apConfig.rawData.enabled.toBoolean) {
      require(!apConfig.rawData.path.equals("default"), "The parquet path must be set")
      val sqlContext = sqc.getOrElse(SparkContextFactory.sparkSqlContextInstance.get)
      def rawDataStorage: RawDataStorageService =
        new RawDataStorageService(sqlContext, apConfig.rawData.path)
      rawDataStorage.save(input)
    }

  def getCubeWriter(cubeName: String,
                    cubes: Seq[Cube],
                    schemas: Seq[TableSchema],
                    cubeModels: Seq[CubeModel],
                    outputs: Seq[Output]): CubeWriter = {
    val cubeWriter = cubes.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cube in the cube writer"))
    val schemaWriter = schemas.find(schema => schema.tableName == cubeName)
      .getOrElse(throw new Exception("Is mandatory one schema in the cube writer"))
    val cubeModel = cubeModels.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cubeModel in the cube writer"))
    val writerOp = getWriterOptions(cubeName, outputs, cubeModel)

    CubeWriter(cubeWriter, schemaWriter, writerOp, outputs)
  }

  def getWriterOptions(cubeName: String, outputsWriter: Seq[Output], cubeModel: CubeModel): WriterOptions =
    cubeModel.writer.fold(WriterOptions(outputsWriter.map(_.name))) { writerModel =>
      val writerOutputs = if (writerModel.outputs.isEmpty) outputsWriter.map(_.name) else writerModel.outputs
      val dateType = Output.getTimeTypeFromString(cubeModel.writer.fold(DefaultTimeStampTypeString) { options =>
        options.dateType.getOrElse(DefaultTimeStampTypeString)
      })
      val fixedMeasures: MeasuresValues = writerModel.fixedMeasure.fold(MeasuresValues(Map.empty)) { fixedMeasure =>
        val fixedMeasureSplitted = fixedMeasure.split(CubeWriter.FixedMeasureSeparator)
        MeasuresValues(Map(fixedMeasureSplitted.head -> Some(fixedMeasureSplitted.last)))
      }
      val isAutoCalculatedId = writerModel.isAutoCalculateId.getOrElse(false)

      WriterOptions(writerOutputs, dateType, fixedMeasures, isAutoCalculatedId)
    }
}
