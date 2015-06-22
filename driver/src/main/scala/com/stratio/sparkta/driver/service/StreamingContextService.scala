/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparkta.driver.service

import java.io.{File, Serializable}
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.util.Try

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.Config
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.reflections.Reflections

import com.stratio.sparkta.aggregator.{MultiCube, Cube}
import com.stratio.sparkta.driver.dto._
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory._
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class StreamingContextService(generalConfig: Config, jars: Seq[File]) extends SLF4JLogging {

  def createStreamingContext(apConfig: AggregationPoliciesDto): StreamingContext = {

    val OutputsSparkConfiguration = "getSparkConfiguration"
    val specifictSparkConfig = SparktaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    val sc = SparkContextFactory.sparkContextInstance(generalConfig, specifictSparkConfig, jars)
    val ssc = SparkContextFactory.sparkStreamingInstance(
      new Duration(apConfig.sparkStreamingWindow),
      apConfig.checkpointing.path).get
    val inputs: Map[String, DStream[Event]] = SparktaJob.inputs(apConfig, ssc)
    val parsers: Seq[Parser] = SparktaJob.parsers(apConfig)
    val operators: Map[String, Operator] = SparktaJob.operators(apConfig)
    val dimensionsMap: Map[String, Dimension] = SparktaJob.instantiateDimensions(apConfig).toMap
    val dimensionsSeq: Seq[Dimension] = SparktaJob.instantiateDimensions(apConfig).map(_._2)
    val cubes: Seq[Cube] = SparktaJob.cubes(apConfig, operators, Seq(), dimensionsMap)

    val bcOperatorsKeyOperation: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]] = {
      val opKeyOp = PolicyFactory.operatorsKeyOperation(operators.values.toSeq)
      if (opKeyOp.size > 0) Some(sc.broadcast(opKeyOp)) else None
    }

    val outputsSchemaConfig: Seq[(String, Map[String, String])] = apConfig.outputs.map(o =>
      (o.name, Map(
        Output.Multiplexer -> Try(o.configuration.get(Output.Multiplexer).get.string).getOrElse("false"),
        Output.FixedAggregation ->
          Try(o.configuration.get(Output.FixedAggregation).get.string.split(Output.FixedAggregationSeparator).head)
            .getOrElse("")
      )))

    val bcCubeOperatorSchema: Option[Broadcast[Seq[TableSchema]]] = {
      val rollOpSchema = PolicyFactory.cubesOperatorsSchemas(cubes, outputsSchemaConfig)
      if (rollOpSchema.size > 0) Some(sc.broadcast(rollOpSchema)) else None
    }
    val datePrecision = if (apConfig.checkpointing.timeDimension.isEmpty) None
    else Some(apConfig.checkpointing.timeDimension)
    val timeName = if (datePrecision.isDefined) datePrecision.get else apConfig.checkpointing.granularity
    val outputs = SparktaJob.outputs(apConfig, sc, bcOperatorsKeyOperation, bcCubeOperatorSchema, timeName)
    val input: DStream[Event] = inputs.head._2
    SparktaJob.saveRawData(apConfig, input)
    val parsed = SparktaJob.applyParsers(input, parsers)

    val dataCube = new MultiCube(dimensionsSeq, cubes, datePrecision, apConfig.checkpointing.granularity).setUp(parsed)
    outputs.map(_._2.persist(dataCube))
    ssc
  }
}

object SparktaJob {

  val OperatorNamePropertyKey = "name"

  @tailrec
  def applyParsers(input: DStream[Event], parsers: Seq[Parser]): DStream[Event] = parsers.headOption match {
    case Some(parser: Parser) => applyParsers(input.map(event => parser.parse(event)), parsers.drop(1))
    case None => input
  }

