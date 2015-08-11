
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

package com.stratio.sparkta.driver

import java.io._
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.io.Source
import scala.util._

import akka.event.slf4j.SLF4JLogging
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.reflections.Reflections

import com.stratio.sparkta.aggregator.{Cube, CubeMaker}
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory.{SchemaFactory, SparkContextFactory}
import com.stratio.sparkta.driver.models.{AggregationPoliciesModel, OperatorModel}
import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.driver.util.PolicyUtils
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

object SparktaJob extends SLF4JLogging {

  val baseJars = Seq("driver-plugin.jar", "aggregator-plugin.jar", "sdk-plugin.jar")

  def main(args: Array[String]): Unit = {
    val file = new Path(s"/user/stratio/${args(0)}.json")
    val fileSystem = FileSystem.get(new Configuration())
    val in = fileSystem.open(file)
    val policyString = Source.fromInputStream(in).getLines.mkString
    in.close
    val policy = PolicyUtils.parseJson(policyString)
    val sc = new SparkContext(new SparkConf().setAppName(s"SPARKTA-${policy.name}"))
    SparkContextFactory.setSparkContext(sc)
    runSparktaJob(sc, policy)
    SparkContextFactory.sparkStreamingInstance.get.start
    SparkContextFactory.sparkStreamingInstance.get.awaitTermination
  }

  def runSparktaJob(sc: SparkContext, apConfig: AggregationPoliciesModel): Any = {

    val checkpointPolicyPath = apConfig.checkpointPath.concat(File.separator).concat(apConfig.name)
    deletePreviousCheckpointPath(checkpointPolicyPath)

    val ssc = SparkContextFactory.sparkStreamingInstance(
      new Duration(apConfig.sparkStreamingWindow), checkpointPolicyPath).get
    val input = SparktaJob.input(apConfig, ssc)
    val parsers =
      SparktaJob.parsers(apConfig).sortWith((parser1, parser2) => parser1.getOrder < parser2.getOrder)
    val cubes = SparktaJob.cubes(apConfig)

    val operatorsKeyOperation: Option[Map[String, (WriteOp, TypeOp)]] = {
      val opKeyOp = SchemaFactory.operatorsKeyOperation(cubes.flatMap(cube => cube.operators))
      if (opKeyOp.size > 0) Some(opKeyOp) else None
    }

    val outputsSchemaConfig: Seq[(String, Map[String, String])] = apConfig.outputs.map(o =>
      (o.name, Map(
        Output.Multiplexer -> Try(o.configuration.get(Output.Multiplexer).get.string)
          .getOrElse(Output.DefaultMultiplexer),
        Output.FixedAggregation ->
          Try(o.configuration.get(Output.FixedAggregation).get.string.split(Output.FixedAggregationSeparator).head)
            .getOrElse("")
      )))

    val bcCubeOperatorSchema: Option[Seq[TableSchema]] = {
      val cubeOpSchema = SchemaFactory.cubesOperatorsSchemas(cubes, outputsSchemaConfig)
      if (cubeOpSchema.size > 0) Some(cubeOpSchema) else None
    }

    val outputs = SparktaJob.outputs(apConfig, operatorsKeyOperation, bcCubeOperatorSchema)
    val inputEvent = input._2
    SparktaJob.saveRawData(apConfig, inputEvent)
    val parsed = SparktaJob.applyParsers(inputEvent, parsers)

    val dataCube = new CubeMaker(cubes).setUp(parsed)
    outputs.map(_._2.persist(dataCube))
  }

  def deletePreviousCheckpointPath(checkpointPolicyPath: String): Unit = {
    if (Files.exists(Paths.get(checkpointPolicyPath))) {
      Try(FileUtils.deleteDirectory(new File(checkpointPolicyPath))) match {
        case Success(_) => log.info(s"Found path for this policy. Path deleted: $checkpointPolicyPath")
        case Failure(e) => log.error(s"Found path for this policy. Cannot delete: $checkpointPolicyPath", e)
      }
    }
  }

  @tailrec
  def applyParsers(input: DStream[Event], parsers: Seq[Parser]): DStream[Event] = {
    parsers.headOption match {
      case Some(headParser: Parser) => applyParsers(input.map(event =>
        Try(headParser.parse(event)) match {
          case Success(event) => Some(event)
          case Failure(exception) => {
            val error = s"Failure[Parser]: ${event.toString} | Message: ${exception.getLocalizedMessage}" +
              s" | Parser: ${headParser.getClass.getSimpleName}"
            log.error(error, exception)
            None
          }
        }).flatMap(event => event match {
        case Some(value) => Seq(value)
        case None => Seq()
      }), parsers.drop(1))
      case None => input
    }
  }

  lazy val getClasspathMap: Map[String, String] = {
    val reflections = new Reflections()
    val inputs = reflections.getSubTypesOf(classOf[Input]).toList
    val dimensionTypes = reflections.getSubTypesOf(classOf[DimensionType]).toList
    val operators = reflections.getSubTypesOf(classOf[Operator]).toList
    val outputs = reflections.getSubTypesOf(classOf[Output]).toList
    val parsers = reflections.getSubTypesOf(classOf[Parser]).toList
    val plugins = inputs ++ dimensionTypes ++ operators ++ outputs ++ parsers
    plugins map (t => t.getSimpleName -> t.getCanonicalName) toMap
  }

