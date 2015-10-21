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

package com.stratio.sparkta.plugin.output.elasticsearch.dao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, ShouldMatchers}

import com.stratio.sparkta.sdk.TypeOp

@RunWith(classOf[JUnitRunner])
class ElasticSearchDAOSpec extends FlatSpec with ShouldMatchers {

  trait BaseValues {

    val dao = new ESDaoMock(Seq(("localhost", 9200)))
  }

  trait ValuesMap extends BaseValues {

    val baseMap = Map("es.nodes" -> "localhost", "es.port" -> "9200", "es.index.auto.create" -> "no")
    val providedMap = Map("es.mapping.id" -> "id")
    val tsMap = Map("es.mapping.names" -> "minutes:@timestamp")
    val expectedProvided = providedMap ++ baseMap
    val expectedWithTs = baseMap ++ tsMap
    val expectedProvidedWithTs = expectedProvided ++ tsMap
  }

  "ElasticSearchDao" should "return a valid configuration" in new ValuesMap {
    dao.getSparkConfig("", false) should be(baseMap)
    dao.getSparkConfig("", true) should be(expectedProvided)
    dao.getSparkConfig("minutes", false) should be(expectedWithTs)
    dao.getSparkConfig("minutes", true) should be(expectedProvidedWithTs)
  }

  it should "return a valid TypeOp from dateType" in new BaseValues {
    dao.getDateTimeType(None) should be(TypeOp.String)
    dao.getDateTimeType(Some("timestamp")) should be(TypeOp.Timestamp)
    dao.getDateTimeType(Some("date")) should be(TypeOp.Date)
    dao.getDateTimeType(Some("datetime")) should be(TypeOp.DateTime)
    dao.getDateTimeType(Some("fake")) should be(TypeOp.String)
  }

  it should "test extra methods for coverage" in new BaseValues {
    dao.idField should be(None)
    dao.defaultIndexMapping should be(None)
    dao.mappingType should be(None)
  }
}

case class ESDaoMock(_nodes: Seq[(String, Int)] = Seq()) extends ElasticSearchDAO {

  override def nodes: Seq[(String, Int)] = _nodes
}