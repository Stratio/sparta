/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.governance

import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum._
import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.core.utils.QualityRulesUtils._
import com.stratio.sparta.core.utils.RegexUtils._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import org.json4s.native.Serialization.read
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.io.BufferedSource

@RunWith(classOf[JUnitRunner])
class SpartaQualityRuleParserTest extends FlatSpec
  with ShouldMatchers
  with SpartaSerializer {

  import SpartaQualityRule._
  import SpartaQualityRuleParserTest._

  it should "parse a GovernanceQualityRule as input to a QualityRule as output" in {
    import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._
    val governanceQualityRule: GovernanceQualityRule = read[GovernanceQualityRule](GovernanceQualityRuleJson)

    val actualSpartaQualityRule: Seq[SpartaQualityRule] = governanceQualityRule.parse()

    val rules: Seq[Cond] = governanceQualityRule.content.head.parameters.filter.get.cond
    rules should have size 1


    actualSpartaQualityRule.seq.head.predicates should have size 1
  }

  it should "parse a Sequence of mixed GovernanceQualityRules" in {
    import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._

    val governanceQualityRule: GovernanceQualityRule = read[GovernanceQualityRule](inputMixed)

    val actualValidSpartaQualityRule: Seq[SpartaQualityRule] = governanceQualityRule.parse()

    actualValidSpartaQualityRule.filter( qr => qr.validSpartaQR.isSuccess) should have size governanceQualityRule.numberOfElements
  }

  it should "parse a Sequence of planned GovernanceQualityRules" in {
    import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._

    val governanceQualityRule: GovernanceQualityRule = read[GovernanceQualityRule](inputPlanned)

    val actualValidSpartaQualityRule: Seq[SpartaQualityRule] = governanceQualityRule.parse()

    actualValidSpartaQualityRule.filter{sqr => sqr.qualityRuleType == Planned } should have size governanceQualityRule.numberOfElements
  }

  it should "extract resources" in {
    val readQR = read[SpartaQualityRule](testQRPostgres)
    val query = extractQueriesFromPlannedQRResources(readQR, Map.empty[String, String])
    println(query.head)
    val strName = readQR.retrieveQueryForInputStepForPlannedQR.cleanOutControlChar.trimAndNormalizeString
    println(strName)
    val resources = extractResources(readQR)
    resources should have size 1
    query should not be empty
  }
}


object SpartaQualityRuleParserTest{

  lazy val resourcePathMixed = getClass().getResource("/allQR.json").getFile
  lazy val sourceMixed: BufferedSource = scala.io.Source.fromFile(resourcePathMixed)
  lazy val inputMixed: String = try sourceMixed.mkString finally sourceMixed.close()

  lazy val resourcePathPlanned = getClass().getResource("/plannedQR.json").getFile
  lazy val sourcePlanned : BufferedSource = scala.io.Source.fromFile(resourcePathPlanned)
  lazy val inputPlanned : String = try sourcePlanned.mkString finally sourcePlanned.close()

  // TODO nistal move it to object when we will get a definitive structure from governance
  val GovernanceQualityRuleJson =
    """{
      |  "content": [
      |    {
      |      "active": true,
      |      "catalogAttributeType": "RESOURCE",
      |      "createdAt": "2019-10-15T08:02:35.767Z",
      |      "description": "",
      |      "id": 92,
      |      "link": {
      |        "dashboards": [
      |        ]
      |      },
      |      "metadataPath": "tenantqa-hdfs-example://test/qr/Case>/:Case:",
      |      "modifiedAt": "2019-10-15T08:02:35.767Z",
      |      "name": "test-sparta",
      |      "parameters": {
      |        "catalogAttributeType": "RESOURCE",
      |        "filter": {
      |          "cond": [
      |            {
      |              "attribute": "id",
      |              "operation": ">",
      |              "order": 1,
      |              "param": [
      |                {
      |                  "name": "",
      |                  "value": "20"
      |                }
      |              ]
      |            }
      |          ],
      |          "order": 1,
      |          "type": "and"
      |        },
      |        "table": {
      |          "type": "HDFS"
      |        }
      |      },
      |      "qualityGenericId": null,
      |      "query": null,
      |      "resultAction": {
      |        "type": "ACT_PASS"
      |      },
      |      "resultExecute": {
      |        "config": {
      |          "executionOptions": {
      |            "size": "S"
      |          },
      |          "scheduling": [
      |            {
      |              "initialization": 1571126700000
      |            }
      |          ]
      |        },
      |        "type": "EXE_PRO"
      |      },
      |      "resultOperation": ">",
      |      "resultOperationType": "%",
      |      "resultUnit": {
      |        "name": "",
      |        "value": "40"
      |      },
      |      "tenant": "tenantqa",
      |      "type": "SPARK",
      |      "userId": "admin"
      |    }
      |  ],
      |  "first": true,
      |  "last": true,
      |  "number": 0,
      |  "numberOfElements": 1,
      |  "pageable": {
      |    "offset": 0,
      |    "pageNumber": 0,
      |    "pageSize": 200,
      |    "paged": true,
      |    "sort": {
      |      "sorted": false,
      |      "unsorted": true
      |    },
      |    "unpaged": false
      |  },
      |  "size": 200,
      |  "sort": {
      |    "sorted": false,
      |    "unsorted": true
      |  },
      |  "totalElements": 1,
      |  "totalPages": 1
      |}""".stripMargin

