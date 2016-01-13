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
import scala.util._

import akka.event.slf4j.SLF4JLogging
import org.apache.commons.io.FileUtils
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}

import com.stratio.sparkta.aggregator._
import com.stratio.sparkta.driver.factory.{SchemaFactory, SparkContextFactory}
import com.stratio.sparkta.driver.service.RawDataStorageService
import com.stratio.sparkta.driver.util.ReflectionUtils
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, OperatorModel}

object SparktaJob extends SLF4JLogging {

 //scalastyle:off
  def runSparktaJob(sc: SparkContext, apConfig: AggregationPoliciesModel): Any = {
    val checkpointPolicyPath = apConfig.checkpointPath.concat(File.separator).concat(apConfig.name)
    //FIXME: check the problem in the checkpoint and fault tolerance
    // deletePreviousCheckpointPath(checkpointPolicyPath)
    val reflectionUtils = new ReflectionUtils
    val ssc = SparkContextFactory.sparkStreamingInstance(
      new Duration(apConfig.sparkStreamingWindow), checkpointPolicyPath)
    val inputDStream = SparktaJob.input(apConfig, ssc.get, reflectionUtils)
    val parsers =
      SparktaJob.parsers(apConfig, reflectionUtils).sortWith((parser1, parser2) => parser1.getOrder < parser2.getOrder)
    val cubes = SparktaJob.cubes(apConfig, reflectionUtils)
    val operatorsKeyOperation: Option[Map[String, (WriteOp, TypeOp)]] = {
      val opKeyOp = SchemaFactory.operatorsKeyOperation(cubes.flatMap(cube => cube.operators))
      if (opKeyOp.nonEmpty) Some(opKeyOp) else None
    }

    val outputsSchemaConfig: Seq[(String, Map[String, String])] = apConfig.outputs.map(o =>
      (o.name, Map(
        Output.FixedMeasure ->
          Try(o.configuration.get(Output.FixedMeasure).get.string.split(Output.FixedMeasureSeparator).head)
            .getOrElse("")
      )))

    val bcCubeOperatorSchema: Option[Seq[TableSchema]] = {
      val cubeOpSchema = SchemaFactory.cubesOperatorsSchemas(cubes, outputsSchemaConfig)
      if (cubeOpSchema.nonEmpty) Option(cubeOpSchema) else None
    }

    SparktaJob.saveRawData(apConfig, inputDStream)
    val parsed = applyParsers(inputDStream, parsers)

    val cubesWithTime: Seq[DStream[(DimensionValuesTime, MeasuresValues)]] =
      cubes
        .flatMap(cube => cube match {
          case cubeWithTime: CubeWithTime =>  new CubeMakerWithTime(cubeWithTime).setUp(parsed)
          case _ =>  None
    })

    val cubesWithoutTime: Seq[DStream[(DimensionValuesWithoutTime, MeasuresValues)]] =
      cubes
        .flatMap(cube => cube match {
          case cubeWithoutTime: CubeWithoutTime => new CubeMakerWithoutTime(cubeWithoutTime).setUp(parsed)
          case _ =>  None
    })

   if(!cubesWithTime.isEmpty) {
     val outputsWithTime =
       SparktaJob.outputs[DimensionValuesTime](apConfig, operatorsKeyOperation, bcCubeOperatorSchema, reflectionUtils)

     outputsWithTime.foreach(outputWithTime => {
       outputWithTime.persist(cubesWithTime)
     })
   }
   if(!cubesWithoutTime.isEmpty){
     val outputsWithoutTime =
       SparktaJob
         .outputs[DimensionValuesWithoutTime](apConfig, operatorsKeyOperation, bcCubeOperatorSchema, reflectionUtils)

     outputsWithoutTime.foreach(outputWithoutTime => {
       outputWithoutTime.persist(cubesWithoutTime)
     })
   }
  }
  //scalastyle:on

  @tailrec
  def applyParsers(input: DStream[Event], parsers: Seq[Parser]): DStream[Event] = {
    parsers.headOption match {
      case Some(headParser: Parser) =>
        applyParsers(input.map(event => parseEvent(headParser, event))
          .flatMap(eventToSeq), parsers.drop(1))
      case None => input
    }
  }

  def eventToSeq(e: Option[Event]): Seq[Event] = {
    e match {
      case Some(value) => Seq(value)
      case None => Seq()
    }
  }

