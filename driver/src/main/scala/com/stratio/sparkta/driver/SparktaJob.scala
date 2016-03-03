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
import com.stratio.sparkta.driver.factory.SparkContextFactory._
import com.stratio.sparkta.driver.helper.SchemaHelper
import com.stratio.sparkta.driver.helper.SchemaHelper._
import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.driver.util.ReflectionUtils
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.constants.ErrorCodes
import com.stratio.sparkta.serving.core.dao.ErrorDAO
import com.stratio.sparkta.serving.core.models._
import org.apache.spark.SparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.json4s.native.Serialization.write

import scala.util.{Try, _}

class SparktaJob(policy: AggregationPoliciesModel) extends SLF4JLogging {

  private val ReflectionUtils = SparktaJob.ReflectionUtils

  def run(sc: SparkContext): StreamingContext = {
    val checkpointPolicyPath = policy.checkpointPath.concat(File.separator).concat(policy.name)
    val ssc = sparkStreamingInstance(new Duration(policy.sparkStreamingWindow), checkpointPolicyPath)
    val parserSchemas = SchemaHelper.getSchemasFromParsers(policy.transformations, Input.InitSchema)
    val parsers = SparktaJob.getParsers(policy, ReflectionUtils, parserSchemas).sorted
    val cubes = SparktaJob.getCubes(policy, ReflectionUtils, getSchemaWithoutRaw(parserSchemas))
    val cubeSchemas = SchemaHelper.getSchemasFromCubes(cubes, policy.cubes, policy.outputs)
    val outputs = SparktaJob.getOutputs(policy, cubeSchemas, ReflectionUtils)

    outputs.foreach(output => output.setup())

    val inputDStream = SparktaJob.getInput(policy, ssc.get, ReflectionUtils)

    SparktaJob.saveRawData(policy.rawData, inputDStream)

    val dataParsed = SparktaJob.applyParsers(inputDStream, parsers)
    val dataCube = CubeMaker(cubes).setUp(dataParsed)

    dataCube.foreach { case (cubeName, aggregatedData) =>
      SparktaJob.getCubeWriter(cubeName, cubes, cubeSchemas, policy.cubes, outputs).write(aggregatedData)
    }
    ssc.get
  }
}

object SparktaJob extends SLF4JLogging with SparktaSerializer {

  lazy val ReflectionUtils = new ReflectionUtils

  def apply(policy: AggregationPoliciesModel): SparktaJob = new SparktaJob(policy)

  def getInput(policy: AggregationPoliciesModel, ssc: StreamingContext,
               refUtils: ReflectionUtils): DStream[Row] = {
    Try(createInput(policy, ssc, refUtils)) match {
      case Success(input) =>
        log.debug(s"Input: ${policy.input.get.`type`} created correctly.")
        input
      case Failure(ex) =>
        throw SparktaJob.logAndCreateEx(
          s"Something gone wrong creating the input: ${policy.input.get.name}. Please re-check the policy.",
          write(policy.input.get), ex, policy.id.get, ErrorCodes.Policy.ParsingInput)
    }
  }

  private def createInput(policy: AggregationPoliciesModel, ssc: StreamingContext,
                          refUtils: ReflectionUtils): DStream[Row] = {
    require(policy.input.isDefined, "You need at least one input in your policy")
    val inputInstance = refUtils.tryToInstantiate[Input](policy.input.get.`type` + Input.ClassSuffix, (c) =>
      refUtils.instantiateParameterizable[Input](c, policy.input.get.configuration))
    inputInstance.setUp(ssc, policy.storageLevel.get)
  }

  def getParsers(policy: AggregationPoliciesModel,
                 refUtils: ReflectionUtils,
                 schemas: Map[String, StructType]): Seq[Parser] =
    policy.transformations.map(parser => createParser(parser, refUtils, policy.id.get, schemas))

