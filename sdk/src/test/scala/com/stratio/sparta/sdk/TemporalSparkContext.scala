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
package com.stratio.sparta.sdk

import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import org.apache.spark._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.{Minute, Span}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec}

import scala.concurrent.duration._

private[sparta] trait TemporalSparkContext extends FlatSpec with BeforeAndAfterAll with BeforeAndAfter
  with SLF4JLogging with TimeLimitedTests {

  implicit val timeout = Timeout(10 seconds)
  val timeLimit = Span(1, Minute)

  val conf = new SparkConf()
    .setAppName("simulator-test")
    .setIfMissing("spark.master", "local[*]")

  @transient private var _sc: SparkContext = _
  @transient private var _ssc: StreamingContext = _
  @transient private var _sparkSession: XDSession = _

  def sc: SparkContext = _sc
  def ssc: StreamingContext = _ssc
  def sparkSession: XDSession = _sparkSession

  override def beforeAll()  {
    _sc = new SparkContext(conf)
    _ssc = new StreamingContext(sc, Seconds(2))
    _sparkSession =  XDSession.builder().config(_sc.getConf).create("dummyUser")
  }

  override def afterAll() : Unit = {
    if(ssc != null){
      ssc.stop(stopSparkContext =  false, stopGracefully = false)
      _ssc = null
    }
    if (sc != null){
      sc.stop()
      _sc = null
    }
    if (sparkSession != null){
      sparkSession.stop()
      _sparkSession = null
    }

    System.gc()
  }


}