  def parseEvent(p: Parser, e: Event): Option[Event] = {

    Try(p.parse(e)) match {
      case Success(okEvent) => Some(okEvent)
      case Failure(exception) => {
        val error = s"Failure[Parser]: ${e.toString} | Message: ${exception.getLocalizedMessage}" +
          s" | Parser: ${p.getClass.getSimpleName}"
        log.error(error, exception)
        None
      }
    }
  }

  def getSparkConfigs(apConfig: AggregationPoliciesModel, methodName: String, suffix: String,
                      refUtils: ReflectionUtils): Map[String, String] = {
    log.info("Initializing reflection")
    apConfig.outputs.flatMap(o => {
      val clazzToInstance = refUtils.getClasspathMap.getOrElse(o.`type` + suffix, o.`type` + suffix)
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
  }

  def input(apConfig: AggregationPoliciesModel, ssc: StreamingContext, refUtils: ReflectionUtils): DStream[Event] = {
    val inputInstance = refUtils.tryToInstantiate[Input](apConfig.input.get.`type` + Input.ClassSuffix, (c) =>
      refUtils.instantiateParameterizable[Input](c, apConfig.input.get.configuration))
    inputInstance.setUp(ssc, apConfig.storageLevel.get)
  }

  def parsers(apConfig: AggregationPoliciesModel, refUtils: ReflectionUtils): Seq[Parser] =
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

  private def createOperator(operatorModel: OperatorModel, refUtils: ReflectionUtils): Operator =
    refUtils.tryToInstantiate[Operator](operatorModel.`type` + Operator.ClassSuffix, (c) =>
      c.getDeclaredConstructor(
        classOf[String],
        classOf[Map[String, Serializable]]
      ).newInstance(operatorModel.name, operatorModel.configuration).asInstanceOf[Operator])

  def getOperators(operatorsModel: Seq[OperatorModel], refUtils: ReflectionUtils): Seq[Operator] =
    operatorsModel.map(operator => createOperator(operator, refUtils))

  def outputs[T](apConfig: AggregationPoliciesModel,
              bcOperatorsKeyOperation: Option[Map[String, (WriteOp, TypeOp)]],
              bcCubeOperatorSchema: Option[Seq[TableSchema]], refUtils: ReflectionUtils): Seq[Output[T]] =
    apConfig.outputs.map(o => refUtils.tryToInstantiate[Output[T]](o.`type` + Output.ClassSuffix, (c) =>
      c.getDeclaredConstructor(
        classOf[String],
        classOf[Option[Int]],
        classOf[Map[String, Serializable]],
        classOf[Option[Map[String, (WriteOp, TypeOp)]]],
        classOf[Option[Seq[TableSchema]]])
        .newInstance(o.name, apConfig.version, o.configuration, bcOperatorsKeyOperation, bcCubeOperatorSchema)
        .asInstanceOf[Output[T]]))

  def cubes(apConfig: AggregationPoliciesModel, refUtils: ReflectionUtils): Seq[Cube] =
    apConfig.cubes.map(cube => {
      val name = cube.name.replaceAll(Output.Separator, "")
      val dimensions = cube.dimensions.map(dimensionDto => {
        new Dimension(dimensionDto.name,
          dimensionDto.field,
          dimensionDto.precision,
          instantiateDimensionType(dimensionDto.`type`, dimensionDto.configuration, refUtils))
      })
      val operators = SparktaJob.getOperators(cube.operators, refUtils)

      if (cube.checkpointConfig.timeDimension.equalsIgnoreCase("none")){
        CubeWithoutTime(name,
          dimensions,
          operators,
          cube.checkpointConfig.interval)

      }else {

        val datePrecision = if (cube.checkpointConfig.timeDimension.isEmpty) None
        else Some(cube.checkpointConfig.timeDimension)
        val timeName = if (datePrecision.isDefined) datePrecision.get else cube.checkpointConfig.granularity

        CubeWithTime(name,
          dimensions,
          operators,
          timeName,
          cube.checkpointConfig.interval,
          cube.checkpointConfig.granularity,
          cube.checkpointConfig.timeAvailability)
      }

    })

  def instantiateDimensionType(dimensionType: String, configuration: Option[Map[String, String]],
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

  def deletePreviousCheckpointPath(checkpointPolicyPath: String): Unit = {
    if (Files.exists(Paths.get(checkpointPolicyPath))) {
      Try(FileUtils.deleteDirectory(new File(checkpointPolicyPath))) match {
        case Success(_) => log.info(s"Found path for this policy. Path deleted: $checkpointPolicyPath")
        case Failure(e) => log.error(s"Found path for this policy. Cannot delete: $checkpointPolicyPath", e)
      }
    }
  }
}
