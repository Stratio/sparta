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
package com.stratio.sparkta.aggregator

import java.io.{Serializable => JSerializable}
import scala.collection.mutable

import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{TestSuiteBase, _}

import com.stratio.sparkta.plugin.bucketer.passthrough.PassthroughBucketer
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.sdk._

//noinspection ScalaStyle
class DataCubeSpec extends TestSuiteBase {

  val myConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName + "" + System.currentTimeMillis())
    .setMaster("local[2]") .set("spark.streaming.clock", "org.apache.spark.streaming.util.ManualClock")
  val sc = new SparkContext(myConf)
  val ssc: StreamingContext = new StreamingContext(sc, Seconds(2))
  val clock = new ClockWrapper(ssc)
  def myDim(i :Int) =  new Dimension("myKey" + i, new PassthroughBucketer().asInstanceOf[Bucketer])
  def getDimensions: Seq[Dimension] = (0 until 10) map (i => myDim(i))

  def getComponents: Seq[(Dimension, BucketType)] = (0 until 1) map (i =>( myDim(i), Bucketer.identity))

  def getOperators: Seq[Operator] = (0 until 1) map (i => new CountOperator(Map("prop" -> "propValue"
    .asInstanceOf[JSerializable])))

  val myRollups: Seq[Rollup] = (0 until 2) map (i => new Rollup(components = getComponents, operators = getOperators))

  val events = mutable.Queue[RDD[Event]]()
  val myDimensions: Seq[Dimension] = getDimensions

  val getEvents: Seq[Event] = (0 until 10) map (i => new Event(Map("key" + i -> System.currentTimeMillis()
    .asInstanceOf[JSerializable])))

  test("DataCube setUp") {
    val dc: DataCube = new DataCube(myDimensions, myRollups)
    val result =dc.setUp(ssc.queueStream(events))
    result.foreach(i => i.print)
    assert(result.size ==2)
    ssc.start()
    events += sc.makeRDD(getEvents)
    clock.advance(2)


  }
}