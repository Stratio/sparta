/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import com.stratio.sparta.core.enumerators.{QualityRuleResourceTypeEnum, QualityRuleTypeEnum}
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.utils.QualityRulesUtils._
import org.json4s.native.Serialization.read
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class QualityRulesUtilsTest extends WordSpec with Matchers {
  val testRegex = RegexUtils

  "QualityRulesUtils" when {

    val defaultThreshold: SpartaQualityRuleThreshold = SpartaQualityRuleThreshold(
      value = 3.0,
      operation = ">=",
      `type` = "abs",
      actionType = SpartaQualityRuleThresholdActionType(path = Some("error"), `type` = "ACT_PASS")
    )


    val seqQR_predicate = Seq(
      SpartaQualityRulePredicate(`type` = Some("aaa"), order = 1, operands = Seq("yellow"), field = "color", operation = "NOT lIKe"))

    val exampleQRComplexWithResources : SpartaQualityRule = SpartaQualityRule(id = 1,
      metadataPath = MetadataPath(
        service = "hdfs1",
        path = Some("/tmp/test"),
        resource = Some("filecsv")).toString,
      name = "",
      qualityRuleScope = "data",
      logicalOperator = None,
      metadataPathResourceType = Option(QualityRuleResourceTypeEnum.HDFS),
      metadataPathResourceExtraParams = Seq(PropertyKeyValue(SpartaQualityRule.qrConnectionURI, "jajajaj"), PropertyKeyValue("schema", "avro")),
      enable = true,
      threshold = defaultThreshold,
      predicates = seqQR_predicate,
      stepName = "tableA",
      outputName = "",
      qualityRuleType = QualityRuleTypeEnum.Planned,
      creationDate = Some(0L),
      modificationDate = Some(0L))


    "extractQueriesFromPlannedQRResources is called" in {

      val queries = extractQueriesFromPlannedQRResources(exampleQRComplexWithResources, Map.empty[String, String])
      queries should have size 1
      println(queries.head)
    }

    "extractResources is called" in {
      val resources = extractResources(exampleQRComplexWithResources)
      resources should have size 1
      val res = resources.head
      println(res)
    }

    "a Crossdata resource getSQL should import the collection" in {
      val qualityRuleXD: SpartaQualityRule =
        exampleQRComplexWithResources.copy(metadataPath = "collectionNameXD://collectionNameXD>/:tableName:fieldName:" , metadataPathResourceType = Option(QualityRuleResourceTypeEnum.XD))
      val query = QualityRulesUtils.extractQueriesFromPlannedQRResources(qualityRuleXD, Map.empty[String, String])
      query should have size 1
      query.head should equal ("REFRESH EXTENDED COLLECTION collectionNameXD")

    }
  }

}
