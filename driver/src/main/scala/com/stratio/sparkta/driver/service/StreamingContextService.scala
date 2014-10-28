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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.aggregator.{DataCube, Rollup}
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import com.typesafe.config.Config
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}


class StreamingContextService(generalConfig: Config, jars: Seq[File]) extends SLF4JLogging{

  def createStreamingContext(apConfig: AggregationPoliciesDto): StreamingContext = {
    val ssc = new StreamingContext(
      /*
      Spark doesn't support multiple active SparkContexts in the same JVM,
      although this isn't well-documented and there's no error-checking for this
      (PySpark has checks for this, though). This isn't to say that we
      can't / won't eventually support multiple contexts per JVM (see SPARK-2243),
      but that could be somewhat difficult in the very short term because there
      may be several baked-in assumptions that we'll have to address
      (the (effectively) global SparkEnv, for example).
       */
      //new SparkContext(configToSparkConf(generalConfiguration, apConfig.name)),
      SparkContextFactory.sparkContextInstance(generalConfig, jars),
      new Duration(apConfig.duration))

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

    val outputSchema = operators.map(op => op.key -> op.writeOperation).toMap

    val outputs = apConfig.outputs.map(o =>
      (o.name, tryToInstantiate[Output](o.elementType, (c) =>
        c.getDeclaredConstructor(classOf[Map[String, Serializable]], classOf[Map[String,WriteOp]]).newInstance(o.configuration, outputSchema).asInstanceOf[Output]
      )))

    val rollups: Seq[Rollup] = apConfig.rollups.map(r => {
      val components = r.dimensionAndBucketTypes.map(dab => {
        dimensionsMap.get(dab.dimensionName) match {
          case Some(x: Dimension) => x.bucketTypes.contains(new BucketType(dab.bucketType)) match {
            case true => (x, new BucketType(dab.bucketType))
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

    var parsed = input
    for (parser <- parsers) {
      parsed = parser.map(parsed)
    }

    output.persist(new DataCube(dimensionsSeq, rollups).setUp(parsed))

    ssc
  }

  private def instantiateDimensions(apConfig: AggregationPoliciesDto) = {
    apConfig.dimensions.map(d => (d.name,
      new Dimension(d.name, tryToInstantiate[Bucketer](d.dimensionType, (c) => {
        d.configuration match {
          case Some(conf) => c.getDeclaredConstructor(classOf[Map[String, Serializable]])
            .newInstance(conf).asInstanceOf[Bucketer]
          case None => c.newInstance().asInstanceOf[Bucketer]
        }
      }))))
  }

  private def tryToInstantiate[C](classAndPackage: String, block: Class[_] => C): C = {
    try {
      val clazz = Class.forName(classAndPackage)
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

  private def instantiateParameterizable[C](clazz: Class[_], properties: Map[String, Serializable]): C = {
    clazz.getDeclaredConstructor(classOf[Map[String, Serializable]]).newInstance(properties).asInstanceOf[C]
  }

}