  val getClasspathMap: Map[String, String] = {
    val reflections = new Reflections()
    val inputs = reflections.getSubTypesOf(classOf[Input]).toList
    val dimensionTypes = reflections.getSubTypesOf(classOf[DimensionType]).toList
    val operators = reflections.getSubTypesOf(classOf[Operator]).toList
    val outputs = reflections.getSubTypesOf(classOf[Output]).toList
    val parsers = reflections.getSubTypesOf(classOf[Parser]).toList
    val plugins = inputs ++ dimensionTypes ++ operators ++ outputs ++ parsers
    plugins map (t => t.getSimpleName -> t.getCanonicalName) toMap
  }

  def getSparkConfigs(apConfig: AggregationPoliciesDto, methodName: String, suffix: String): Map[String, String] =
    apConfig.outputs.flatMap(o => {
      val clazzToInstance = SparktaJob.getClasspathMap.getOrElse(o.`type` + suffix, o.`type` + suffix)
      val clazz = Class.forName(clazzToInstance)
      clazz.getMethods.find(p => p.getName == methodName) match {
        case Some(method) => {
          method.setAccessible(true)
          method.invoke(clazz, o.configuration.asInstanceOf[Map[String, Serializable]])
            .asInstanceOf[Seq[(String, String)]]
        }
        case None => Seq()
      }
    }).toMap

  def inputs(apConfig: AggregationPoliciesDto, ssc: StreamingContext): Map[String, DStream[Event]] =
    apConfig.inputs.map(i =>
      (i.name, tryToInstantiate[Input](i.`type` + Input.ClassSuffix, (c) =>
        instantiateParameterizable[Input](c, i.configuration)).setUp(ssc))).toMap

  def parsers(apConfig: AggregationPoliciesDto): Seq[Parser] = apConfig.parsers.map(p =>
    tryToInstantiate[Parser](p.`type` + Parser.ClassSuffix, (c) =>
      instantiateParameterizable[Parser](c, p.configuration)))

  private def createOperator(operatorDto: OperatorDto): Operator = {
    tryToInstantiate[Operator](operatorDto.`type` + Operator.ClassSuffix,
      (c) => instantiateParameterizable[Operator](c,
        operatorDto.configuration + (OperatorNamePropertyKey -> new JsoneyString(operatorDto.measureName))))
  }

  def operators(apConfig: AggregationPoliciesDto): Map[String, Operator] =
    apConfig.cubes.flatMap(cube => cube.operators.map(operator =>
      (getOperatorKeyName(cube.name, operator), createOperator(operator))))
      .toMap

