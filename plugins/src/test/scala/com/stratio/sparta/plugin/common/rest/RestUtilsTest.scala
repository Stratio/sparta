/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.rest

import java.time.LocalDate

import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class RestUtilsTest extends FlatSpec with Matchers {


  "RestTransformStep" should "allow to find a replaceable field" in {
    Seq("https://sparta.labs.stratio.com/users/${userid}", "<xml-doc>\n<users>\n<${field${userid}}").foreach{ url =>
      RestUtils.findReplaceableFields(url) should contain theSameElementsAs Seq("userid")
    }
  }
  it should "allow to find muliple replaceable fields" in {
    Seq(
      "https://sparta.labs.stratio.com/user/${field_1}/job/${field_2}",
      "<xml-doc>\n<users>\n<${field${field_1}}\n${field_2}").foreach{ url =>
      RestUtils.findReplaceableFields(url).toSeq should contain theSameElementsAs Seq("field_1", "field_2")
    }
  }

  it should "not match wrong replaceable fields" in {
    val url = "<xml-doc>${}\n<users>$${\n}<${field${}}"
    RestUtils.findReplaceableFields(url).toSeq shouldBe empty
  }

  it should "replace placeholders with row data" in {
    val schema: StructType = StructType(Seq(StructField("id", IntegerType), StructField("name", StringType), StructField("surname", StringType)))

    import RestUtils._
    replaceInputFields(
      new GenericRowWithSchema(Array(1, "jhon", "smith"), schema),
      Map("name" -> false),
      "http://stratio.com/users/${name}"
    ) shouldBe "http://stratio.com/users/jhon"

    replaceInputFields(
      new GenericRowWithSchema(Array(1, "jhon", "smith"), schema),
      Map("id" -> true, "name" -> false, "surname" -> true),
      "{id: ${id}, name: ${name}, surname: ${surname}}"
    ) shouldBe """{id: 1, name: jhon, surname: "smith"}"""


    val structOfStructRow = new GenericRowWithSchema(
      Array(
        java.sql.Date.valueOf(LocalDate.of(2015, 11, 30)),
        42,
        new GenericRowWithSchema(
          Array("a glass of wine a day keeps the doctor away", 1138),
          StructType(StructField("structField1",StringType,true)::StructField("structField2",IntegerType,true)::Nil)
        )
      ),
      StructType(
        List(
          StructField("field1", DateType, true),
          StructField("field2", IntegerType, true),
          StructField("struct1",StructType(StructField("structField1",StringType,true)::StructField("structField2",IntegerType,true)::Nil),true)
        )
      )
    )

    replaceInputFields(
      structOfStructRow,
      Map("field1" -> true, "struct1" -> true),
      "{field1: ${field1}, struct1: ${struct1}}"
    ) shouldBe """{field1: "2015-11-30", struct1: {"structField1":"a glass of wine a day keeps the doctor away","structField2":1138}}"""


  }

}