  def getSparkConfigs(apConfig: AggregationPoliciesModel, methodName: String, suffix: String): Map[String, String] =
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

  def input(apConfig: AggregationPoliciesModel, ssc: StreamingContext): (String, DStream[Event]) =
    (apConfig.input.get.name, tryToInstantiate[Input](apConfig.input.get.`type` + Input.ClassSuffix, (c) =>
      instantiateParameterizable[Input](c, apConfig.input.get.configuration)).setUp(ssc))

  def parsers(apConfig: AggregationPoliciesModel): Seq[Parser] =
    apConfig.transformations.map(parser =>
      tryToInstantiate[Parser](parser.`type` + Parser.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Integer],
          classOf[String],
          classOf[Seq[String]],
          classOf[Map[String, Serializable]])
          .newInstance(parser.name, parser.order, parser.inputField, parser.outputFields, parser.configuration)
          .asInstanceOf[Parser]))

  private def createOperator(operatorModel: OperatorModel): Operator =
    tryToInstantiate[Operator](operatorModel.`type` + Operator.ClassSuffix, (c) =>
      c.getDeclaredConstructor(
        classOf[String],
        classOf[Map[String, Serializable]]
      ).newInstance(operatorModel.name, operatorModel.configuration).asInstanceOf[Operator])

  def getOperators(operatorsModel: Seq[OperatorModel]): Seq[Operator] =
    operatorsModel.map(operator => createOperator(operator))

  def outputs(apConfig: AggregationPoliciesModel,
              bcOperatorsKeyOperation: Option[Map[String, (WriteOp, TypeOp)]],
              bcCubeOperatorSchema: Option[Seq[TableSchema]]): Seq[(String, Output)] =
    apConfig.outputs.map(o => (o.name, tryToInstantiate[Output](o.`type` + Output.ClassSuffix, (c) =>
      c.getDeclaredConstructor(
        classOf[String],
        classOf[Map[String, Serializable]],
        classOf[Option[Map[String, (WriteOp, TypeOp)]]],
        classOf[Option[Seq[TableSchema]]])
        .newInstance(o.name, o.configuration, bcOperatorsKeyOperation, bcCubeOperatorSchema)
        .asInstanceOf[Output])))

  def cubes(apConfig: AggregationPoliciesModel): Seq[Cube] =
    apConfig.cubes.map(cube => {
      val name = cube.name.replaceAll(Output.Separator, "")
      val multiplexer = Try(cube.multiplexer.toBoolean)
        .getOrElse(throw DriverException.create("The multiplexer value must be boolean"))
      val dimensions = cube.dimensions.map(dimensionDto => {
        new Dimension(dimensionDto.name,
          dimensionDto.field,
          dimensionDto.precision,
          instantiateDimensionType(dimensionDto.`type`, dimensionDto.configuration))
      })
      val operators = SparktaJob.getOperators(cube.operators)

      val datePrecision = if (cube.checkpointConfig.timeDimension.isEmpty) None
      else Some(cube.checkpointConfig.timeDimension)
      val timeName = if (datePrecision.isDefined) datePrecision.get else cube.checkpointConfig.granularity

      new Cube(name,
        dimensions,
        operators,
        multiplexer,
        timeName,
        cube.checkpointConfig.interval,
        cube.checkpointConfig.granularity,
        cube.checkpointConfig.timeAvailability)
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

  def instantiateDimensionType(dimensionType: String, configuration: Option[Map[String, String]]): DimensionType =
    tryToInstantiate[DimensionType](dimensionType + Dimension.FieldClassSuffix, (c) => {
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
        new RawDataStorageService(sqlContext, apConfig.rawData.path, apConfig.rawData.partitionFormat)
      rawDataStorage.save(input)
    }

  def jarsFromPolicy(apConfig: AggregationPoliciesModel): Seq[String] = {
    val input = apConfig.input.get.jarFile match {
      case Some(file) => Seq(file)
      case None => Seq()
    }
    val outputs = apConfig.outputs.flatMap(_.jarFile)
    val transformations = apConfig.transformations.flatMap(_.jarFile)
    val operators = apConfig.cubes.flatMap(cube => cube.operators.map(_.jarFile)).flatten
    Seq(baseJars, input, outputs, transformations, operators).flatten
  }

  def activeJars(apConfig: AggregationPoliciesModel, jars: Seq[File]): Either[Seq[String], Seq[String]] = {
    val policyJars = jarsFromPolicy(apConfig)
    val names = jars.map(file => file.getName)
    val missing = for (name <- policyJars if !names.contains(name)) yield name
    if (missing.isEmpty) Right(policyJars)
    else Left(missing)
  }

  def activeJarFiles(policyJars: Seq[String], jars: Seq[File]): Seq[File] =
    jars.filter(file => policyJars.contains(file.getName))
}
