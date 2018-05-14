/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.crossdata

import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.datasource.models.{OffsetConditions, OffsetField, OffsetOperator}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import com.stratio.sparta.plugin.utils.TestUtils._
import org.scalactic._
import java.io.{Serializable => JSerializable}


@RunWith(classOf[JUnitRunner])
class CrossdataInputStepStreamingTest extends WordSpec with Matchers with MockitoSugar {

  val ssc = mock[StreamingContext]
  val xdSession = mock[XDSession]
  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

  "CrossdataInputStep" should {
      val offsetFields =
        """[
          |{
          |"offsetField":"id",
          |"offsetOperator":">=",
          |"offsetValue": "500"
          |},
          |{
          |"offsetField":"storeID",
          |"offsetOperator":">=",
          |"offsetValue": "75"
          |},
          |{
          |"offsetField":"cashierID",
          |"offsetOperator":">=",
          |"offsetValue": "1002"
          |}
          |]
        """.stripMargin

      val properties = Map("offsetFields" -> JsoneyString(offsetFields))
      val input = new CrossdataInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)

      val conditions = OffsetConditions(
        input.offsetItems,
        input.limitRecords)

      val complexConditions = conditions.copy(
        fromOffset = conditions.fromOffset.map( x =>
        OffsetField(
          x.name,
          OffsetOperator.toMultiProgressOperator(x.operator),
          x.value
        )))

    "parse and concatenate correctly offset options" in {
      input.offsetItems should contain theSameElementsInOrderAs Seq(
        OffsetField("id",OffsetOperator.>=,Some("500")),
        OffsetField("storeID",OffsetOperator.>=,Some("75")),
        OffsetField("cashierID",OffsetOperator.>=,Some("1002")))
    }
    "create a simple WHERE condition query if simple operators" in {
      val actualConditionsSimpleString =
        conditions.extractConditionSentence(None)

      val expectedConditionsSimpleString = " WHERE id >= '500' AND storeID >= '75' AND cashierID >= '1002'"

      assert((actualConditionsSimpleString === expectedConditionsSimpleString)
        (after being whiteSpaceNormalisedAndTrimmed))
    }

    "append previous WHERE condition to the new query if simple operators" in {
      val actualConditionsSimpleString =
        conditions.extractConditionSentence(Option("name > The"))

      val expectedConditionsSimpleString =
        " WHERE id >= '500' AND storeID >= '75' AND cashierID >= '1002' AND name > The"

      assert((actualConditionsSimpleString === expectedConditionsSimpleString)
        (after being whiteSpaceNormalisedAndTrimmed))
    }

    "create a complex WHERE condition query if incremental/decremental operators" in {
      val actualConditionsComplexString = complexConditions
        .extractConditionSentence(None)

      val expectedConditionsComplexString =
        "  WHERE ( storeID = '75' AND id = '500' AND cashierID >'1002' ) OR ( id = '500' AND storeID >'75' ) " +
          "OR id >'500'"

      assert((actualConditionsComplexString === expectedConditionsComplexString)
        (after being whiteSpaceNormalisedAndTrimmed))
    }

    "create a simple ORDER BY condition no matter the operators" in {
      val actualConditionsComplexString = conditions.extractOrderSentence("select * from tableA")
      val actualConditionsSimpleString = complexConditions.extractOrderSentence("select * from tableA")
      val expectedConditionsString = " ORDER BY id DESC ,storeID DESC ,cashierID DESC"

      assert(Seq(actualConditionsComplexString, actualConditionsSimpleString)
        .forall(actual => (actual === expectedConditionsString)(after being whiteSpaceNormalised)))
    }

    "if no offsetConditions are passed should not add any WHERE or ORDER BY query unless it is in the sql query" in {
      val offsetFields = "[]"
      val properties = Map("offsetFields" -> JsoneyString(offsetFields))
      val input = new CrossdataInputStepStreaming("name",
        outputOptions, Option(ssc), xdSession, properties)
      val conditions = OffsetConditions(
        input.offsetItems,
        input.limitRecords)

      val actualConditionsComplexString = conditions.extractConditionSentence(None)
      val expectedConditionsString = ""
      assert((actualConditionsComplexString === expectedConditionsString)
        (after being whiteSpaceNormalised))

      val actualOrderComplexString = conditions.extractOrderSentence("select * from tableA")
      val expectedOrderComplexString = ""

      actualOrderComplexString should be (expectedOrderComplexString)

      val actualConditionsComplexStringPrevious = conditions
        .extractConditionSentence(Some("storeID = '75' AND id = '500'"))
      val expectedConditionsStringPrevious = "WHERE storeID = '75' AND id = '500'"
      assert((actualConditionsComplexStringPrevious === expectedConditionsStringPrevious)
        (after being whiteSpaceNormalisedAndTrimmed))
    }
  }
}
