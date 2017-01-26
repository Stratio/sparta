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

import com.stratio.sparta.driver.cube.{Cube, CubeMaker}
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.driver.helper.SchemaHelper
import com.stratio.sparta.driver.helper.SchemaHelper._
import com.stratio.sparta.driver.service.RawDataStorageService
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.driver.writer.{CubeWriter, CubeWriterOptions, StreamWriter, StreamWriterOptions}
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, DimensionType}
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.models.policy._
import com.stratio.sparta.serving.core.models.policy.cube.{CubeModel, OperatorModel}
import com.stratio.sparta.serving.core.models.policy.trigger.TriggerModel
import com.stratio.sparta.serving.core.models.{PhaseEnum, PolicyErrorModel}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, PolicyUtils}
import org.apache.spark.SparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.util.{Failure, Success, Try}

class SpartaJob(policy: PolicyModel) extends PolicyUtils with CheckpointUtils {

  private val ReflectionUtils = SpartaJob.ReflectionUtils

  def run(sc: SparkContext): StreamingContext = {
    val checkpointPolicyPath = checkpointPath(policy)
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
    Try(ErrorDAO.getInstance.dao.delete(policy.id.get))
    cubesOutputs.foreach(output => output.setup())

    val input = SpartaJob.getInput(policy, ssc.get, ReflectionUtils)
    val inputDStream = SpartaJob.getDStream(policy, ssc.get, input)

    SpartaJob.saveRawData(policy.rawData, inputDStream)

    val parsedData = SpartaJob.applyParsers(inputDStream, parsers)

    SpartaJob.getTriggers(policy.streamTriggers, policy)
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

  def apply(policy: PolicyModel): SpartaJob = new SpartaJob(policy)


  def getDStream(policy: PolicyModel, ssc: StreamingContext, input: Input): DStream[Row] = {
    val errorMessage = s"Something gone wrong creating the input stream for: ${policy.input.get.name}."
    val okMessage = s"Stream for Input: ${policy.input.get.`type`} created correctly."
    generalTransformation(PhaseEnum.InputStream, policy, okMessage, errorMessage) {
      input.setUp(ssc, policy.storageLevel.get)
    }
  }

  def getInput(policy: PolicyModel, ssc: StreamingContext,
               refUtils: ReflectionUtils): Input = {
    val errorMessage = s"Something gone wrong creating the input: ${policy.input.get.name}. Please re-check the policy."
    val okMessage = s"Input: ${policy.input.get.`type`} created correctly."
    generalTransformation(PhaseEnum.Input, policy, okMessage, errorMessage) {
      createInput(policy, ssc, refUtils)
    }
  }

  private def createInput(policy: PolicyModel, ssc: StreamingContext,
                          refUtils: ReflectionUtils): Input = {
    require(policy.input.isDefined, "You need at least one input in your policy")
    refUtils.tryToInstantiate[Input](policy.input.get.`type` + Input.ClassSuffix, (c) =>
      refUtils.instantiateParameterizable[Input](c, policy.input.get.configuration))
  }

  def getParsers(policy: PolicyModel,
                 refUtils: ReflectionUtils,
                 schemas: Map[String, StructType]): Seq[Parser] =
    policy.transformations.map(parser => createParser(parser, refUtils, policy, schemas))

  private def createParser(model: TransformationsModel,
                           refUtils: ReflectionUtils,
                           policy: PolicyModel,
                           schemas: Map[String, StructType]): Parser = {
    val errorMessage = s"Something gone wrong creating the parser: ${model.`type`}. Please re-check the policy."
    val okMessage = s"Parser: ${model.`type`} created correctly."
    generalTransformation(PhaseEnum.Parser, policy, okMessage, errorMessage) {
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
    }
  }

  def generalTransformation[T](code: PhaseEnum.Value, policy: PolicyModel, okMessage: String, errorMessage: String)
                              (f: => T): T = {
    Try(f) match {
      case Success(result) =>
        log.info(okMessage)
        result
      case Failure(ex) => throw logAndCreateEx(code, ex, policy, errorMessage)
    }
  }

  def logAndCreateEx(
                      code: PhaseEnum.Value,
                      ex: Throwable,
                      policy: PolicyModel,
                      message: String
                    ): IllegalArgumentException = {
    val originalMsg = ex.getCause match {
      case _: ClassNotFoundException => "The component couldn't be found in classpath. Please check the type."
      case _: Exception => ex.getCause.toString
      case _ => "No more detail provided"
    }
    val policyError = PolicyErrorModel(policy.id.get, message, code, originalMsg)
    log.error("An error was detected : {}", policyError)
    Try(ErrorDAO.getInstance.upsert(policyError))
    new IllegalArgumentException(message, ex)
  }

  def applyParsers(input: DStream[Row], parsers: Seq[Parser]): DStream[Row] = {
    if (parsers.isEmpty) input
    else input.mapPartitions(rows => rows.flatMap(row => executeParsers(row, parsers)), preservePartitioning = true)
  }

  def executeParsers(row: Row, parsers: Seq[Parser]): Option[Row] = {
    if (parsers.size == 1) parseEvent(row, parsers.head, removeRaw = true)
    else parseEvent(row, parsers.head).flatMap(eventParsed => executeParsers(eventParsed, parsers.drop(1)))
  }

  //TODO: Check if we need to wrap this also
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

  def getOutputs(policy: PolicyModel,
                 schemas: Seq[SpartaSchema],
                 refUtils: ReflectionUtils): Seq[Output] = policy.outputs.map(o => {
    val schemasAssociated = schemas.filter(tableSchema => tableSchema.outputs.contains(o.name))
    createOutput(policy, o, schemasAssociated, refUtils, policy.version)
  })

  def createOutput(policy: PolicyModel, model: PolicyElementModel, schemasAssociated: Seq[SpartaSchema],
                   refUtils: ReflectionUtils, version: Option[Int]): Output = {
    val errorMessage = s"Something gone wrong creating the output: ${model.`type`}. Please re-check the policy."
    val okMessage = s"Output: ${model.`type`} created correctly."
    generalTransformation(PhaseEnum.Output, policy, okMessage, errorMessage) {
      refUtils.tryToInstantiate[Output](model.`type` + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Option[Int]],
          classOf[Map[String, Serializable]],
          classOf[Seq[SpartaSchema]])
          .newInstance(model.name, version, model.configuration, schemasAssociated)
          .asInstanceOf[Output])
    }
  }

  def getCubes(policy: PolicyModel,
               refUtils: ReflectionUtils,
               initSchema: StructType): Seq[Cube] = {
    policy.cubes.map(cube => createCube(cube, refUtils, policy, initSchema: StructType))
  }

  def getOperators(operatorsModel: Seq[OperatorModel],
                   refUtils: ReflectionUtils,
                   policy: PolicyModel,
                   initSchema: StructType): Seq[Operator] =
    operatorsModel.map(operator => createOperator(operator, refUtils, policy, initSchema))

  def getTriggers(triggers: Seq[TriggerModel], policy: PolicyModel): Seq[Trigger] =
    triggers.map(trigger => createTrigger(trigger, policy))

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
                    triggersOutputs: Seq[Output]): CubeWriter = {
    val cubeWriter = cubes.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cube in the cube writer"))
    val schemaWriter = schemas.find(schema => schema.tableName == cubeName)
      .getOrElse(throw new Exception("Is mandatory one schema in the cube writer"))
    val cubeModel = cubeModels.find(cube => cube.name == cubeName)
      .getOrElse(throw new Exception("Is mandatory one cubeModel in the cube writer"))
    val writerOp = getCubeWriterOptions(cubeName, outputs, cubeModel)

    CubeWriter(cubeWriter, schemaWriter, writerOp, outputs, triggersOutputs, triggerSchemas)
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

  def getSparkConfigFromPolicy(policy: PolicyModel): Map[String, String] =
    policy.sparkConf.flatMap { sparkProperty =>
      if (sparkProperty.sparkConfKey.isEmpty || sparkProperty.sparkConfValue.isEmpty)
        None
      else Option((sparkProperty.sparkConfKey, sparkProperty.sparkConfValue))
    }.toMap

  private def createCube(cubeModel: CubeModel,
                         refUtils: ReflectionUtils,
                         policy: PolicyModel,
                         initSchema: StructType): Cube = {
    val errorMessage = s"Cube: ${cubeModel.name} created correctly."
    val okMessage = s"Something gone wrong creating the cube: ${cubeModel.name}. Please re-check the policy."
    generalTransformation(PhaseEnum.Cube, policy, okMessage, errorMessage) {
      val name = cubeModel.name
      val dimensions = cubeModel.dimensions.map(dimensionDto => {
        val fieldType = initSchema.find(stField => stField.name == dimensionDto.field).map(_.dataType)
        val defaultType = fieldType.flatMap(field => SchemaHelper.mapSparkTypes.get(field))

        new Dimension(dimensionDto.name,
          dimensionDto.field,
          dimensionDto.precision,
          instantiateDimensionType(dimensionDto.`type`, dimensionDto.configuration, refUtils, defaultType))
      })
      val operators = SpartaJob.getOperators(cubeModel.operators, refUtils, policy, initSchema)
      val expiringDataConfig = SchemaHelper.getExpiringData(cubeModel)
      val triggers = getTriggers(cubeModel.triggers, policy)
      Cube(name, dimensions, operators, initSchema, expiringDataConfig, triggers)
    }
  }

  private def createOperator(model: OperatorModel,
                             refUtils: ReflectionUtils,
                             policy: PolicyModel,
                             initSchema: StructType): Operator = {
    val errorMessage = s"Operator: ${model.`type`} created correctly."
    val okMessage = s"Something gone wrong creating the operator: ${model.`type`}. Please re-check the policy."
    generalTransformation(PhaseEnum.Operator, policy, okMessage, errorMessage) {
      refUtils.tryToInstantiate[Operator](model.`type` + Operator.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[StructType],
          classOf[Map[String, Serializable]])
          .newInstance(model.name, initSchema, model.configuration).asInstanceOf[Operator])
    }
  }

  private def createTrigger(trigger: TriggerModel, policy: PolicyModel): Trigger = {
    val errorMessage = s"Trigger: ${trigger.name} created correctly."
    val okMessage = s"Something gone wrong creating the trigger: ${trigger.name}. Please re-check the policy."
    generalTransformation(PhaseEnum.Trigger, policy, okMessage, errorMessage) {
      Trigger(
        trigger.name,
        trigger.sql,
        trigger.writer.outputs,
        trigger.overLast,
        trigger.computeEvery,
        trigger.primaryKey,
        trigger.writer.saveMode,
        trigger.configuration)
    }
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

}
