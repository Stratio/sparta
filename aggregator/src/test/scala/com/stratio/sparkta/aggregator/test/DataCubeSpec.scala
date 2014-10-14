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
package com.stratio.sparkta.aggregator.test

import com.stratio.sparkta.aggregator.bucket.{Bucketer, StringBucketer}
import com.stratio.sparkta.aggregator.domain.InputEvent
import com.stratio.sparkta.aggregator.parser.KeyValueParser
import com.stratio.sparkta.aggregator.{DataCube, Dimension, Rollup}
import org.apache.spark.rdd.OneFileDStream
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by ajnavarro on 13/10/14.
 */
class DataCubeSpec extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll with MockitoSugar {

  var ssc: StreamingContext = null

  before {
    ssc = new StreamingContext("local[2]", "test-app", new Duration(1000))
  }

  after {
    ssc = null
  }

  "A DataCube" should {
    "test" in {
      val dimension1 = new Dimension("col1", new StringBucketer())
      val dimension2 = new Dimension("col2", new StringBucketer())
      val rollup1 = new Rollup(Seq((dimension1, Bucketer.identity)))
      val rollup2 = new Rollup(Seq((dimension1, Bucketer.identity), (dimension2, Bucketer.identity)))

      val dataCube = new DataCube(Seq[Dimension](dimension1, dimension2), Seq(rollup1, rollup2))

      val dstream = new KeyValueParser()
        .map(new OneFileDStream(ssc, "/testFile.data", 5).map(data => new InputEvent(null, data.getBytes)))
      dataCube.setUp(dstream).map(d => d.foreachRDD(f => f.collect().foreach(umo => println(umo.keyString))))

      ssc.start()

      ssc.awaitTermination(5000)
    }
  }
}
