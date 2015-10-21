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

package com.stratio.sparkta.plugin.parser.typeparser

import com.stratio.sparkta.sdk.Event
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TypeParserSpec extends WordSpecLike {

  val StringValue: String = "141633078"
  val StringDecimalValue: String = "141633078.2323"
  val StringDecimalValueWithComma: String = "141633078,2323"
  val StringShortValue: String = "3"
  val StringByteValue: String = "1"
  val inputField = "stringField"
  val otherInputField = "stringField2"
  val outputsFields = Seq("numericField")

  "A TypeParser" should {
    "parse string to int" in {
      val e1 = new Event(Map("stringField" -> StringValue))
      val e2 = new Event(Map("numericField" -> StringValue.toInt))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
            "type" -> "int",
            "newField" -> "numericField")).parse(e1))
    }

    "parse string to long first letter in capital" in {
      val e1 = new Event(Map("stringField" -> StringValue))
      val e2 = new Event(Map("numericField" -> StringValue.toLong))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "Long",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to float first letter in capital" in {
      val e1 = new Event(Map("stringField" -> StringDecimalValue))
      val e2 = new Event(Map("numericField" -> StringDecimalValue.toFloat))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "Float",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to double first letter in capital" in {
      val e1 = new Event(Map("stringField" -> StringDecimalValue))
      val e2 = new Event(Map("numericField" -> StringDecimalValue.toDouble))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "Double",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to long" in {
      val e1 = new Event(Map("stringField" -> StringValue))
      val e2 = new Event(Map("numericField" -> StringValue.toLong))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "long",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to float" in {
      val e1 = new Event(Map("stringField" -> StringDecimalValue))
      val e2 = new Event(Map("numericField" -> StringDecimalValue.toFloat))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "float",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to double" in {
      val e1 = new Event(Map("stringField" -> StringDecimalValue))
      val e2 = new Event(Map("numericField" -> StringDecimalValue.toDouble))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "double",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to short" in {
      val e1 = new Event(Map("stringField" -> StringShortValue))
      val e2 = new Event(Map("numericField" -> StringShortValue.toShort))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "short",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to byte" in {
      val e1 = new Event(Map("stringField" -> StringByteValue))
      val e2 = new Event(Map("numericField" -> StringByteValue.toByte))
      assertResult (e2) (
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "byte",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string with wrong type should throw IllegalArgumentException" in {
      val e1 = new Event(Map("stringField" -> StringByteValue))
      val e2 = new Event(Map("numericField" -> StringByteValue.toByte))

      val err = intercept[IllegalArgumentException] {
        new TypeParser("name", 1, inputField, outputsFields, Map(
          "type" -> "bytedd",
          "newField" -> "numericField")).parse(e1)
      }

      assertResult (err.getMessage) ("Possible values for property type are: Byte, Short, Int, Long, Float and Double")
    }

    "not parse if the input field does not exist" in {
      val e1 = new Event(Map("stringField" -> StringByteValue))
      val e2 = new Event(Map("stringField" -> StringByteValue))
      assertResult (e2) (
        new TypeParser("name", 1, otherInputField, outputsFields, Map(
          "type" -> "byte",
          "newField" -> "numericField")).parse(e1))
    }
  }
}
