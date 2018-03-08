/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka.serializers

import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class RowSerializerTest  extends WordSpec with Matchers with MockitoSugar {

  "RowSerializer" should {

    "return a correct values in row string" in {
      val properties = Map("value.serializer.outputFormat" -> "ROW")
      val desSerializer = new RowSerializer
      desSerializer.configure(properties, false)
      val message = "Stratio Sparta"
      val schema = StructType(Seq(StructField("rawField", StringType)))
      val row = new GenericRowWithSchema(Array(message), schema)
      val result = new String(desSerializer.serialize("topic", row))
      val expected = message

      result should be(expected)
    }

    "return a correct values in json string" in {
      val properties = Map("value.serializer.outputFormat" -> "JSON")
      val desSerializer = new RowSerializer
      desSerializer.configure(properties, false)
      val message = "Stratio Sparta"
      val schema = StructType(Seq(StructField("rawField", StringType)))
      val row = new GenericRowWithSchema(Array(message), schema)
      val result = new String(desSerializer.serialize("topic", row))
      val expected = s"""{"rawField":"$message"}"""

      result should be(expected)
    }

  }
}
