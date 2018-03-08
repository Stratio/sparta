/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
    val oneL = 1L
    val zero = 0
    val two = 2
    val twoL = 2L
    val oneString = "1"
    val emptyString = ""
    val theLong = 2L
    val theDouble = 2D
    val trueString = "true"
    val falseString = "false"
    val trueBool = true
    val data: Map[String, JSerializable] = Map("someString" -> theString, "someInt" -> one, "someLong" -> theLong,
      "someTrue" -> trueString, "someFalse" -> falseString, "zero" -> zero, "two" -> two, "twoL" -> twoL,
      "someBoolean" -> trueBool, "oneString" -> oneString, "theDouble" -> theDouble, "emptyStr" -> emptyString)
  }

  "ValidatingProperty" should " returs value as String" in new ValuesMap {
    data.getString("someString") should be(theString)
    data.getString("someInt") should be(one.toString)
    data.getString("someInt", "default") should be(one.toString)
    data.getString("dummy", "default") should be("default")
    an[IllegalStateException] should be thrownBy data.getString("otherLong")
  }

  it should "returns value as Option" in new ValuesMap {
    data.getString("someInt", None) should be(Some(one.toString))
    data.getString("dummy", None) should be(None)
    data.getString("emptyStr", None) should be(None)
    data.getString("dummy", Some("dummy")) should be(Some("dummy"))
  }

  it should "returns value as Boolean" in new ValuesMap {
    data.getBoolean("someTrue") should be(true)
    data.getBoolean("someFalse") should be(false)
    data.getBoolean("someInt") should be(true)
    data.getBoolean("zero") should be(false)
    data.getBoolean("someBoolean") should be(true)
    an[Exception] should be thrownBy data.getBoolean("dummy")
    an[IllegalStateException] should be thrownBy data.getBoolean("two")
  }

  it should "returns value as Boolean with default value" in new ValuesMap {
    data.getBoolean("someTrue", false) should be(true)
    data.getBoolean("someFalse", false) should be(false)
    data.getBoolean("someInt", false) should be(true)
    data.getBoolean("zero", false) should be(false)
    data.getBoolean("someBoolean", false) should be(true)
    data.getBoolean("dummy", false) should be(false)
    data.getBoolean("two", false) should be(false)
  }

  it should "returns value as Boolean with default value as Option" in new ValuesMap {
    data.getBoolean("someTrue", None) should be(Some(true))
    data.getBoolean("someFalse", None) should be(Some(false))
    data.getBoolean("someInt", None) should be(Some(true))
    data.getBoolean("zero", None) should be(Some(false))
    data.getBoolean("someBoolean", None) should be(Some(true))
    data.getBoolean("dummy", None) should be(None)
    data.getBoolean("two", None) should be(None)
  }

  it should "returns value as Int" in new ValuesMap {
    data.getInt("oneString") should be(one)
    an[IllegalStateException] should be thrownBy data.getInt("theString")
    an[IllegalStateException] should be thrownBy data.getInt("someString")
    data.getInt("someInt") should be(one)
    data.getInt("someLong") should be(two)
    an[IllegalStateException] should be thrownBy data.getInt("theDouble")
  }

  it should "returns value as Int with default value" in new ValuesMap {
    data.getInt("oneString", 2) should be(one)
    data.getInt("theString", 2) should be(two)
    data.getInt("someString", 2) should be(two)
    data.getInt("someInt", 2) should be(one)
    data.getInt("someLong", 2) should be(two)
    data.getInt("theDouble", 2) should be(two)
  }

  it should "returns value as Int with default value as option" in new ValuesMap {
    data.getInt("oneString", None) should be(Some(one))
    data.getInt("theString", None) should be(None)
    data.getInt("someString", None) should be(None)
    data.getInt("someInt", None) should be(Some(one))
    data.getInt("someLong", None) should be(Some(two))
    data.getInt("theDouble", None) should be(None)
  }

  it should "returns value as Long" in new ValuesMap {
    data.getLong("oneString") should be(oneL)
    an[IllegalStateException] should be thrownBy data.getLong("theString")
    an[IllegalStateException] should be thrownBy data.getLong("someString")
    data.getLong("someInt") should be(oneL)
    data.getLong("someLong") should be(twoL)
    an[IllegalStateException] should be thrownBy data.getLong("theDouble")
  }

  it should "returns value as Long with default value" in new ValuesMap {
    data.getLong("oneString", 2L) should be(oneL)
    data.getLong("theString", 2L) should be(twoL)
    data.getLong("someString", 2L) should be(twoL)
    data.getLong("someInt", 2L) should be(oneL)
    data.getLong("someLong", 2L) should be(twoL)
    data.getLong("theDouble", 2L) should be(twoL)
  }

  it should "returns value as Long with default value as option" in new ValuesMap {
    data.getLong("oneString", None) should be(Some(oneL))
    data.getLong("theString", None) should be(None)
    data.getLong("someString", None) should be(None)
    data.getLong("someInt", None) should be(Some(oneL))
    data.getLong("someLong", None) should be(Some(twoL))
    data.getLong("theDouble", None) should be(None)
  }

  it should "check key" in new ValuesMap {
    data.hasKey("someBoolean") should be(true)
    data.hasKey("dummy") should be(false)
  }

  it should "parse to a connection chain" in {
    val conn = """[{"host":"host1","port":"20304"},{"host":"host2","port":"20304"},{"host":"host3","port":"20304"}]"""
    val validating: ValidatingPropertyMap[String, JsoneyString] =
      new ValidatingPropertyMap[String, JsoneyString](Map("nodes" -> JsoneyString(conn)))

    validating.getMapFromArrayOfValues("nodes") should be(List(
      Map("host" -> "host1", "port" -> "20304"),
      Map("host" -> "host2", "port" -> "20304"),
      Map("host" -> "host3", "port" -> "20304")
    ))
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
