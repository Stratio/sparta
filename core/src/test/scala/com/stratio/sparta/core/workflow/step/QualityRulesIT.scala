/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import java.sql.{Date, Timestamp}
import java.util.TimeZone

import com.github.nscala_time.time.Imports.DateTimeZone
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.{DistributedMonad, TemporalSparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class QualityRulesIT extends TemporalSparkContext with Matchers {

  var classTest: Option[DistributedMonad.Implicits.RDDDistributedMonad] = None
  var dataSet: Option[RDD[Row]] = None
  var dataSetComplex: Option[RDD[Row]] = None

  val testTable = "myTable"

  val fakeTimestamp: Timestamp = Timestamp.valueOf("2019-03-15 13:30:00.06")
  val dt = DateTime.now
    .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")))
  val fakeDate: Date = new Date(dt.getMillis)

  val errorManagement = ErrorsManagement()
  val outputOptions = OutputWriterOptions.defaultOutputWriterOptions("tableA", None, Option("tableName"))
  val defaultThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
    value = 3.0,
    operation = ">=",
    `type` = "abs",
    actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
  )

  val defaultQR: SpartaQualityRule = SpartaQualityRule(id = 1,
    metadataPath = "blabla1",
    name = "",
    qualityRuleScope = "RESOURCE",
    logicalOperator = Some("and"),
    metadataPathResourceExtraParams = Seq.empty[PropertyKeyValue],
    enable = true,
    threshold = defaultThreshold,
    predicates = Seq.empty[SpartaQualityRulePredicate],
    stepName = "tableA",
    outputName = "",
    qualityRuleType = Reactive)

  val percentageThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
    value = 90.0,
    operation = ">=",
    `type` = "%",
    actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
  )

  val totalEventsQRDataframe: Long = 7

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
          new GenericRowWithSchema(Array( null, "cyan", 12.0, 10, 1.0, "fantasy", fakeTimestamp, fakeDate), inputSchema),
          new GenericRowWithSchema(Array( "", "cyan", 12.0, 10, 1.0, "fantasy", fakeTimestamp, fakeDate), inputSchema),
          new GenericRowWithSchema(Array("Fernandez", "purple", 14.0,null,null, "comedy",fakeTimestamp, fakeDate), inputSchema)
        )
      val dataInRow = dataIn.map(_.asInstanceOf[Row])
      dataSet = Option(sc.parallelize(dataInRow))

      classTest= Option(new RDDDistributedMonad(dataSet.get) {
        xdSession = sparkSession
        dataSet.get.registerAsTable(xdSession, inputSchema, s"$testTable")
      })

      val qr_predicate1 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("10.0"), field = "price", operation = ">")

      val qr_predicate2 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("13.5"), field = "price", operation = "<")

      val seqQualityRules = Seq(qr_predicate1, qr_predicate2)


      val qualityRule1 = defaultQR.copy(name = "greater 10 less 13.5", predicates = seqQualityRules)

      val result1 = classTest.get.writeRDDTemplate(dataSet.get,
        outputOptions,
        errorManagement,
        Seq.empty[OutputStep[RDD]],
        Seq("input1", "transformation1"),
        Seq(qualityRule1),
        emptyFunction)

      result1 should not be empty

      result1.head.numDiscardedEvents shouldEqual 2
      result1.head.numPassedEvents shouldEqual 5
      result1.head.numTotalEvents shouldEqual totalEventsQRDataframe
      result1.head.satisfied shouldEqual true

    }

   it should "execute correctly a complex QualityRule with (OR)" in {

     /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
       **/

     val qr_predicate1 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "stock", operation = "IS Not null")

     val qr_predicate2 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("14.0"), field = "price", operation = ">=")

     val seqQualityRules = Seq(qr_predicate1, qr_predicate2)

    val qualityRule1 = defaultQR.copy(name = "not null greater or equal than 14.0 price",
      logicalOperator = Some("or"),
      threshold = percentageThreshold,
      predicates = seqQualityRules)


     val expectedDiscards = 1

     val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRule1),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly the EqualOperation" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
     **/

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("1"), field = "section_id", operation = "="))

    val qualityRuleEqual = defaultQR.copy(name = "must be equal", predicates = seqQualityRules)

    val expectedDiscards: Long = 2

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleEqual),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the GreaterEqualOperation and MinorEqualOperation" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("12.0"), field = "price", operation = ">="),
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("14.0"), field = "price", operation = "<="))

    val qualityRuleGEq_MEq = defaultQR.copy(name = "must be greater or equal", predicates = seqQualityRules)

    val expectedDiscards: Long = 2

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleGEq_MEq),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the GreaterOperation and MinorOperation" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("L"), field = "author_surname", operation = ">"),
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("N"), field = "author_surname", operation = "<"))

    val qualityRuleGt_Mt = defaultQR.copy(name = "must start with a letter after L but before N", predicates = seqQualityRules)

    val expectedDiscards: Long = 4

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleGt_Mt),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the InOperation (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val primaryColours = Seq("red","yellow", "blue")
    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = primaryColours, field = "color", operation = "IN"))

    val qualityRuleLike = defaultQR.copy(name = "only primary", predicates = seqQualityRules)

    val expectedDiscards: Long = 4

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the is empty (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/


    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "author_surname", operation = "is EMPty"))

    val qualityRuleReg = defaultQR.copy(name = "is empty", predicates = seqQualityRules)

    val expectedDiscards: Long = 6

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleReg),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly the is NOT empty (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/


    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "author_surname", operation = "is NOT EMPty"))

    val qualityRuleReg = defaultQR.copy(name = "is NOT empty", predicates = seqQualityRules)

    val expectedDiscards: Long = 2

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleReg),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isNullOperation (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "stock", operation = "IS null"))

    val qualityRuleIsNull = defaultQR.copy(name = "must be null", predicates = seqQualityRules)

    val expectedDiscards: Long = 4

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isDateOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "launchDate", operation = "is DAte"))

    val qualityRuleIsNull = defaultQR.copy(name = "must be a date", predicates = seqQualityRules)

    val expectedDiscards: Long = 0

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isNotDateOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "publicationDate", operation = "is NOT DaTe"))

    val qualityRuleIsNull = defaultQR.copy(name = "must not be a date", predicates = seqQualityRules)

    val expectedDiscards: Long = 0

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isNotTimestampOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "author_surname", operation = "is NOT TiMeStAMp"))

    val qualityRuleIsNull = defaultQR.copy(name = "must not be a timestamp", predicates = seqQualityRules)

    val expectedDiscards: Long = 0

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the isTimestampOperation (case insensitive)" in {

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "publicationDate", operation = "is TiMeStAMp"))

    val qualityRuleIsNull = defaultQR.copy(name = "must be a timestamp", predicates = seqQualityRules)

    val expectedDiscards: Long = 0

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the LikeOperation (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/


    val irishPrefix = "M%c%" //Matches Mac, Mc and M*c

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq(irishPrefix), field = "author_surname", operation = "lIKe"))

    val qualityRuleLike = defaultQR.copy(name = "gaelic stile", predicates = seqQualityRules)

    val expectedDiscards: Long = 4

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
}

  it should "execute correctly the NotEqualOperation" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("fantasy"), field = "section", operation = "<>"))

    val qualityRuleIsNull = defaultQR.copy(name = "must be different than", predicates = seqQualityRules)

    val expectedDiscards: Long = 5

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly the NotInOperation (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val nonPrimaryColours = Seq("cyan", "purple")
    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = nonPrimaryColours, field = "color", operation = "NOT IN"))

    val qualityRuleLike = defaultQR.copy(name = "only NON-primary", predicates = seqQualityRules)

    val expectedDiscards: Long = 4

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the NotLikeOperation (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val irishPrefix = "M%c%" //Matches Mac, Mc and M*c

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq(irishPrefix), field = "author_surname", operation = "NOT lIKe"))

    val qualityRuleLike = defaultQR.copy(name = "no gaelic stile", predicates = seqQualityRules)

    val expectedDiscards: Long = 3

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the NotNullOperation (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq.empty[String], field = "stock", operation = "is NOT null"))

    val qualityRuleIsNotNull = defaultQR.copy(name = "must not be null", predicates = seqQualityRules)

    val expectedDiscards: Long = 3

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleIsNotNull),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "execute correctly the RegexOperation (case insensitive)" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val irishPrefixRegex = "^M.*c*"

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq(irishPrefixRegex), field = "author_surname", operation = "regEX"))

    val qualityRuleReg = defaultQR.copy(name = "gaelic stile", predicates = seqQualityRules)

    val expectedDiscards: Long = 4

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleReg),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual true
  }

  it should "not round more than 2 decimals" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/

    val seqQualityRules = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("yellow"), field = "color", operation = "NOT lIKe"))

    val qualityRuleLike = defaultQR.copy(name = "no yellow",threshold = percentageThresholdNotToRound, predicates = seqQualityRules)

    val expectedDiscards: Long = 1

    val result = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleLike),
      emptyFunction)

    result should not be empty

    result.head.numDiscardedEvents shouldEqual expectedDiscards
    result.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result.head.satisfied shouldEqual false
  }

  it should "execute correctly a planned complex QR" in {

    /**+--------------+------+-----+-----+----------+-------+--------------------+----------+
        |author_surname| color|price|stock|section_id|section|     publicationDate|launchDate|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
        |      McNamara|  blue| 15.0| null|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |      McDonald|   red| 11.0|    2|       1.0| sci-fi|2019-03-15 13:30:...|2019-09-29|
        |     MacCormic|yellow| 13.0| null|      null|fantasy|2019-03-15 13:30:...|2019-09-29|
        |         Gomez|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |          null|  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |              |  cyan| 12.0|   10|       1.0|fantasy|2019-03-15 13:30:...|2019-09-29|
        |     Fernandez|purple| 14.0| null|      null| comedy|2019-03-15 13:30:...|2019-09-29|
        +--------------+------+-----+-----+----------+-------+--------------------+----------+
      **/


    val inputSchema = StructType(Seq(StructField("count", LongType)))
    val countElems = dataSet.get.count()
    val dataIn = Seq(new GenericRowWithSchema(Array(countElems), inputSchema))

    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    dataSetComplex = Option(sc.parallelize(dataInRow))

    val qualityRuleComplex = defaultQR.copy( name = "must be equal",
      qualityRuleType = Planned,
      plannedQuery = Some(PlannedQuery(
        query = "select count(*) from ${1} WHERE price > 10.0 AND PRICE < 13.5",
        queryReference = "",
        resources = Seq(ResourcePlannedQuery(1L, "blabla", testTable)))
      )
    )

    val result1 = classTest.get.writeRDDTemplate(dataSetComplex.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleComplex),
      emptyFunction)

    val expectedDiscards: Long = 2

    result1 should not be empty

    result1.head.numDiscardedEvents shouldEqual expectedDiscards
    result1.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result1.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result1.head.satisfied shouldEqual true
  }

  it should "execute correctly a planned simple QR" in {

    val qr_predicate1 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("10.0"), field = "price", operation = ">")

    val qr_predicate2 = SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("13.5"), field = "price", operation = "<")

    val seqQualityRules = Seq(qr_predicate1, qr_predicate2)

    val expectedDiscards: Long = 2

    val qualityRuleComplex = defaultQR.copy( name = "must be equal", qualityRuleType = Planned, predicates = seqQualityRules)

    val result1 = classTest.get.writeRDDTemplate(dataSet.get,
      outputOptions,
      errorManagement,
      Seq.empty[OutputStep[RDD]],
      Seq("input1", "transformation1"),
      Seq(qualityRuleComplex),
      emptyFunction)

    result1 should not be empty

    result1.head.numDiscardedEvents shouldEqual expectedDiscards
    result1.head.numPassedEvents shouldEqual totalEventsQRDataframe - expectedDiscards
    result1.head.numTotalEvents shouldEqual totalEventsQRDataframe
    result1.head.satisfied shouldEqual true
  }

}