  def outputs(apConfig: AggregationPoliciesDto,
              sparkContext: SparkContext,
              bcOperatorsKeyOperation: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
              bcCubeOperatorSchema: Option[Broadcast[Seq[TableSchema]]],
              timeName: String): Seq[(String, Output)] =
    apConfig.outputs.map(o =>
      (o.name, tryToInstantiate[Output](o.`type` + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Map[String, Serializable]],
          classOf[SparkContext],
          classOf[Option[Broadcast[Map[String, (WriteOp, TypeOp)]]]],
          classOf[Option[Broadcast[Seq[TableSchema]]]],
          classOf[String])
          .newInstance(o.name, o.configuration, sparkContext, bcOperatorsKeyOperation, bcCubeOperatorSchema, timeName)
          .asInstanceOf[Output])))

  private def getOperatorsWithNames(operators: Seq[Operator], selectedOperators: Seq[String]): Seq[Operator] = {
    operators.filter(op => {
      val propertyTuple = Option(op.properties.filter(tuple => tuple._1.equals(OperatorNamePropertyKey)))

      propertyTuple match {
        case Some(tuple) => selectedOperators.contains(tuple.get(OperatorNamePropertyKey).get.toString)
        case _ => false
      }
    })
  }

  private def getOperatorKeyName(cubeName: String, operatorDto: OperatorDto): String =
    s"$cubeName${operatorDto.measureName}"

  private def getOutputsWithNames(outputs: Seq[Output], selectedOutputs: Seq[String]): Seq[Output] = {
    outputs.filter(out => selectedOutputs.contains(out.getName))
  }

  def cubes(apConfig: AggregationPoliciesDto,
            operators: Map[String, Operator],
            outputs: Seq[Output],
            dimensionsMap: Map[String, Dimension]): Seq[Cube] =
    apConfig.cubes.map(cube => {
      val name = cube.name
      val multiplexer = Try(cube.multiplexer.toBoolean).getOrElse(false)
      val components = cube.dimensions.map(dab => {
        dimensionsMap.get(dab.dimension) match {
          case Some(x: Dimension) => getDimensionPrecision(x, dab)
          case None => throw new DriverException("Dimension name " + dab.dimension + " not found.")
        }
      })

      val operatorsForCube = cube.operators.flatMap(operator => operators.get(getOperatorKeyName(cube.name, operator)))

      new Cube(name,
        components,
        operatorsForCube,
        multiplexer,
        apConfig.checkpointing.interval,
        apConfig.checkpointing.granularity,
        apConfig.checkpointing.timeAvailability)
    })

  def getDimensionPrecision(dimension: Dimension, dimPrecisionDto: PrecisionDto): DimensionPrecision = {
    if (dimension.precisions.contains(dimPrecisionDto.precision)) {
      val precision = dimension.precisions(dimPrecisionDto.precision)
      DimensionPrecision(dimension, new Precision(precision.id,
        precision.typeOp,
        precision.properties ++ dimPrecisionDto.configuration.getOrElse(Map())))
    } else {
      throw new DriverException(
        "Precision type " + dimPrecisionDto.precision + " not supported in dimension " + dimPrecisionDto.dimension)
    }
  }

  def instantiateParameterizable[C](clazz: Class[_], properties: Map[String, Serializable]): C =
    clazz.getDeclaredConstructor(classOf[Map[String, Serializable]]).newInstance(properties).asInstanceOf[C]

  def tryToInstantiate[C](classAndPackage: String, block: Class[_] => C): C = {
    val clazMap: Map[String, String] = SparktaJob.getClasspathMap
    val finalClazzToInstance = clazMap.getOrElse(classAndPackage, classAndPackage)
    try {
      val clazz = Class.forName(finalClazzToInstance)
      block(clazz)
    } catch {
      case cnfe: ClassNotFoundException =>
        throw DriverException.create("Class with name " + classAndPackage + " Cannot be found in the classpath.", cnfe)
      case ie: InstantiationException =>
        throw DriverException.create(
          "Class with name " + classAndPackage + " cannot be instantiated", ie)
      case e: Exception => throw DriverException.create(
        "Generic error trying to instantiate " + classAndPackage, e)
    }
  }

  def dimensionsMap(apConfig: AggregationPoliciesDto): Map[String, Dimension] = instantiateDimensions(apConfig).toMap

  def dimensionsSeq(apConfig: AggregationPoliciesDto): Seq[Dimension] = instantiateDimensions(apConfig).map(_._2)

  def instantiateDimensions(apConfig: AggregationPoliciesDto): Seq[(String, Dimension)] =
    apConfig.fields.map(d => (d.name,
      new Dimension(d.name, tryToInstantiate[DimensionType](d.`type` + Dimension.ClassSuffix, (c) => {
        d.configuration match {
          case Some(conf) => c.getDeclaredConstructor(classOf[Map[String, Serializable]])
            .newInstance(conf).asInstanceOf[DimensionType]
          case None => c.getDeclaredConstructor().newInstance().asInstanceOf[DimensionType]
        }
      }))))

  def saveRawData(apConfig: AggregationPoliciesDto, input: DStream[Event], sqc: Option[SQLContext] = None): Unit =
    if (apConfig.rawData.enabled.toBoolean) {
      require(!apConfig.rawData.path.equals("default"), "The parquet path must be set")
      val sqlContext = sqc.getOrElse(SparkContextFactory.sparkSqlContextInstance.get)
      def rawDataStorage: RawDataStorageService =
        new RawDataStorageService(sqlContext, apConfig.rawData.path, apConfig.rawData.partitionFormat)
      rawDataStorage.save(input)
    }
}
