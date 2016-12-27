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

import scala.util.{Try, _}
import akka.event.slf4j.SLF4JLogging
import org.apache.spark.SparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.json4s.native.Serialization.write
import com.stratio.sparta.driver.cube.{Cube, CubeMaker}
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.driver.helper.SchemaHelper
import com.stratio.sparta.driver.helper.SchemaHelper._
import com.stratio.sparta.driver.service.RawDataStorageService
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.driver.writer.{CubeWriter, CubeWriterOptions, StreamWriter, StreamWriterOptions}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, DimensionType}
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.pipeline.schema.{SpartaSchema, TypeOp}
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.serving.core.constants.ErrorCodes
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.models.policy._
import com.stratio.sparta.serving.core.models.policy.cube.{CubeModel, OperatorModel}
import com.stratio.sparta.serving.core.models.policy.trigger.TriggerModel
import com.stratio.sparta.serving.core.utils.PolicyUtils

class SpartaJob(policy: PolicyModel) extends SLF4JLogging {

  private val ReflectionUtils = SpartaJob.ReflectionUtils

  def run(sc: SparkContext): StreamingContext = {
    val checkpointPolicyPath = SpartaJob.generateCheckpointPath(policy)
    val sparkStreamingWindow = AggregationTime.parseValueToMilliSeconds(policy.sparkStreamingWindow)
    val ssc = sparkStreamingInstance(Duration(sparkStreamingWindow), checkpointPolicyPath, policy.remember)
    val parserSchemas = SchemaHelper.getSchemasFromParsers(policy.transformations, Input.InitSchema)
    val parsers = SpartaJob.getParsers(policy, ReflectionUtils, parserSchemas).sorted
    val schemaWithoutRaw = getSchemaWithoutRaw(parserSchemas)
    val cubes = SpartaJob.getCubes(policy, ReflectionUtils, schemaWithoutRaw)
    val cubesSchemas = SchemaHelper.getSchemasFromCubes(cubes, policy.cubes)
    val cubesOutputs = SpartaJob.getOutputs(policy, cubesSchemas, ReflectionUtils)
    val cubesTriggersSchemas = SchemaHelper.getSchemasFromCubeTrigger(policy.cubes, policy.outputs)
    val cubesTriggersOutputs = SpartaJob.getOutputs(policy, cubesTriggersSchemas, ReflectionUtils)
    val streamTriggersSchemas = SchemaHelper.getSchemasFromTriggers(policy.streamTriggers, policy.outputs)
    val streamTriggersOutputs = SpartaJob.getOutputs(policy, streamTriggersSchemas, ReflectionUtils)

    cubesOutputs.foreach(output => output.setup())

    val inputDStream = SpartaJob.getInput(policy, ssc.get, ReflectionUtils)

    SpartaJob.saveRawData(policy.rawData, inputDStream)

    val parsedData = SpartaJob.applyParsers(inputDStream, parsers)

    SpartaJob.getTriggers(policy.streamTriggers, policy.id.get)
      .groupBy(trigger => (trigger.overLast, trigger.computeEvery))
      .foreach { case ((overLast, computeEvery), triggers) =>
        SpartaJob.getStreamWriter(
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
      SpartaJob.getCubeWriter(cubeName,
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
}

object SpartaJob extends PolicyUtils {

  lazy val ReflectionUtils = new ReflectionUtils

  def generateCheckpointPath(policy: PolicyModel): String = checkpointPath(policy)

  def apply(policy: PolicyModel): SpartaJob = new SpartaJob(policy)

  def getInput(policy: PolicyModel, ssc: StreamingContext,
               refUtils: ReflectionUtils): DStream[Row] = {
    Try(createInput(policy, ssc, refUtils)) match {
      case Success(input) =>
        log.debug(s"Input: ${policy.input.get.`type`} created correctly.")
        input
      case Failure(ex) =>
        throw SpartaJob.logAndCreateEx(
          s"Something gone wrong creating the input: ${policy.input.get.name}. Please re-check the policy.",
          write(policy.input.get), ex, policy.id.get, ErrorCodes.Policy.ParsingInput)
    }
  }

  private def createInput(policy: PolicyModel, ssc: StreamingContext,
                          refUtils: ReflectionUtils): DStream[Row] = {
    require(policy.input.isDefined, "You need at least one input in your policy")
    val inputInstance = refUtils.tryToInstantiate[Input](policy.input.get.`type` + Input.ClassSuffix, (c) =>
      refUtils.instantiateParameterizable[Input](c, policy.input.get.configuration))
    inputInstance.setUp(ssc, policy.storageLevel.get)
  }

  def getParsers(policy: PolicyModel,
                 refUtils: ReflectionUtils,
                 schemas: Map[String, StructType]): Seq[Parser] =
    policy.transformations.map(parser => createParser(parser, refUtils, policy.id.get, schemas))

  private def createParser(model: TransformationsModel,
                           refUtils: ReflectionUtils,
                           policyId: String,
                           schemas: Map[String, StructType]): Parser = {
    Try {
      val outputFieldsNames = model.outputFieldsTransformed.map(_.name)
      val schema = schemas.getOrElse(model.order.toString, throw new Exception("Can not find transformation schema"))
      refUtils.tryToInstantiate[Parser](model.`type` + Parser.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[Integer],
          classOf[Option[String]],
          classOf[Seq[String]],
          classOf[StructType],
          classOf[Map[String, Serializable]])
          .newInstance(model.order, model.inputField, outputFieldsNames, schema, model.configuration)
          .asInstanceOf[Parser])
    } match {
      case Success(transformer) =>
        log.debug(s"Parser: ${model.`type`} created correctly.")
        transformer
      case Failure(ex) =>
        throw SpartaJob.logAndCreateEx(
          s"Something gone wrong creating the parser: ${model.`type`}. Please re-check the policy.",
          write(model), ex, policyId, ErrorCodes.Policy.ParsingParser)
    }
  }

  def applyParsers(input: DStream[Row], parsers: Seq[Parser]): DStream[Row] = {
    if (parsers.isEmpty) input
    else input.mapPartitions(rows => rows.flatMap(row => executeParsers(row, parsers)), preservePartitioning = true)
  }

  def executeParsers(row: Row, parsers: Seq[Parser]): Option[Row] = {
    if (parsers.size == 1) parseEvent(row, parsers.head, removeRaw = true)
    else parseEvent(row, parsers.head).flatMap(eventParsed => executeParsers(eventParsed, parsers.drop(1)))
  }

  def parseEvent(row: Row, parser: Parser, removeRaw: Boolean = false): Option[Row] =
    Try {
      parser.parse(row, removeRaw)
    } match {
      case Success(eventParsed) =>
        eventParsed
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
        throw SpartaJob.logAndCreateEx(
          s"Something gone wrong creating the operator: ${model.`type`}. Please re-check the policy.",
          write(model), ex, policyId, ErrorCodes.Policy.ParsingOperator)
    }

  def getOutputs(policy: PolicyModel,
                 schemas: Seq[SpartaSchema],
                 refUtils: ReflectionUtils): Seq[Output] = policy.outputs.map(o => {
    val schemasAssociated = schemas.filter(tableSchema => tableSchema.outputs.contains(o.name))
    createOutput(o, schemasAssociated, refUtils, policy.version)
  })

  def createOutput(model: PolicyElementModel, schemasAssociated: Seq[SpartaSchema], refUtils: ReflectionUtils,
                   version: Option[Int]): Output = {
    Try {
      refUtils.tryToInstantiate[Output](model.`type` + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Option[Int]],
          classOf[Map[String, Serializable]],
          classOf[Seq[SpartaSchema]])
          .newInstance(model.name, version, model.configuration, schemasAssociated)
          .asInstanceOf[Output])
    } match {
      case Success(transformer) =>
        log.debug(s"Output: ${model.`type`} created correctly.")
        transformer
      case Failure(ex) =>
        throw SpartaJob.logAndCreateEx(
          s"Something gone wrong creating the output: ${model.`type`}. Please re-check the policy.",
          write(model), ex, "aa", ErrorCodes.Policy.ParsingOutput)
    }
  }

  def getCubes(policy: PolicyModel,
               refUtils: ReflectionUtils,
               initSchema: StructType): Seq[Cube] = {
    policy.cubes.map(cube => createCube(cube, refUtils, policy.id.get, initSchema: StructType))
  }

  private def createCube(cubeModel: CubeModel,
                         refUtils: ReflectionUtils,
                         policyId: String,
                         initSchema: StructType): Cube =
    Try {
      val name = cubeModel.name
      val dimensions = cubeModel.dimensions.map(dimensionDto => {
        val fieldType = initSchema.find(stField => stField.name == dimensionDto.field).map(_.dataType)
        val defaultType = fieldType.flatMap(field => SchemaHelper.mapSparkTypes.get(field))

        new Dimension(dimensionDto.name,
          dimensionDto.field,
          dimensionDto.precision,
          instantiateDimensionType(dimensionDto.`type`, dimensionDto.configuration, refUtils, defaultType))
      })
      val operators = SpartaJob.getOperators(cubeModel.operators, refUtils, policyId, initSchema)
      val expiringDataConfig = SchemaHelper.getExpiringData(cubeModel)
      val triggers = getTriggers(cubeModel.triggers, policyId)

      Cube(name, dimensions, operators, initSchema, expiringDataConfig, triggers)
    } match {
      case Success(created) =>
        log.debug(s"Cube: $created created correctly.")
        created
      case Failure(ex) =>
        throw SpartaJob.logAndCreateEx(
          s"Something gone wrong creating the cube: ${cubeModel.name}. Please re-check the policy.",
          write(cubeModel), ex, policyId, ErrorCodes.Policy.ParsingCube)
    }

  def getTriggers(triggers: Seq[TriggerModel], policyId: String): Seq[Trigger] =
    triggers.map(trigger => createTrigger(trigger, policyId))

  private def createTrigger(trigger: TriggerModel, policyId: String): Trigger =
    Try {
      Trigger(
        trigger.name,
        trigger.sql,
        trigger.writer.outputs,
        trigger.overLast,
        trigger.computeEvery,
        trigger.primaryKey,
        trigger.writer.saveMode,
        trigger.configuration)
    } match {
      case Success(created) =>
        log.debug(s"Trigger: $created created correctly.")
        created
      case Failure(ex) =>
        throw SpartaJob.logAndCreateEx(
          s"Something gone wrong creating the trigger: ${trigger.name}. Please re-check the policy.",
          write(trigger), ex, policyId, ErrorCodes.Policy.ParsingTrigger)
    }

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

  def saveRawData(rawModel: RawDataModel, input: DStream[Row]): Unit =
    if (rawModel.enabled.toBoolean) {
      require(!rawModel.path.equals("default"), "The parquet path must be set")
      RawDataStorageService.save(input, rawModel.path)
    }

  def getCubeWriter(cubeName: String,
                    cubes: Seq[Cube],
                    schemas: Seq[SpartaSchema],
                    triggerSchemas: Seq[SpartaSchema],
                    cubeModels: Seq[CubeModel],
                    outputs: Seq[Output],
                    triggersOuputs: Seq[Output]): CubeWriter = {
    val cubeWriter = cubes.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cube in the cube writer"))
    val schemaWriter = schemas.find(schema => schema.tableName == cubeName)
      .getOrElse(throw new Exception("Is mandatory one schema in the cube writer"))
    val cubeModel = cubeModels.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cubeModel in the cube writer"))
    val writerOp = getCubeWriterOptions(cubeName, outputs, cubeModel)

    CubeWriter(cubeWriter, schemaWriter, writerOp, outputs, triggersOuputs, triggerSchemas)
  }

  def getCubeWriterOptions(cubeName: String, outputsWriter: Seq[Output], cubeModel: CubeModel): CubeWriterOptions = {
    val dateType = SchemaHelper.getTimeTypeFromString(cubeModel.writer.dateType.getOrElse(DefaultTimeStampTypeString))

    CubeWriterOptions(cubeModel.writer.outputs, dateType, cubeModel.writer.saveMode)
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

  def getSparkConfigs(policy: PolicyModel, methodName: String, suffix: String,
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

  def getSparkConfigFromPolicy(policy: PolicyModel) : Map[String, String] =
    policy.sparkConf.flatMap{ sparkProperty =>
      if(sparkProperty.sparkConfKey.isEmpty || sparkProperty.sparkConfValue.isEmpty)
          None
      else Option((sparkProperty.sparkConfKey, sparkProperty.sparkConfValue))}.toMap

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
      case _ =>
        log.error("Error instantiating policy component:", ex)
    }
    saveErrorZK(policyId, code)
    new IllegalArgumentException(message, ex)
  }

  def saveErrorZK(policyId: String, code: Int): Unit = ErrorDAO().dao.create(policyId, code.toString)
}
