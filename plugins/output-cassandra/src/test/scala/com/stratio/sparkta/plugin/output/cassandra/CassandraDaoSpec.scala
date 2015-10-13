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

package com.stratio.sparkta.plugin.output.cassandra

import com.datastax.spark.connector.cql.CassandraConnector
import com.stratio.sparkta.plugin.output.cassandra.dao.CassandraDAO
import com.stratio.sparkta.sdk.TableSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType, _}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class CassandraDaoSpec extends FlatSpec with Matchers with MockitoSugar with CassandraDAO {

  val cassandraConector = mock[CassandraConnector]
  val tableSchema = Seq(TableSchema("outputName", "dim1", StructType(Array(
    StructField("dim1", StringType, false))), "minute"))
  val structField = StructField("name", StringType, false)
  val schema: StructType = StructType(Array(structField))

  "createIndex" should "return true" in {

    val res = createIndex(cassandraConector, "tablename", "fieldname")
    res should be (true)
  }

  "createIndexes" should "return true" in {

    val res = createIndexes(cassandraConector, tableSchema, false)
    res should be (true)
  }

  "createTextIndexes" should "return true" in {

    val res = createTextIndexes(cassandraConector, tableSchema)
    res should be (true)
  }

  "getTextIndexSentence" should "return the schema" in {

    val fields = Array("field1", "field2")
    val res = getTextIndexSentence(fields)

    res should be ("""'schema' :'{ fields : { field1 : {type : "field1" },field2 : {type : "field2" } } }'""")
  }

  "dataTypeToCassandraType" should "return the type" in {

    val res = dataTypeToCassandraType(dataType = TimestampType)
    res should be ("timestamp")
  }

  "schemaToPkCcolumns" should "return the schema" in {

    val res = schemaToPkCcolumns(schema, "cluster", false)
    res should be (Some("(name text, PRIMARY KEY (name))"))
  }

  "createTable" should "return true" in {

    val res = createTable(cassandraConector, "tablename", schema, "cluster", false)
    res should be (true)
  }
  override def keyspace: String = "sparkta"

  override def keyspaceClass: String = "sparktaCLass"

  override def refreshSeconds: String = "seconds"

  override def textIndexFields: Option[Array[String]] = Option(Array("text1"))

  override def textIndexName: String = "textindexname"

  override def clusteringPrecisions: Option[Array[String]] = None

  override def analyzer: String = "analyzer"

  override def indexFields: Option[Array[String]] = Option(Array("index1"))

  override def replicationFactor: String = "replicationFactor"

  override def dateFormat: String = "dateformat"
}
