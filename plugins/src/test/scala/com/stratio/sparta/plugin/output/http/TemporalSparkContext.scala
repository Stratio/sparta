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
package com.stratio.sparta.plugin.output.http

import org.scalatest.{BeforeAndAfterAll, FlatSpec, FunSuite, Suite}
import org.apache.spark._


private[http] trait TemporalSparkContext extends FlatSpec with BeforeAndAfterAll{

  val conf = new SparkConf()
    .setAppName("Rest-simulator-test")
    .setIfMissing("spark.master", "local[*]")

  @transient private var _sc: SparkContext = _

  def sc: SparkContext = _sc

  override def beforeAll()  {
    _sc = new SparkContext(conf)
  }

  override def afterAll() : Unit = {
    if (sc != null) {
      sc.stop()
      _sc = null
    }

    System.gc()
  }


}
