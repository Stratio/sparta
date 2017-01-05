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
package com.stratio.sparta.sdk.properties

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.properties.models._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatingPropertyMapTest extends FlatSpec with ShouldMatchers {

  trait ValuesMap {

    val theString = "Sparta is awesome!"
    val one = 1
    val zero = 0
    val two = 2
    val oneString = "1"
    val theLong = 2L
    val theDouble = 2D
    val trueString = "true"
    val falseString = "false"
    val trueBool = true
    val data: Map[String, JSerializable] = Map("someString" -> theString, "someInt" -> one, "someLong" -> theLong,
      "someTrue" -> trueString, "someFalse" -> falseString, "zero" -> zero, "two" -> two,
      "someBoolean" -> trueBool, "oneString" -> oneString, "theDouble" -> theDouble)
  }

  "ValidatingProperty" should " returs value as String" in new ValuesMap {
    data.getString("someString") should be(theString)
    data.getString("someInt") should be(one.toString)
    data.getString("someInt", "default") should be(one.toString)
    data.getString("dummy", "default") should be("default")
    an[IllegalStateException] should be thrownBy data.getString("otherLong")
  }

  it should "returs value as Option" in new ValuesMap {
    data.getString("someInt", None) should be(Some(one.toString))
    data.getString("dummy", None) should be(None)
    data.getString("dummy", Some("dummy")) should be(Some("dummy"))
  }

  it should "returs value as Boolean" in new ValuesMap {
    data.getBoolean("someTrue") should be(true)
    data.getBoolean("someFalse") should be(false)
    data.getBoolean("someInt") should be(true)
    data.getBoolean("zero") should be(false)
    data.getBoolean("someBoolean") should be(true)
    an[Exception] should be thrownBy data.getBoolean("dummy")
    an[IllegalStateException] should be thrownBy data.getBoolean("two")
  }

  it should "returs value as Int" in new ValuesMap {
    data.getInt("oneString") should be(one)
    an[IllegalStateException] should be thrownBy data.getInt("theString")
    an[IllegalStateException] should be thrownBy data.getInt("someString")
    data.getInt("someInt") should be(one)
    data.getInt("someLong") should be(two)
    an[IllegalStateException] should be thrownBy data.getInt("theDouble")
  }

  it should "check key" in new ValuesMap {
    data.hasKey("someBoolean") should be(true)
    data.hasKey("dummy") should be(false)
  }

  it should "parse to a connection chain" in {
    val conn = """[{"host":"host1","port":"20304"},{"host":"host2","port":"20304"},{"host":"host3","port":"20304"}]"""
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("nodes" -> JsoneyString(conn)))

    validating.getMapFromJsoneyString("nodes") should be(List(
      Map("host" -> "host1", "port" -> "20304"),
      Map("host" -> "host2", "port" -> "20304"),
      Map("host" -> "host3", "port" -> "20304")
    ))
  }

  it should "parse to hostPort Model" in {
    val portStr = "20304"
    val conn = """[{"host":"host1","port":20304},{"host":"host2","port":"20304"},{"host":"host3","port":"20304"}]"""
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("hostsPorts" -> JsoneyString(conn)))

    validating.getHostsPorts("hostsPorts") should be(HostsPortsModel(Seq(
      HostPortModel("host1", portStr),
      HostPortModel("host2", portStr),
      HostPortModel("host3", portStr)
    )))
  }

  it should "parse to queries Model" in {
    val query = "select"
    val conn =
      """[
        |{"field":"field1","query":"select"},
        |{"field":"field2","query":"select"},
        |{"field":"field3","query":"select"}
        |]""".stripMargin
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("queries" -> JsoneyString(conn)))

    validating.getPropertiesQueries("queries") should be(PropertiesQueriesModel(Seq(
      PropertiesQueryModel("field1", query),
      PropertiesQueryModel("field2", query),
      PropertiesQueryModel("field3", query)
    )))
  }

  it should "parse to options" in {
    val query = "select"
    val conn =
      """[
        |{"field":"field1","query":"select"},
        |{"field":"field2","query":"select"},
        |{"field":"field3","query":"select"}
        |]""".stripMargin
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("queries" -> JsoneyString(conn)))

    validating.getOptionsList("queries", "field", "query") should be(Map(
      "field1" -> query,
      "field2" -> query,
      "field3" -> query
    ))
  }


}
