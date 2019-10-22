/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.models.ResourcePlannedQuery
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RegexUtilsTest extends WordSpec with Matchers{
  val testRegex = RegexUtils
  "RegexUtils" when {
    val testQuery: String = "select count(*) from ${1} join ${2} on ${1}.id = ${2}.id"

    val resourceXD = ResourcePlannedQuery(
      id = 1,
      metadataPath = "newtestsparta://newtestsparta>/:random:",
      typeResource = QualityRuleResourceTypeEnum.XD,
      resource = "random")

    val resourceHDFS = ResourcePlannedQuery(
      id = 2,
      metadataPath = "tenantqa-hdfs-example://test/qr/random>/:random:",
      typeResource = QualityRuleResourceTypeEnum.HDFS,
      resource = "random1")

    val resourceJDBC = ResourcePlannedQuery(
      id = 3,
      metadataPath = "tenantqa-postgresqa.tenantqa://governance>/:governance.random:",
      typeResource = QualityRuleResourceTypeEnum.JDBC,
      resource = "governance.random")


    val testSeq: Seq[ResourcePlannedQuery] = Seq(resourceXD, resourceHDFS)

    "sizeAndUnit(sizeString: String)" should {
      "extract the size and the unit if string complies with the regex" in {
        val (size1,unit1) = testRegex.sizeAndUnit("25KB")
        (size1, unit1) shouldEqual((25,"KB"))
      }
      "throw an exception if string does not comply with the regex" in {
        val caughtException = intercept[RuntimeException] {
          testRegex.sizeAndUnit("1hshshsh")
          }
        assert(caughtException.getMessage.contains("Cannot extract size and unit"))
      }
    }
    "replacePlaceholdersWithResources" should {

      "correctly replace the resources inside the query" in {
        val expectedQuery = s"select count(*) from ${testRegex.resourceUniqueTableName(resourceXD)} join ${testRegex.resourceUniqueTableName(resourceHDFS)} on ${testRegex.resourceUniqueTableName(resourceXD)}.id = ${testRegex.resourceUniqueTableName(resourceHDFS)}.id"
        testRegex.replacePlaceholdersWithResources(testQuery, testSeq) should equal(expectedQuery)
      }

      "not replace anything if there are no placeholders" in {
        val testQuery2 = "select count(*) from table"
        testRegex.replacePlaceholdersWithResources(testQuery2, testSeq) should equal(testQuery2)
      }

      "not replace anything if there are no resources" in {
        testRegex.replacePlaceholdersWithResources(testQuery, Seq.empty[ResourcePlannedQuery]) should equal(testQuery)
      }

      "not replace the placeholder if there is no related resource" in {
        val expectedQuery = s"select count(*) from ${testRegex.resourceUniqueTableName(resourceXD)} join " + "${2}" + s" on ${testRegex.resourceUniqueTableName(resourceXD)}.id = "+"${2}.id"
        testRegex.replacePlaceholdersWithResources(testQuery, Seq(resourceXD)) should equal(expectedQuery)
      }
    }
    "findResourcesUsed" should {
      "retrieve all the resources" in {
        val res: Seq[ResourcePlannedQuery] = testRegex.findResourcesUsed(testQuery, testSeq)
        res should contain theSameElementsAs testSeq
      }

      "retrieve no resources" in {
        testRegex.findResourcesUsed(testQuery, Seq.empty[ResourcePlannedQuery]) shouldBe empty
      }
    }

    "containsPlaceholders" should {
      "return false if no ${val} is present" in {
        testRegex.containsPlaceholders("No placeholders here") shouldBe false
      }

      "return true if a ${val} is present" in {
        testRegex.containsPlaceholders("No placeholders here. Kidding, here is one:${1}") shouldBe true
      }
    }

    "valuePlaceholders" should {
      "return the value if the placeholder is valid e.g. ${1}" in {
        testRegex.valuePlaceholder("${1}") shouldBe "1"
      }

      "return the previous text if the placeholder is invalid e.g. ${ajkjakjakl}" in {
        testRegex.valuePlaceholder("${ajkjakjakl}") shouldBe "${ajkjakjakl}"
      }
    }

    "cleanOutControlChar" should {
      import RegexUtils._

      "return the string same as it is if no control char were there" in {
        "This is a simple string".cleanOutControlChar shouldBe "This is a simple string"
      }

      "return the string without control chars" in {
        val charAck: Char = 6 //Acknowledge character	ACK
        val charBell: Char = 7 // Bell character
        s"This is ${charAck}a simple ${charBell}string".cleanOutControlChar.trimAndNormalizeString shouldBe "This is  a simple  string".trimAndNormalizeString
      }
    }

    "resourceUniqueTableName" should {
      "create a unique table name" in {
        val res: String = testRegex.resourceUniqueTableName(resourceJDBC)
        res shouldEqual "random_3"
      }
    }

  }
}