  val SpartaQualityRuleJson =
    """[
      |   {
      |      "id":1,
      |      "metadataPath":"postgreseos://postgreseos>/:writepeople:",
      |      "name":"Quality Rule testing",
      |      "qualityRuleScope":"RESOURCE",
      |      "logicalOperator":"and",
      |      "enable":true,
      |      "threshold":{
      |         "value":12.0,
      |         "operation":"=",
      |         "type":"abs",
      |         "actionType":{
      |            "type":"ACT_PASS"
      |         }
      |      },
      |      "predicates":[
      |         {
      |            "order":1,
      |            "operands":[
      |               "20"
      |            ],
      |            "field":"age",
      |            "operation":">="
      |         },
      |         {
      |            "order":2,
      |            "operands":[
      |               "19%"
      |            ],
      |            "field":"yearBirthday",
      |            "operation":"like"
      |         },
      |         {
      |            "order":3,
      |            "operands":[
      |               "Female"
      |            ],
      |            "field":"gender",
      |            "operation":"in"
      |         }
      |      ],
      |      "stepName":"stepName",
      |      "outputName":"outputName",
      |      "qualityRuleType" : "EXE_PRO"
      |   }
      |]""".stripMargin

  val testQRPostgres =
    """
      |{
      |  "id": 132,
      |  "metadataPath": "tenantqa-postgresqa.tenantqa://governance>/:governance.random:",
      |  "name": "test-simple-governance",
      |  "qualityRuleScope": "RESOURCE",
      |  "logicalOperator": "and",
      |  "metadataPathResourceType": "JDBC",
      |  "metadataPathResourceExtraParams": [
      |    {
      |      "key": "connectionURI",
      |      "value": "jdbc:postgresql://pg-0001.tenantqa-postgresqa.tenantqa:5432/"
      |    },
      |    {
      |      "key": "tlsEnabled",
      |      "value": "true"
      |    },
      |    {
      |      "key": "securityType",
      |      "value": "TLS"
      |    },
      |    {
      |      "key": "driver",
      |      "value": "org.postgresql->postgresql->42.1.0"
      |    },
      |    {
      |      "key": "vendor",
      |      "value": "postgres"
      |    }
      |  ],
      |  "enable": true,
      |  "threshold": {
      |    "value": 20,
      |    "operation": ">",
      |    "type": "%",
      |    "actionType": {
      |      "type": "ACT_PASS"
      |    }
      |  },
      |  "predicates": [
      |    {
      |      "order": 1,
      |      "operands": [
      |        "Mc%"
      |      ],
      |      "field": "name",
      |      "operation": "like"
      |    }
      |  ],
      |  "stepName": "plannedQR_input",
      |  "outputName": "metrics",
      |  "qualityRuleType": "Planned",
      |  "tenant": "tenantqa",
      |  "creationDate": 1571649731548,
      |  "modificationDate": 1571649731548,
      |  "schedulingDetails": {
      |    "initDate": 1571649900000,
      |    "sparkResourcesSize": "S"
      |  },
      |  "taskId": "198f3b93-ba3d-409c-8739-5e44a1ed1c45"
      |}
      |""".stripMargin


}
