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

import java.io.Serializable

import com.stratio.sparkta.aggregator.{DataCube, Rollup}
import com.stratio.sparkta.driver.configuration._
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/**
 * Created by ajnavarro on 8/10/14.
 */
class StreamingContextService(generalConfiguration: GeneralConfiguration) {

  def createStreamingContext(apConfig: AggregationPoliciesDto): StreamingContext = {
    val ssc = new StreamingContext(
      new SparkContext(configToSparkConf(generalConfiguration, apConfig.name)),
      //TODO one spark context to all streaming contexts is not working
      //SparkContextFactory.sparkContextInstance(generalConfiguration),
      new Duration(apConfig.duration))

    apConfig.jarPaths.foreach(j => ssc.sparkContext.addJar(j))

    val inputs: Map[String, DStream[Event]] = apConfig.inputs.map(i =>
      (i.name, tryToInstantiate[Input](i.elementType, (c) =>
        instantiateParameterizable[Input](c, i.configuration)).setUp(ssc))).toMap

    val outputs = apConfig.outputs.map(o =>
      (o.name, tryToInstantiate[Output](o.elementType, (c) =>
        instantiateParameterizable[Output](c, o.configuration)))).toMap

    val parsers: Seq[Parser] = apConfig.parsers.map(p =>
      tryToInstantiate[Parser](p.elementType, (c) =>
        instantiateParameterizable[Parser](c, p.configuration)))

    val operators: Seq[Operator] = apConfig.operators.map(op =>
      tryToInstantiate[Operator](op.elementType, (c) =>
        instantiateParameterizable[Operator](c, op.configuration)))

    val dimensionsMap = apConfig.dimensions.map(d => (d.name,
      new Dimension(d.name, tryToInstantiate[Bucketer](d.dimensionType, (c) =>
        c.newInstance().asInstanceOf[Bucketer])))).toMap

    val dimensionsSeq = apConfig.dimensions.map(d =>
      new Dimension(d.name, tryToInstantiate[Bucketer](d.dimensionType, (c) =>
        c.newInstance().asInstanceOf[Bucketer])))

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


  private def configToSparkConf(generalConfiguration: GeneralConfiguration, name: String): SparkConf = {
    val conf = new SparkConf()
    conf.setMaster(generalConfiguration.master)
      .setAppName(name)

    conf.set("spark.cores.max", generalConfiguration.cpus.toString)

    // Should be a -Xmx style string eg "512m", "1G"
    conf.set("spark.executor.memory", generalConfiguration.memory)

    Try(generalConfiguration.sparkHome).foreach { home => conf.setSparkHome(generalConfiguration.sparkHome)}

    // Set the Jetty port to 0 to find a random port
    conf.set("spark.ui.port", "0")

    conf.set("spark.streaming.concurrentJobs", "20")

    conf
  }
}