  private def createParser(model: TransformationsModel,
                           refUtils: ReflectionUtils,
                           policyId: String,
                           schemas: Map[String, StructType]): Parser = {
    Try {
      val outputFieldsNames = model.outputFieldsTransformed.map(_.name)
      val schema = schemas.getOrElse(model.name, throw new Exception("Can not find transformation schema"))
      refUtils.tryToInstantiate[Parser](model.`type` + Parser.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Integer],
          classOf[String],
          classOf[Seq[String]],
          classOf[StructType],
          classOf[Map[String, Serializable]])
          .newInstance(model.name, model.order, model.inputField, outputFieldsNames, schema, model.configuration)
          .asInstanceOf[Parser])
    } match {
      case Success(transformer) =>
        log.debug(s"Parser: ${model.`type`} created correctly.")
        transformer
      case Failure(ex) =>
        throw SparktaJob.logAndCreateEx(
          s"Something gone wrong creating the parser: ${model.`type`}. Please re-check the policy.",
          write(model), ex, policyId, ErrorCodes.Policy.ParsingParser)
    }
  }

  def applyParsers(input: DStream[Row], parsers: Seq[Parser]): DStream[Row] = {
    if (parsers.isEmpty) input
    else input.mapPartitions(rows => rows.flatMap(row => executeParsers(row, parsers)), true)
  }

  def executeParsers(row: Row, parsers: Seq[Parser]): Option[Row] = {
    if (parsers.size == 1) parseEvent(row, parsers.head, true)
    else parseEvent(row, parsers.head).flatMap(eventParsed => executeParsers(eventParsed, parsers.drop(1)))
  }

  def parseEvent(row: Row, parser: Parser, removeRaw: Boolean = false): Option[Row] =
    Try(parser.parse(row, removeRaw)) match {
      case Success(okEvent) =>
        Some(okEvent)
      case Failure(exception) =>
        val error = s"Failure[Parser]: ${row.mkString(",")} | Message: ${exception.getLocalizedMessage}" +
          s" | Parser: ${parser.getClass.getSimpleName}"
        log.error(error, exception)
        None
    }

  def getOperators(operatorsModel: Seq[OperatorModel],
                   refUtils: ReflectionUtils,
                   policyId: String,
                   initSchema: StructType): Seq[Operator] =
    operatorsModel.map(operator => createOperator(operator, refUtils, policyId, initSchema))

  private def createOperator(model: OperatorModel,
                             refUtils: ReflectionUtils,
                             policyId: String,
                             initSchema: StructType): Operator =
    Try(
      refUtils.tryToInstantiate[Operator](model.`type` + Operator.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[StructType],
          classOf[Map[String, Serializable]])
          .newInstance(model.name, initSchema, model.configuration).asInstanceOf[Operator])
    ) match {
      case Success(transformer) =>
        log.debug(s"Operator: ${model.`type`} created correctly.")
        transformer
      case Failure(ex) =>
        throw SparktaJob.logAndCreateEx(
          s"Something gone wrong creating the operator: ${model.`type`}. Please re-check the policy.",
          write(model), ex, policyId, ErrorCodes.Policy.ParsingOperator)
    }

  def getOutputs(policy: AggregationPoliciesModel,
                 cubesOperatorsSchema: Seq[TableSchema],
                 refUtils: ReflectionUtils): Seq[Output] = policy.outputs.map(o => {
    val schemasAssociated = cubesOperatorsSchema.filter(tableSchema => tableSchema.outputs.contains(o.name))
    createOutput(o, schemasAssociated, refUtils, policy.version)
  })

  def createOutput(model: PolicyElementModel, schemasAssociated: Seq[TableSchema], refUtils: ReflectionUtils,
                   version: Option[Int]): Output = {
    Try {
      refUtils.tryToInstantiate[Output](model.`type` + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Option[Int]],
          classOf[Map[String, Serializable]],
          classOf[Seq[TableSchema]])
          .newInstance(model.name, version, model.configuration, schemasAssociated)
          .asInstanceOf[Output])
    } match {
      case Success(transformer) =>
        log.debug(s"Output: ${model.`type`} created correctly.")
        transformer
      case Failure(ex) =>
        throw SparktaJob.logAndCreateEx(
          s"Something gone wrong creating the output: ${model.`type`}. Please re-check the policy.",
          write(model), ex, "aa", ErrorCodes.Policy.ParsingOutput)
    }
  }

  def getCubes(policy: AggregationPoliciesModel,
               refUtils: ReflectionUtils,
               initSchema: StructType): Seq[Cube] = {
    require(policy.cubes.nonEmpty, "You need at least one cube in your policy")
    policy.cubes.map(cube => createCube(cube, refUtils, policy.id.get, initSchema: StructType))
  }

  private def createCube(cube: CubeModel,
                         refUtils: ReflectionUtils,
                         policyId: String,
                         initSchema: StructType): Cube =
    Try {
      val name = cube.name
      val dimensions = cube.dimensions.map(dimensionDto => {
        val fieldType = initSchema.find(stField => stField.name == dimensionDto.field).map(_.dataType)
        val defaultType = fieldType.flatMap(field => SchemaHelper.mapSparkTypes.get(field))

        new Dimension(dimensionDto.name,
          dimensionDto.field,
          dimensionDto.precision,
          instantiateDimensionType(dimensionDto.`type`, dimensionDto.configuration, refUtils, defaultType))
      })
      val operators = SparktaJob.getOperators(cube.operators, refUtils, policyId, initSchema)
      val expiringDataConfig = SchemaHelper.getExpiringData(cube.checkpointConfig)

      Cube(name, dimensions, operators, initSchema, expiringDataConfig)
    } match {
      case Success(created) =>
        log.debug(s"Cube: $created created correctly.")
        created
      case Failure(ex) =>
        throw SparktaJob.logAndCreateEx(
          s"Something gone wrong creating the cube: ${cube.name}. Please re-check the policy.",
          write(cube), ex, policyId, ErrorCodes.Policy.ParsingCube)
    }

  //scalastyle:off
  private def instantiateDimensionType(dimensionType: String,
                                       configuration: Option[Map[String, String]],
                                       refUtils: ReflectionUtils,
                                       defaultType: Option[TypeOp]): DimensionType =
    refUtils.tryToInstantiate[DimensionType](dimensionType + Dimension.FieldClassSuffix, (c) => {
      (configuration, defaultType) match {
        case (Some(conf), Some(defType)) =>
          c.getDeclaredConstructor(classOf[Map[String, Serializable]], classOf[TypeOp])
            .newInstance(conf, defType).asInstanceOf[DimensionType]
        case (Some(conf), None) =>
          c.getDeclaredConstructor(classOf[Map[String, Serializable]]).newInstance(conf).asInstanceOf[DimensionType]
        case (None, Some(defType)) =>
          c.getDeclaredConstructor(classOf[TypeOp]).newInstance(defType).asInstanceOf[DimensionType]
        case (None, None) =>
          c.getDeclaredConstructor().newInstance().asInstanceOf[DimensionType]
      }
    })

  //scalastyle:on

  def saveRawData(rawModel: RawDataModel, input: DStream[Row]): Unit =
    if (rawModel.enabled.toBoolean) {
      require(!rawModel.path.equals("default"), "The parquet path must be set")
      sparkSqlContextInstance match {
        case Some(sqlContext) =>
          RawDataStorageService.save(sqlContext, input, rawModel.path)
        case None =>
          log.warn("Impossible to save raw data because sqlContext is empty")
      }
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
      val isAutoCalculatedId = writerModel.isAutoCalculatedId.getOrElse(false)

      WriterOptions(writerOutputs, dateType, fixedMeasures, isAutoCalculatedId)
    }

  def getSparkConfigs(policy: AggregationPoliciesModel, methodName: String, suffix: String,
                      refUtils: Option[ReflectionUtils] = None): Map[String, String] = {
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

  def logAndCreateEx(message: String,
                     json: String,
                     ex: Throwable,
                     policyId: String,
                     code: Int): IllegalArgumentException = {
    log.error(message)
    log.error(s"JSON: $json")
    ex.getCause match {
      case cause: ClassNotFoundException =>
        log.error("The component couldn't be found in classpath. Please check the type.")
    }
    saveErrorZK(policyId, code)
    new IllegalArgumentException(message, ex)
  }

  def saveErrorZK(policyId: String, code: Int): Unit = ErrorDAO().dao.create(policyId, code.toString)
}
