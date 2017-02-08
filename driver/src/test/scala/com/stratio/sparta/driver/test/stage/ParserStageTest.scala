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
package com.stratio.sparta.driver.test.stage

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.driver.stage.{LogError, ParserStage}
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.policy.{OutputFieldsModel, PolicyModel, TransformationsModel}
import com.stratio.sparta.serving.core.utils.ReflectionUtils
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class ParserStageTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  case class TestStage(policy: PolicyModel) extends ParserStage with LogError

  lazy val ReflectionUtils = new ReflectionUtils


  def mockPolicy: PolicyModel = {
    val policy = mock[PolicyModel]
    when(policy.storageLevel).thenReturn(Some("StorageLevel"))
    when(policy.id).thenReturn(Some("id"))
    policy
  }

  "ParserStage" should "Generate an empty seq with no transformations" in {
    val policy = mockPolicy
    val reflection = mock[ReflectionUtils]
    when(policy.transformations).thenReturn(Seq.empty)
    val result = TestStage(policy).parserStage(reflection, Map.empty)

    result should be(Seq.empty)
  }

  "ParserStage" should "Generate a single parser" in {
    val policy = mockPolicy
    val outputFieldModel = OutputFieldsModel("output", Some("long"))
    val input = Some("input")
    val transformationSchema = Seq(outputFieldModel)
    val configuration: Map[String, JsoneyString] = Map("conf" -> JsoneyString("prop"))
    val transformation = TransformationsModel("Test", 1, input, transformationSchema, configuration = configuration)
    val outputScheme = StructType(Seq.empty)
    when(policy.transformations).thenReturn(Seq(transformation))

    val result = TestStage(policy).parserStage(ReflectionUtils, Map("1" -> outputScheme))

    result.size should be(1)
    val parser = result.head.asInstanceOf[TestParser]
    parser.getOrder should be(1)
    parser.inputField should be(input)
    parser.outputFields should be(List("output"))
    parser.schema should be(outputScheme)
    parser.properties should be(configuration)
  }

  "ParserStage" should "Generate a two parsers" in {
    val policy = mockPolicy
    val outputFieldModel = OutputFieldsModel("output", Some("long"))
    val input = Some("input")
    val transformationSchema = Seq(outputFieldModel)
    val configuration: Map[String, JsoneyString] = Map("conf" -> JsoneyString("prop"))
    val transformation = TransformationsModel("Test", 1, input, transformationSchema, configuration = configuration)
    val secondTransformation =
      TransformationsModel("Test", 2, input, transformationSchema, configuration = configuration)
    val outputScheme = StructType(Seq.empty)
    when(policy.transformations).thenReturn(Seq(transformation, secondTransformation))

    val result = TestStage(policy).parserStage(ReflectionUtils, Map("1" -> outputScheme, "2" -> outputScheme))

    result.size should be(2)
    val parser = result(1).asInstanceOf[TestParser]
    parser.getOrder should be(2)
    parser.inputField should be(input)
    parser.outputFields should be(List("output"))
    parser.schema should be(outputScheme)
    parser.properties should be(configuration)
  }

  "ParserStage" should "Fail when reflectionUtils don't behave correctly" in {
    val reflection = mock[ReflectionUtils]
    val policy = mockPolicy
    val outputFieldModel = OutputFieldsModel("output", Some("long"))
    val input = Some("input")
    val transformationSchema = Seq(outputFieldModel)
    val configuration: Map[String, JsoneyString] = Map("conf" -> JsoneyString("prop"))
    val transformation = TransformationsModel("Test", 1, input, transformationSchema, configuration = configuration)
    val outputScheme = StructType(Seq.empty)
    val myInputClass = mock[Input]
    when(reflection.tryToInstantiate(any(), any())).thenReturn(myInputClass)
    when(policy.transformations).thenReturn(Seq(transformation))

    the[IllegalArgumentException] thrownBy {
      TestStage(policy).parserStage(reflection, Map("1" -> outputScheme))
    } should have message "Something gone wrong creating the parser: Test. Please re-check the policy."
  }

  "ParserStage" should "Fail gracefully with bad input" in {
    val reflection = mock[ReflectionUtils]
    val policy = mockPolicy
    val outputFieldModel = OutputFieldsModel("output", Some("long"))
    val input = Some("input")
    val transformationSchema = Seq(outputFieldModel)
    val configuration: Map[String, JsoneyString] = Map("conf" -> JsoneyString("prop"))
    val transformation = TransformationsModel("Test", 1, input, transformationSchema, configuration = configuration)
    val outputScheme = StructType(Seq.empty)
    when(reflection.tryToInstantiate(any(), any())).thenThrow(new RuntimeException("Fake"))
    when(policy.transformations).thenReturn(Seq(transformation))

    the[IllegalArgumentException] thrownBy {
      TestStage(policy).parserStage(reflection, Map("1" -> outputScheme))
    } should have message "Something gone wrong creating the parser: Test. Please re-check the policy."
  }

  it should "parse a event" in {
    val parser: Parser = mock[Parser]
    val event: Row = mock[Row]
    val parsedEvent = Seq(mock[Row])
    when(parser.parse(event)).thenReturn(parsedEvent)

    val result = ParserStage.parseEvent(event, parser)
    result should be(parsedEvent)
  }
  it should "return none if a parse Event fails" in {
    val parser: Parser = mock[Parser]
    val event: Row = mock[Row]
    when(parser.parse(event)).thenThrow(new RuntimeException("testEx"))

    val result = ParserStage.parseEvent(event, parser)
    result should be(Seq.empty)
  }
}

class TestParser(val order: Integer,
                 val inputField: Option[String],
                 val outputFields: Seq[String],
                 val schema: StructType,
                 override val properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  override def parse(data: Row): Seq[Row] = throw new RuntimeException("Fake implementation")
}