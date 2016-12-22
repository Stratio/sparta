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
package com.stratio.sparta.plugin.output.cassandra

import java.io.{Serializable => JSerializable}

import com.datastax.spark.connector.cql.CassandraConnector
import com.stratio.sparta.sdk._
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.sdk.properties.JsoneyString
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class CassandraOutputTest extends FlatSpec with Matchers with MockitoSugar with AnswerSugar {

  val s = "sum"
  val properties = Map(("connectionHost", "127.0.0.1"), ("connectionPort", "9042"))

  "getSparkConfiguration" should "return a Seq with the configuration" in {
    val configuration = Map(("connectionHost", "127.0.0.1"), ("connectionPort", "9042"))
    val cass = CassandraOutput.getSparkConfiguration(configuration)

    cass should be(List(("spark.cassandra.connection.host", "127.0.0.1"), ("spark.cassandra.connection.port", "9042")))
  }

  "setup" should "return X" in {

    val tableSchema = Seq(SpartaSchema(Seq("outputName"), "dim1", StructType(Array(
      StructField("dim1", StringType, false))), Option("minute")))

    val cassandraConnector: CassandraConnector = mock[CassandraConnector]

    val out = new CassandraOutput("key", Option(1), properties, tableSchema) {
      override val textIndexFields = Option(Array("test"))

      override def getCassandraConnector(): CassandraConnector = {
        cassandraConnector
      }
    }
    out.setup()
  }

  "getSparkConfiguration" should "return all cassandra-spark config" in {
    val config: Map[String, JSerializable] = Map(
      ("sparkProperties" -> JsoneyString(
        "[{\"sparkPropertyKey\":\"spark.cassandra.input.fetch.size_in_rows\",\"sparkPropertyValue\":\"2000\"}," +
          "{\"sparkPropertyKey\":\"spark.cassandra.input.split.size_in_mb\",\"sparkPropertyValue\":\"64\"}]")),
      ("anotherProperty" -> "true")
    )

    val sparkConfig = CassandraOutput.getSparkConfiguration(config)

    sparkConfig.exists(_ == ("spark.cassandra.input.fetch.size_in_rows" -> "2000")) should be(true)
    sparkConfig.exists(_ == ("spark.cassandra.input.split.size_in_mb" -> "64")) should be(true)
    sparkConfig.exists(_ == ("anotherProperty" -> "true")) should be(false)
  }

  "getSparkConfiguration" should "not return cassandra-spark config" in {
    val config: Map[String, JSerializable] = Map(
      ("hadoopProperties" -> JsoneyString(
        "[{\"sparkPropertyKey\":\"spark.cassandra.input.fetch.size_in_rows\",\"sparkPropertyValue\":\"2000\"}," +
          "{\"sparkPropertyKey\":\"spark.cassandra.input.split.size_in_mb\",\"sparkPropertyValue\":\"64\"}]")),
      ("anotherProperty" -> "true")
    )

    val sparkConfig = CassandraOutput.getSparkConfiguration(config)

    sparkConfig.exists(_ == ("spark.cassandra.input.fetch.size_in_rows" -> "2000")) should be(false)
    sparkConfig.exists(_ == ("spark.cassandra.input.split.size_in_mb" -> "64")) should be(false)
    sparkConfig.exists(_ == ("anotherProperty" -> "true")) should be(false)
  }
}
