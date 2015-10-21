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

package com.stratio.sparkta.plugin.output.elasticsearch

import java.io.{Serializable => JSerializable}
import scala.annotation.tailrec

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.{FieldType, _}
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.sdk._

@RunWith(classOf[JUnitRunner])
class ElasticSearchOutputSpec extends FlatSpec with ShouldMatchers {

  trait BaseValues {

    final val localPort = 9200
    final val remotePort = 9300
    val output = getInstance()
    val outputMultipleNodes = new ElasticSearchOutput("ES-out",
      Map("nodes" ->
        new JsoneyString( s"""[{"node":"host-a","defaultPort":"$remotePort"},{"node":"host-b","defaultPort":"9301"}]""")
        , "dateType" -> "timestamp"),
      None, None)

    def getInstance(host: String = "localhost", port: Int = localPort): ElasticSearchOutput =
      new ElasticSearchOutput("ES-out",
        Map("nodes" -> new JsoneyString( s"""[{"node":"$host","defaultPort":"$port"}]"""), "dateType" -> "timestamp"),
        None, None)
  }

  trait NodeValues extends BaseValues {

    val ipOutput = getInstance("127.0.0.1", localPort)
    val ipv6Output = getInstance("0:0:0:0:0:0:0:1", localPort)
    val remoteOutput = getInstance("dummy", remotePort)
  }

  trait TestingValues extends BaseValues {

    val indexNameType = "sparktatable/sparkta"
    val tableName = "sparktaTable"
    val baseFields = Seq(StructField("string", StringType), StructField("int", IntegerType))
    val schema = StructType(baseFields)
    val tableSchema = TableSchema("ES-out", tableName, schema, "timestamp")
    val extraFields = Seq(StructField("id", StringType, false), StructField("timestamp", TimestampType, false))
    val expectedSchema = StructType(extraFields ++ baseFields)
    val expectedTableSchema = tableSchema.copy(tableName = "id_sparktaTable_timestamp", schema = expectedSchema)
    val properties = Map("nodes" -> new JsoneyString( """[{"node":"localhost","defaultPort":"9200"}]"""), "dateType"
      -> "timestamp")
    override val output = new ElasticSearchOutput("ES-out", properties, None, bcSchema = Some(Seq(tableSchema)))
  }

  trait SchemaValues extends BaseValues {

    val fields = Seq(
      StructField("long", LongType),
      StructField("double", DoubleType),
      StructField("decimal", DecimalType()),
      StructField("int", IntegerType),
      StructField("boolean", BooleanType),
      StructField("date", DateType),
      StructField("timestamp", TimestampType),
      StructField("array", ArrayType(StringType)),
      StructField("map", MapType(StringType, IntegerType)),
      StructField("string", StringType),
      StructField("binary", BinaryType))
    val completeSchema = StructType(fields)
    val definitions = Seq(
      "long".typed(FieldType.LongType),
      "double".typed(FieldType.DoubleType),
      "decimal".typed(FieldType.DoubleType),
      "int".typed(FieldType.IntegerType),
      "boolean".typed(FieldType.BooleanType),
      "date".typed(FieldType.DateType),
      "timestamp".typed(FieldType.DateType),
      "array".typed(FieldType.MultiFieldType),
      "map".typed(FieldType.ObjectType),
      "string" typed FieldType.StringType index "not_analyzed",
      "binary".typed(FieldType.BinaryType)
    )

    def equals(a: TypedFieldDefinition, b: TypedFieldDefinition): Boolean = (a.name, a.`type`) ==(b.name, b.`type`)

    @tailrec
    final def equals(a: Seq[TypedFieldDefinition], b: Seq[TypedFieldDefinition]): Boolean = {
      if (a.nonEmpty && b.nonEmpty)
        if (!equals(a.head, b.head)) false
        else equals(a.drop(1), b.drop(1))
      else true
    }
  }

  "ElasticSearchOutput" should "format properties" in new NodeValues {
    output.nodes should be(Seq(("localhost", 9200)))
    outputMultipleNodes.nodes should be(Seq(("host-a", 9300),("host-b", 9301)))
    output.dateType should be(TypeOp.Timestamp)
    output.isAutoCalculateId should be(true)
    output.isLocalhost should be(true)
    ipOutput.isLocalhost should be(true)
    remoteOutput.isLocalhost should be(false)
    ipv6Output.isLocalhost should be(true)
  }

  it should "return correct types" in new SchemaValues {
    val result = output.getElasticsearchFields(completeSchema)
    equals(result, definitions) should be(true)
  }

  it should "parse correct index name type" in new TestingValues {
    output.indexNameType(tableName) should be(indexNameType)
  }

  it should "get schema fixed id" in new TestingValues {
    output.getSchema should be(Seq(expectedTableSchema))
  }
}
