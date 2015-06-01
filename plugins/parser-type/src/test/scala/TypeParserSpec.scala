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

import com.stratio.sparkta.sdk.Event
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TypeParserSpec extends WordSpecLike {

  val StringValue: String = "141633078"
  val StringDecimalValue: String = "141633078.2323"
  val StringDecimalValueWithComma: String = "141633078,2323"

  "A TypeParser" should {
    "parse string to int" in {
      val e1 = new Event(Map("stringField" -> StringValue))
      val e2 = new Event(Map("numericField" -> StringValue.toInt))
      assertResult (e2) (
        new TypeParser(Map(
            "sourceField" -> "stringField",
            "type" -> "Int",
            "newField" -> "numericField")).parse(e1))
    }

    "parse string to long" in {
      val e1 = new Event(Map("stringField" -> StringValue))
      val e2 = new Event(Map("numericField" -> StringValue.toLong))
      assertResult (e2) (
        new TypeParser(Map(
          "sourceField" -> "stringField",
          "type" -> "Long",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to float" in {
      val e1 = new Event(Map("stringField" -> StringDecimalValue))
      val e2 = new Event(Map("numericField" -> StringDecimalValue.toFloat))
      assertResult (e2) (
        new TypeParser(Map(
          "sourceField" -> "stringField",
          "type" -> "Float",
          "newField" -> "numericField")).parse(e1))
    }

    "parse string to double" in {
      val e1 = new Event(Map("stringField" -> StringDecimalValue))
      val e2 = new Event(Map("numericField" -> StringDecimalValue.toDouble))
      assertResult (e2) (
        new TypeParser(Map(
          "sourceField" -> "stringField",
          "type" -> "Double",
          "newField" -> "numericField")).parse(e1))
    }
  }
}
