/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.governance

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.models.{PropertyKeyValue, ResourcePlannedQuery}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.governanceDataAsset.GovernanceDataAssetResponse
import org.json4s.native.Serialization.read
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class SpartaGovernanceAssetParserTest extends FlatSpec
  with ShouldMatchers
  with SpartaSerializer {
  import SpartaGovernanceAssetParserTest._

  it should "parse a Governance Resource of type HDFS Json" in {
    val governanceResponse: GovernanceDataAssetResponse = read[GovernanceDataAssetResponse](GovernanceResourceHDFSJson)

    governanceResponse.dataAssets.dataAssets should have size 1

    governanceResponse.getDataAssetType shouldBe Some("HDFS")

    val prop = governanceResponse.retrieveDetailsFromDatastore ++ governanceResponse.retrieveConnectionDetailsFromDatastore
    prop.foreach(k => println(s"${k.key}=${k.value}"))

    val sec = governanceResponse.dataAssets.dataAssets.head.properties.dataStore.map(ass => ass.dataSources.head.security.`type`)

    sec shouldBe Some("KRB")
  }

  it should "parse a Governance Resource of type Postgres Json" in {
    val governanceResponse: GovernanceDataAssetResponse = read[GovernanceDataAssetResponse](GovernanceResourcePostgresJson)

    val prop = governanceResponse.retrieveDetailsFromDatastore ++ governanceResponse.retrieveConnectionDetailsFromDatastore
    prop.foreach(k => println(s"${k.key}=${k.value}"))

    governanceResponse.dataAssets.dataAssets should have size 1
    val sec = governanceResponse.dataAssets.dataAssets.head.properties.dataStore.map(ass => ass.dataSources.head.security.`type`)
    sec shouldBe Some("TLS")
  }

  it should "parse a Governance Resource of an HDFS file" in {
    val governanceResponse: GovernanceDataAssetResponse = read[GovernanceDataAssetResponse](GovernanceResourceHDFSFileJson)

    val schema = governanceResponse.dataAssets.dataAssets.head.properties.hdfsFile.map(file => file.schema)

    schema shouldBe Some("parquet")
  }

  it should "retrieve the necessary resources of HDFS file" in {
    val governanceResponseAsset: GovernanceDataAssetResponse = read[GovernanceDataAssetResponse](GovernanceResourceHDFSJson)
    val governanceResponseFile: GovernanceDataAssetResponse = read[GovernanceDataAssetResponse](GovernanceResourceHDFSFileJson)

    val resourceAllDetails = governanceResponseAsset.retrieveConnectionDetailsFromDatastore ++ governanceResponseAsset.retrieveDetailsFromDatastore ++ governanceResponseFile.retrieveSchemaFromFile

    val resource = ResourcePlannedQuery(id = 1,
      metadataPath = "HDFS:/blabla",
      resource=  "hdfstable",
      typeResource = QualityRuleResourceTypeEnum.HDFS,
      listProperties= resourceAllDetails )

    ResourcePlannedQuery.allDetailsForSingleResource(resource) shouldEqual Success(true)

  }

}

object SpartaGovernanceAssetParserTest {

  val GovernanceResourceHDFSJson =
    """
      |{
      |  "dataAssets": {
      |    "dataAssets": [
      |      {
      |        "active": true,
      |        "discoveredAt": "1571152261214",
      |        "id": "3154",
      |        "metadataPath": "tenantqa-hdfs-example:",
      |        "modifiedAt": "1571152261214",
      |        "name": "tenantqa-hdfs-example",
      |        "properties": {
      |          "dataStore": {
      |            "dataSources": [
      |              {
      |                "driver": "org.apache->hadoop->2.7.2",
      |                "prefered": "true",
      |                "security": {
      |                  "type": "KRB"
      |                },
      |                "typeConfig": "FILE",
      |                "urls": [
      |                  {
      |                    "url": "http://api.tenantqa-hdfs-example.marathon.l4lb.thisdcos.directory/v1/config/krb5.conf"
      |                  },
      |                  {
      |                    "url": "http://api.tenantqa-hdfs-example.marathon.l4lb.thisdcos.directory/v1/config/hdfs-site.xml"
      |                  },
      |                  {
      |                    "url": "http://api.tenantqa-hdfs-example.marathon.l4lb.thisdcos.directory/v1/config/core-site.xml"
      |                  }
      |                ]
      |              }
      |            ],
      |            "security": "KRB",
      |            "url": "hdfs://tenantqa-hdfs-example",
      |            "version": "2.7.2"
      |          }
      |        },
      |        "subtype": "DS",
      |        "tenant": "tenantqa",
      |        "type": "HDFS",
      |        "userId": "default",
      |        "vendor": "postgres"
      |      }
      |    ]
      |  },
      |  "pageElementsSize": 1,
      |  "totalElements": "1",
      |  "totalPages": 1
      |}
      |""".stripMargin

  val GovernanceResourcePostgresJson =
    """
      |{
      |  "pageElementsSize": 1,
      |  "totalElements": "1",
      |  "totalPages": 1,
      |  "dataAssets": {
      |    "dataAssets": [
      |      {
      |        "id": "4455",
      |        "name": "postgrestest",
      |        "type": "SQL",
      |        "subtype": "DS",
      |        "tenant": "NONE",
      |        "properties": {
      |          "dataStore": {
      |            "version": "11",
      |            "url": "jdbc:postgresql://127.0.0.1:5432/-db-",
      |            "security": "TLS",
      |            "dataSources": [
      |              {
      |                "prefered": "true",
      |                "security": {
      |                  "type": "TLS",
      |                  "attributes": []
      |                },
      |                "typeConfig": "DS",
      |                "driver": "org.postgresql->postgresql->42.2.5",
      |                "urls": [
      |                  {
      |                    "url": "jdbc:postgresql://127.0.0.1:5432/-db-"
      |                  }
      |                ],
      |                "attributes": []
      |              }
      |            ]
      |          }
      |        },
      |        "metadataPath": "postgrestest:",
      |        "active": true,
      |        "vendor": "postgres",
      |        "userId": "default",
      |        "discoveredAt": "1569928146743",
      |        "modifiedAt": "1569928146743"
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin

  val GovernanceResourceHDFSFileJson =
    """
      |{
      |  "dataAssets": {
      |    "dataAssets": [
      |      {
      |        "active": true,
      |        "alias": "case",
      |        "discoveredAt": "1571061204708",
      |        "id": "2090",
      |        "metadataPath": "tenantqa-hdfs-example://test/qr/Case>/:Case:",
      |        "modifiedAt": "1571132329378",
      |        "name": "Case",
      |        "properties": {
      |          "hdfsFile": {
      |            "blockSize": "134217728",
      |            "group": "supergroup",
      |            "isEncrypted": "false",
      |            "length": "980",
      |            "modifiedAt": "1571061198450",
      |            "owner": "tenantqa-sparta-qr",
      |            "partition": "-",
      |            "permissions": "666",
      |            "replication": "3",
      |            "schema": "parquet"
      |          }
      |        },
      |        "subtype": "RESOURCE",
      |        "tenant": "tenantqa",
      |        "type": "HDFS",
      |        "userId": "admin",
      |        "vendor": "postgres"
      |      }
      |    ]
      |  },
      |  "pageElementsSize": 1,
      |  "totalElements": "1",
      |  "totalPages": 1
      |}
      |""".stripMargin

}