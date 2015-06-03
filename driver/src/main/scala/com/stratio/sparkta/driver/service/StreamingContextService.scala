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

import com.stratio.sparkta.aggregator.{DataCube, Rollup}
import com.stratio.sparkta.driver.dto.{AggregationPoliciesDto, PolicyElementDto}
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory._
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class StreamingContextService(generalConfig: Config, jars: Seq[File]) extends SLF4JLogging {

  def createStreamingContext(apConfig: AggregationPoliciesDto): StreamingContext = {

    val OutputsSparkConfiguration = "getSparkConfiguration"
    val specifictSparkConfig = SparktaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration)
    val sc = SparkContextFactory.sparkContextInstance(generalConfig, specifictSparkConfig, jars)
    val sqlContext = SparkContextFactory.sparkSqlContextInstance.get
    val ssc = SparkContextFactory.sparkStreamingInstance(new Duration(apConfig.duration), apConfig.checkpointDir).get
    val inputs: Map[String, DStream[Event]] = SparktaJob.inputs(apConfig, ssc)
    val parsers: Seq[Parser] = SparktaJob.parsers(apConfig)
    val operators: Seq[Operator] = SparktaJob.operators(apConfig)
    val dimensionsMap: Map[String, Dimension] = SparktaJob.instantiateDimensions(apConfig).toMap
    val dimensionsSeq: Seq[Dimension] = SparktaJob.instantiateDimensions(apConfig).map(_._2)
    val rollups: Seq[Rollup] = SparktaJob.rollups(apConfig, operators, dimensionsMap)

    val bcOperatorsKeyOperation: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]] = {
      val opKeyOp = PolicyFactory.operatorsKeyOperation(operators)
      if (opKeyOp.size > 0) Some(sc.broadcast(opKeyOp)) else None
    }

    val outputsSchemaConfig: Seq[(String, Map[String, String])] = apConfig.outputs.map(o =>
      (o.name, Map(
        Output.Multiplexer -> Try(o.configuration.get(Output.Multiplexer).get.string).getOrElse("false"),
        Output.FixedAggregation ->
          Try(o.configuration.get(Output.FixedAggregation).get.string.split(Output.FixedAggregationSeparator).head)
            .getOrElse("")
      )))

    val bcRollupOperatorSchema: Option[Broadcast[Seq[TableSchema]]] = {
      val rollOpSchema = PolicyFactory.rollupsOperatorsSchemas(rollups, outputsSchemaConfig)
      if (rollOpSchema.size > 0) Some(sc.broadcast(rollOpSchema)) else None
    }
    val dateBucket = if (apConfig.timeBucket.isEmpty) None else Some(apConfig.timeBucket)
    val timeName = if (dateBucket.isDefined) dateBucket.get else apConfig.checkpointGranularity
    val outputs = SparktaJob.outputs(apConfig, sc, bcOperatorsKeyOperation, bcRollupOperatorSchema, timeName)
    val input: DStream[Event] = inputs.head._2
    SparktaJob.saveRawData(apConfig, sqlContext, input)
    val parsed = SparktaJob.applyParsers(input, parsers)

    val dataCube = new DataCube(dimensionsSeq, rollups, dateBucket, apConfig.checkpointGranularity).setUp(parsed)
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
    val bucketers = reflections.getSubTypesOf(classOf[Bucketer]).toList
    val operators = reflections.getSubTypesOf(classOf[Operator]).toList
    val outputs = reflections.getSubTypesOf(classOf[Output]).toList
    val parsers = reflections.getSubTypesOf(classOf[Parser]).toList
    val plugins = inputs ++ bucketers ++ operators ++ outputs ++ parsers
    plugins map (t => t.getSimpleName -> t.getCanonicalName) toMap
  }

  def getSparkConfigs(apConfig: AggregationPoliciesDto, methodName: String): Map[String, String] =
    apConfig.outputs.flatMap(o => {
      val clazzToInstance = SparktaJob.getClasspathMap.getOrElse(o.elementType, o.elementType)
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
      (i.name, tryToInstantiate[Input](i.elementType, (c) =>
        instantiateParameterizable[Input](c, i.configuration)).setUp(ssc))).toMap

  def parsers(apConfig: AggregationPoliciesDto): Seq[Parser] = apConfig.parsers.map(p =>
    tryToInstantiate[Parser](p.elementType, (c) =>
      instantiateParameterizable[Parser](c, p.configuration)))

  private def createOperator(op2: PolicyElementDto): Operator = {
    tryToInstantiate[Operator](op2.elementType,
      (c) => instantiateParameterizable[Operator](c,
        op2.configuration + (OperatorNamePropertyKey -> new JsoneyString(op2.name))))
  }

  def operators(apConfig: AggregationPoliciesDto): Seq[Operator] =
    apConfig.operators
      .map(op2 => createOperator(op2))

  def outputs(apConfig: AggregationPoliciesDto,
              sparkContext: SparkContext,
              bcOperatorsKeyOperation: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
              bcRollupOperatorSchema: Option[Broadcast[Seq[TableSchema]]],
              timeName: String): Seq[(String, Output)] =
    apConfig.outputs.map(o =>
      (o.name, tryToInstantiate[Output](o.elementType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Map[String, Serializable]],
          classOf[SparkContext],
          classOf[Option[Broadcast[Map[String, (WriteOp, TypeOp)]]]],
          classOf[Option[Broadcast[Seq[TableSchema]]]],
          classOf[String])
          .newInstance(o.name, o.configuration, sparkContext, bcOperatorsKeyOperation, bcRollupOperatorSchema, timeName)
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

  def rollups(apConfig: AggregationPoliciesDto,
              operators: Seq[Operator],
              dimensionsMap: Map[String, Dimension]): Seq[Rollup] =
    apConfig.rollups.map(r => {
      val components = r.dimensionAndBucketTypes.map(dab => {
        dimensionsMap.get(dab.dimensionName) match {
          case Some(x: Dimension) => x.bucketTypes.contains(new BucketType(dab.bucketType)) match {
            case true => DimensionBucket(x, new BucketType(dab.bucketType, dab.configuration.getOrElse(Map())))
            case _ =>
              throw new DriverException(
                "Bucket type " + dab.bucketType + " not supported in dimension " + dab.dimensionName)
          }
          case None => throw new DriverException("Dimension name " + dab.dimensionName + " not found.")
        }
      })

      val operatorsForRollup = Option(r.operators) match {
        case Some(selectedOperators) => getOperatorsWithNames(operators, selectedOperators)
        case _ => Seq()
      }

      new Rollup(components,
        operatorsForRollup,
        apConfig.checkpointInterval,
        apConfig.checkpointGranularity,
        apConfig.checkpointTimeAvailability)
    })

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
    apConfig.dimensions.map(d => (d.name,
      new Dimension(d.name, tryToInstantiate[Bucketer](d.dimensionType, (c) => {
        //TODO fix behaviour when configuration is empty
        d.configuration match {
          case Some(conf) => c.getDeclaredConstructor(classOf[Map[String, Serializable]])
            .newInstance(conf).asInstanceOf[Bucketer]
          case None => c.getDeclaredConstructor().newInstance().asInstanceOf[Bucketer]
        }
      }))))

  def saveRawData(apConfig: AggregationPoliciesDto, sqlContext: SQLContext, input: DStream[Event]): Unit =
    if (apConfig.saveRawData.toBoolean) {
      def rawDataStorage: RawDataStorageService =
        new RawDataStorageService(sqlContext, apConfig.rawDataParquetPath, apConfig.rawDataGranularity)
      rawDataStorage.save(input)
    }
}
