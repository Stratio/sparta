
/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.service

import java.io.{File, Serializable}
import com.stratio.sparkta.sdk.TypeOp.TypeOp
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext

import scala.annotation.tailrec
import scala.collection.JavaConversions._

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.Config
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.reflections.Reflections

import com.stratio.sparkta.aggregator.{DataCube, Rollup}
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class StreamingContextService(generalConfig: Config, jars: Seq[File]) extends SLF4JLogging {

  // scalastyle:ignore method.length
  def createStreamingContext(apConfig: AggregationPoliciesDto): StreamingContext = {

    val sc = SparkContextFactory.sparkContextInstance(generalConfig, jars)
    val sqlContext = SparkContextFactory.sparkSqlContextInstance.get
    val ssc = SparkContextFactory.sparkStreamingInstance(new Duration(apConfig.duration)).get

    val inputs: Map[String, DStream[Event]] = apConfig.inputs.map(i =>
      (i.name, tryToInstantiate[Input](i.elementType, (c) =>
        instantiateParameterizable[Input](c, i.configuration)).setUp(ssc))).toMap

    val parsers: Seq[Parser] = apConfig.parsers.map(p =>
      tryToInstantiate[Parser](p.elementType, (c) =>
        instantiateParameterizable[Parser](c, p.configuration)))

    val operators: Seq[Operator] = apConfig.operators.map(op =>
      tryToInstantiate[Operator](op.elementType, (c) =>
        instantiateParameterizable[Operator](c, op.configuration)))

    //TODO workaround this instantiateDimensions(apConfig).toMap.map(_._2) is not serializable.
    val dimensionsMap: Map[String, Dimension] = instantiateDimensions(apConfig).toMap
    val dimensionsSeq: Seq[Dimension] = instantiateDimensions(apConfig).map(_._2)

    val bcOperatorsKeyOperation: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]] ={
      val opKeyOp = PolicyFactory.operatorsKeyOperation(operators)
      if(opKeyOp.size > 0) Some(sc.broadcast(opKeyOp)) else None
    }

    val outputs: Seq[(String, Output)] = apConfig.outputs.map(o =>
      (o.name, tryToInstantiate[Output](o.elementType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Map[String, Serializable]],
          classOf[SQLContext],
          classOf[Option[Broadcast[Map[String, (WriteOp, TypeOp)]]]])
          .newInstance(o.name, o.configuration, sqlContext, bcOperatorsKeyOperation).asInstanceOf[Output]
      )))

    val rollups: Seq[Rollup] = apConfig.rollups.map(r => {
      val components = r.dimensionAndBucketTypes.map(dab => {
        dimensionsMap.get(dab.dimensionName) match {
          case Some(x: Dimension) => x.bucketTypes.contains(new BucketType(dab.bucketType)) match {
            case true => (x, new BucketType(dab.bucketType, dab.configuration.getOrElse(Map())))
            case _ =>
              throw new DriverException(
                "Bucket type " + dab.bucketType + " not supported in dimension " + dab.dimensionName)
          }
          case None => throw new DriverException("Dimension name " + dab.dimensionName + " not found.")
        }
      })
      new Rollup(components, operators)
    })

    //TODO only support one input
    val input: DStream[Event] = inputs.head._2
    //TODO only support one output
    val output = outputs.head._2
    val parsed = StreamingContextService.applyParsers(input, parsers)

    implicit val bcRollupOperatorSchema = {
      val rollOpSchema = PolicyFactory.rollupsOperatorsSchemas(rollups, outputs, operators)
      if(rollOpSchema.size > 0) Some(sc.broadcast(rollOpSchema)) else None
    }

    output.persist(new DataCube(dimensionsSeq, rollups).setUp(parsed))
    ssc
  }

  private def instantiateDimensions(apConfig: AggregationPoliciesDto) = {
    apConfig.dimensions.map(d => (d.name,
      new Dimension(d.name, tryToInstantiate[Bucketer](d.dimensionType, (c) => {
        //TODO fix behaviour when configuration is empty
        d.configuration match {
          case Some(conf) => c.getDeclaredConstructor(classOf[Map[String, Serializable]])
            .newInstance(conf).asInstanceOf[Bucketer]
          case None => c.getDeclaredConstructor().newInstance().asInstanceOf[Bucketer]
        }
      }))))
  }

  private def tryToInstantiate[C](classAndPackage: String, block: Class[_] => C): C = {

    val clazMap: Map[String, String] = StreamingContextService.getClasspathMap

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

  private def instantiateParameterizable[C](clazz: Class[_], properties: Map[String, Serializable]): C =
    clazz.getDeclaredConstructor(classOf[Map[String, Serializable]]).newInstance(properties).asInstanceOf[C]
}

object StreamingContextService {

  @tailrec
  def applyParsers(input: DStream[Event], parsers: Seq[Parser]): DStream[Event] = {
    if (parsers.size > 0) applyParsers(input.map(event => parsers.head.parse(event)), parsers.drop(1))
    else input
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

}
