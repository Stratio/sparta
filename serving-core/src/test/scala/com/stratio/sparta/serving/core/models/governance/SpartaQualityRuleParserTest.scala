/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.governance

import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.serving.core.models.SpartaSerializer
import org.json4s.native.Serialization.{read, write}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class SpartaQualityRuleParserTest extends FlatSpec
  with ShouldMatchers
  with SpartaSerializer {

  it should "parse a GovernanceQualityRule as input to a QualityRule as output" in {
    import com.stratio.sparta.serving.core.models.governance.QualityRuleParser._
    val governanceQualityRule = read[GovernanceQualityRule](GovernanceQualityRuleJson)
    val expectedSpartaQualityRule = read[Seq[SpartaQualityRule]](SpartaQualityRuleJson)

    governanceQualityRule.parse("stepName","outputName") should be (expectedSpartaQualityRule)
  }

  // TODO nistal move it to object when we will get a definitive structure from governance
  val GovernanceQualityRuleJson =
    """{
      |"content": [
      |    {
      |      "id": 1,
      |      "metadataPath": "postgreseos://postgreseos>/:writepeople:",
      |      "name": "Quality Rule testing",
      |      "description": "Quality Rule testing Quality Rule testing Quality Rule testing Quality Rule testing",
      |      "type": "SPARK",
      |      "catalogAttributeType": "RESOURCE",
      |      "parameters": {
      |        "filter": {
      |          "cond": [
      |            {
      |              "order": 1,
      |              "param": [
      |                "20"
      |              ],
      |              "attribute": "age",
      |              "operation": ">="
      |            },
      |            {
      |              "order": 2,
      |              "attribute": "yearBirthday",
      |              "param": [
      |                "19%"
      |              ],
      |              "operation": "like"
      |            },
      |            {
      |              "order": 3,
      |              "attribute": "gender",
      |              "param": [
      |                "Female"
      |              ],
      |              "operation": "in"
      |            }
      |          ],
      |          "type": "and",
      |          "order": 1
      |        },
      |        "catalogAttributeType": "RESOURCE"
      |      },
      |      "query": null,
      |      "active": true,
      |      "resultUnit": 12,
      |      "resultOperation": "=",
      |      "resultOperationType": "abs",
      |      "resultAction": {
      |        "type": "ACT_PASS"
      |      },
      |      "resultExecute": {
      |        "type": "EXE_REA"
      |      },
      |      "link": {
      |        "dashboards": []
      |      },
      |      "tenant": "NONE",
      |      "createdAt": "2019-03-19T15:51:37.639Z",
      |      "modifiedAt": "2019-03-19T15:51:37.639Z"
      |    }
      |  ],
      |  "pageable": {
      |    "sort": {
      |      "sorted": false,
      |      "unsorted": true
      |    },
      |    "pageSize": 20,
      |    "pageNumber": 0,
      |    "offset": 0,
      |    "unpaged": false,
      |    "paged": true
      |  },
      |  "totalElements": 1,
      |  "totalPages": 1,
      |  "last": true,
      |  "first": true,
      |  "sort": {
      |    "sorted": false,
      |    "unsorted": true
      |  },
      |  "numberOfElements": 1,
      |  "size": 20,
      |  "number": 0
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
      |      "outputName":"outputName"
      |   }
      |]""".stripMargin

}
