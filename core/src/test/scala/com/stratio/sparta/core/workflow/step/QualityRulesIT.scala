/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import java.sql.{Date, Timestamp}
import java.util.TimeZone

import com.github.nscala_time.time.Imports.DateTimeZone
import com.stratio.sparta.core.{DistributedMonad, TemporalSparkContext}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.core.DistributedMonad.Implicits._
import org.joda.time.DateTime

@RunWith(classOf[JUnitRunner])
class QualityRulesIT extends TemporalSparkContext with Matchers {

  var classTest: Option[DistributedMonad.Implicits.RDDDistributedMonad] = None
  var dataSet: Option[RDD[Row]] = None

  val fakeTimestamp: Timestamp = Timestamp.valueOf("2019-03-15 13:30:00.06")
  val dt = DateTime.now
    .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")))
  val fakeDate: Date = new Date(dt.getMillis)

  val errorManagement = ErrorsManagement()
  val outputOptions = OutputWriterOptions.defaultOutputOptions("tableA", None, Option("tableName"))
  val defaultThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
    value = 3.0,
    operation = ">=",
    `type` = "abs",
    actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
  )

  val percentageThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
    value = 90.0,
    operation = ">=",
    `type` = "%",
    actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
  )

  val percentageThresholdNotToRound: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
    value = 79.98,
    operation = "<=",
    `type` = "%",
    actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
  )

  def emptyFunction: (DataFrame, SaveModeEnum.Value, Map[String, String]) => Unit =
    (_, _, _) => println("Fire in the hole!")


  "SparkQualityRule" should "execute correctly a complex QualityRule (AND)" in {

      val inputSchema = StructType(Seq(StructField("author_surname", StringType), StructField("color", StringType), StructField("price", DoubleType), StructField("stock", IntegerType), StructField("section_id", DoubleType), StructField("section", StringType), StructField("publicationDate", TimestampType), StructField("launchDate", DateType) ))
      val dataIn =
        Seq(
          new GenericRowWithSchema(Array("McNamara", "blue", 15.0, null, 1.0, "fantasy",fakeTimestamp, fakeDate), inputSchema),
          new GenericRowWithSchema(Array("McDonald", "red", 11.0, 2,1.0, "sci-fi", fakeTimestamp, fakeDate), inputSchema),
          new GenericRowWithSchema(Array("MacCormic", "yellow", 13.0, null, null, "fantasy",fakeTimestamp, fakeDate), inputSchema),
          new GenericRowWithSchema(Array("Gomez", "cyan", 12.0, 10, 1.0, "fantasy", fakeTimestamp, fakeDate), inputSchema),
          new GenericRowWithSchema(Array("Fernandez", "purple", 14.0,null,null, "comedy",fakeTimestamp, fakeDate), inputSchema)
        )
      val dataInRow = dataIn.map(_.asInstanceOf[Row])
      dataSet = Option(sc.parallelize(dataInRow))

      classTest= Option(new RDDDistributedMonad(dataSet.get) {
        xdSession = sparkSession
      })

      val qr_predicate1 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("10.0"), field = "price", operation = ">")

      val qr_predicate2 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("13.5"), field = "price", operation = "<")

      val seqQualityRules = Seq(qr_predicate1, qr_predicate2)


      val qualityRule1 = SpartaQualityRule(id = 1, metadataPath = "blabla1",
        name = "greater 10 less 13.5", qualityRuleScope = "data", logicalOperator = "and",
        enable = true,
        threshold = defaultThreshold,
        predicates = seqQualityRules,
        stepName = "tableA",
        outputName = "")


      val result1 = classTest.get.writeRDDTemplate(dataSet.get,
        outputOptions.outputStepWriterOptions("tableA", ""),
        errorManagement,
        Seq.empty[OutputStep[RDD]],
        Seq("input1", "transformation1"),
        Seq(qualityRule1),
        emptyFunction)

      result1 should not be empty

      result1.head.numDiscardedEvents shouldEqual 2
      result1.head.numPassedEvents shouldEqual 3
      result1.head.numTotalEvents shouldEqual 5
      result1.head.satisfied shouldEqual true

    }

   it should "execute correctly a complex QualityRule with (OR)" in {

     val qr_predicate1 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "stock", operation = "IS Not null")

     val qr_predicate2 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("14.0"), field = "price", operation = ">=")

     val seqQualityRules = Seq(qr_predicate1, qr_predicate2)

    val qualityRule1 = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "greater 10 less 13.5", qualityRuleScope = "data", logicalOperator = "or",
      enable = true,
      threshold = percentageThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")


    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRule1),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 1
    result.head.numPassedEvents shouldEqual 4
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly the EqualOperation" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("1"), field = "section_id", operation = "="))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must be equal", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the GreaterEqualOperation and MinorEqualOperation" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("12.0"), field = "price", operation = ">="),
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("14.0"), field = "price", operation = "<="))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must be greater or equal", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the GreaterOperation and MinorOperation" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("L"), field = "author_surname", operation = ">"),
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("N"), field = "author_surname", operation = "<"))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must start with a letter after L but before N", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the InOperation (case insensitive)" in {

    val primaryColours = Seq("red","yellow", "blue")
    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = primaryColours, field = "color", operation = "IN"))

    val qualityRuleLike = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "only primary", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isNullOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "stock", operation = "IS null"))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must be null", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isDateOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "launchDate", operation = "is DAte"))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must be a date", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 0
    result.head.numPassedEvents shouldEqual 5
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isNotDateOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "publicationDate", operation = "is NOT DaTe"))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must not be a date", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 0
    result.head.numPassedEvents shouldEqual 5
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isNotTimestampOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "author_surname", operation = "is NOT TiMeStAMp"))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must not be a timestamp", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 0
    result.head.numPassedEvents shouldEqual 5
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isTimestampOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "publicationDate", operation = "is TiMeStAMp"))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must be a timestamp", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 0
    result.head.numPassedEvents shouldEqual 5
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the LikeOperation (case insensitive)" in {

    val irishPrefix = "M%c%" //Matches Mac, Mc and M*c

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq(irishPrefix), field = "author_surname", operation = "lIKe"))

    val qualityRuleLike = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "gaelic stile", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
}

  it should "execute correctly the NotEqualOperation" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("fantasy"), field = "section", operation = "<>"))

    val qualityRuleIsNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must be different than", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 3
    result.head.numPassedEvents shouldEqual 2
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly the NotInOperation (case insensitive)" in {

    val nonPrimaryColours = Seq("cyan", "purple")
    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = nonPrimaryColours, field = "color", operation = "NOT IN"))

    val qualityRuleLike = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "only NON-primary", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the NotLikeOperation (case insensitive)" in {

    val irishPrefix = "M%c%" //Matches Mac, Mc and M*c

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq(irishPrefix), field = "author_surname", operation = "NOT lIKe"))

    val qualityRuleLike = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "no gaelic stile", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 3
    result.head.numPassedEvents shouldEqual 2
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly the NotNullOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "stock", operation = "is NOT null"))

    val qualityRuleIsNotNull = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "must not be null", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNotNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 3
    result.head.numPassedEvents shouldEqual 2
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly the RegexOperation (case insensitive)" in {

    val irishPrefixRegex = "^M.*c*"

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq(irishPrefixRegex), field = "author_surname", operation = "regEX"))

    val qualityRuleReg = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "gaelic stile", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleReg),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 2
    result.head.numPassedEvents shouldEqual 3
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual true
  }

  it should "not round more than 2 decimals" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("yellow"), field = "color", operation = "NOT lIKe"))

    val qualityRuleLike = SpartaQualityRule(id = 1, metadataPath = "blabla1",
      name = "no yellow", qualityRuleScope = "data", logicalOperator = "and",
      enable = true,
      threshold = percentageThresholdNotToRound,
      predicates = seqQualityRules,
      stepName = "tableA",
      outputName = "")

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions.outputStepWriterOptions("tableA", ""),
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual 1
    result.head.numPassedEvents shouldEqual 4
    result.head.numTotalEvents shouldEqual 5
    result.head.satisfied shouldEqual false
  }

}
