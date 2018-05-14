/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class EnvironmentCleanerUtilsTest extends WordSpecLike
  with Matchers
  with MockitoSugar {

  "The Environment Cleaner Actor" when {
    "passing a JSON describing the path /sparta/tenantName/workflows" should {
      "be able to retrieve all empty groups" in {
        val actualResult = MarathonAPIUtils.parseFindingEmpty(lotsOfEmptyGroups)
        val expectedResult = Seq("/sparta/sparta-fl/workflows/home/kafka-elastic",
          "/sparta/sparta-fl/workflows/home/kafka-elastic-tickets-p10",
          "/sparta/sparta-fl/workflows/home/workflow-name",
          "/sparta/sparta-fl/workflows/home/a/kafka-elastic-tickets-p10",
          "/sparta/sparta-fl/workflows/home/a",
          "/sparta/sparta-fl/workflows/home")
        //assertResult(expectedResult)(actualResult)
        assert(actualResult.forall(res => expectedResult.contains(res)))
      }

      "be able to retrieve only the empty groups" in {
        val actualResult = MarathonAPIUtils.parseFindingEmpty(someEmptySomeFull)
        val expectedResult = Seq("/sparta/eventra-test/workflows/home/workflow-jc")
        assertResult(expectedResult)(actualResult)
        assert(actualResult.forall(res => expectedResult.contains(res)))
      }

      "be able to retrieve also nested empty groups" in {
        val actualResult = MarathonAPIUtils.parseFindingEmpty(deeplyNestedEmpty)
        val expectedResult = Seq("/sparta/sparta-server/workflows/home/a/a2",
          "/sparta/sparta-server/workflows/home/a/a3/a4",
          "/sparta/sparta-server/workflows/home/a/a3",
          "/sparta/sparta-server/workflows/home/a",
          "/sparta/sparta-server/workflows/home")
        //assertResult(expectedResult)(actualResult)
        assert(actualResult.forall(res => expectedResult.contains(res)))
      }
    }


    "an empty JSON is passed" should {
      "return an empty DCOSgroup sequence" in {
        val actualResult = MarathonAPIUtils.parseFindingEmpty(emptyDCOSResponse)
        val expectedResult = Seq.empty
        assertResult(expectedResult)(actualResult)
      }
    }

    "a JSON with no empty groups is passed" should {
      "return an empty DCOSgroup sequence" in {
        val actualResult = MarathonAPIUtils.parseFindingEmpty(allFull)
        val expectedResult = Seq.empty
        assertResult(expectedResult)(actualResult)
      }
    }
  }

  val emptyDCOSResponse = ""

  val lotsOfEmptyGroups =
    """
      |{
      |  "id": "/sparta/sparta-fl/workflows",
      |  "dependencies": [],
      |  "version": "2018-03-02T13:01:17.371Z",
      |  "apps": [],
      |  "groups": [{
      |    "id": "/sparta/sparta-fl/workflows/home",
      |    "dependencies": [],
      |    "version": "2018-03-02T13:01:17.371Z",
      |    "apps": [],
      |    "groups": [{
      |      "id": "/sparta/sparta-fl/workflows/home/a",
      |      "dependencies": [],
      |      "version": "2018-03-02T13:01:17.371Z",
      |      "apps": [],
      |      "groups": [{
      |        "id": "/sparta/sparta-fl/workflows/home/a/kafka-elastic-tickets-p10",
      |        "dependencies": [],
      |        "version": "2018-03-02T13:01:17.371Z",
      |        "apps": [],
      |        "groups": []
      |      }]
      |    }, {
      |      "id": "/sparta/sparta-fl/workflows/home/kafka-elastic",
      |      "dependencies": [],
      |      "version": "2018-03-02T13:01:17.371Z",
      |      "apps": [],
      |      "groups": []
      |    }, {
      |      "id": "/sparta/sparta-fl/workflows/home/kafka-elastic-tickets-p10",
      |      "dependencies": [],
      |      "version": "2018-03-02T13:01:17.371Z",
      |      "apps": [],
      |      "groups": []
      |    }, {
      |      "id": "/sparta/sparta-fl/workflows/home/workflow-name",
      |      "dependencies": [],
      |      "version": "2018-03-02T13:01:17.371Z",
      |      "apps": [],
      |      "groups": []
      |    }]
      |  }]
      |}
    """.stripMargin

  val someEmptySomeFull =
    """
      |{
      |  "id": "/sparta/eventra-test/workflows",
      |  "dependencies": [],
      |  "version": "2018-04-06T09:40:03.363Z",
      |  "apps": [],
      |  "groups": [{
      |    "id": "/sparta/eventra-test/workflows/home",
      |    "dependencies": [],
      |    "version": "2018-04-06T09:40:03.363Z",
      |    "apps": [],
      |    "groups": [{
      |      "id": "/sparta/eventra-test/workflows/home/eventra-hdfs",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [{
      |        "id": "/sparta/eventra-test/workflows/home/eventra-hdfs/eventra-hdfs-v0"
      |      }],
      |      "groups": []
      |    }, {
      |      "id": "/sparta/eventra-test/workflows/home/eventra-journal-update",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [{
      |        "id": "/sparta/eventra-test/workflows/home/eventra-journal-update/eventra-journal-update-v0"
      |      }],
      |      "groups": []
      |    }, {
      |      "id": "/sparta/eventra-test/workflows/home/workflow-jc",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [],
      |      "groups": []
      |    }]
      |  }]
      |}
    """.stripMargin


  val allFull =
    """
      |{
      |  "id": "/sparta/eventra-test/workflows",
      |  "dependencies": [],
      |  "version": "2018-04-06T09:40:03.363Z",
      |  "apps": [],
      |  "groups": [{
      |    "id": "/sparta/eventra-test/workflows/home",
      |    "dependencies": [],
      |    "version": "2018-04-06T09:40:03.363Z",
      |    "apps": [],
      |    "groups": [{
      |      "id": "/sparta/eventra-test/workflows/home/eventra-hdfs",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [{
      |        "id": "/sparta/eventra-test/workflows/home/eventra-hdfs/eventra-hdfs-v0"
      |      }],
      |      "groups": []
      |    }, {
      |      "id": "/sparta/eventra-test/workflows/home/eventra-journal-update",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [{
      |        "id": "/sparta/eventra-test/workflows/home/eventra-journal-update/eventra-journal-update-v0"
      |      }],
      |      "groups": []
      |    }, {
      |      "id": "/sparta/eventra-test/workflows/home/workflow-jc",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [{
      |      "id": "/sparta/eventra-test/workflows/home/workflow-jc/workflow-jc-v0"
      |      }],
      |      "groups": []
      |    }]
      |  }]
      |}
    """.stripMargin


  val deeplyNestedEmpty =
    """
      |{
      |  "id": "/sparta/sparta-server/workflows",
      |  "dependencies": [],
      |  "version": "2018-04-06T09:40:03.363Z",
      |  "apps": [],
      |  "groups": [{
      |    "id": "/sparta/sparta-server/workflows/home",
      |    "dependencies": [],
      |    "version": "2018-04-06T09:40:03.363Z",
      |    "apps": [],
      |    "groups": [{
      |      "id": "/sparta/sparta-server/workflows/home/a",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [],
      |      "groups": [{
      |         "id": "/sparta/sparta-server/workflows/home/a/a2",
      |         "dependencies": [],
      |         "version": "2018-04-06T09:40:03.363Z",
      |         "apps": [],
      |         "groups": []
      |      },
      |      {
      |         "id": "/sparta/sparta-server/workflows/home/a/a3",
      |         "dependencies": [],
      |         "version": "2018-04-06T09:40:03.363Z",
      |         "apps": [],
      |         "groups": [{
      |           "id": "/sparta/sparta-server/workflows/home/a/a3/a4",
      |           "dependencies": [],
      |           "version": "2018-04-06T09:40:03.363Z",
      |           "apps": [],
      |           "groups": []
      |           }]
      |      }]
      |    }]
      |    }, {
      |      "id": "/sparta/sparta-server/workflows/home/eventra-journal-update",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [{
      |        "id": "/sparta/sparta-server/workflows/home/eventra-journal-update/eventra-journal-update-v0"
      |      }],
      |      "groups": []
      |    }, {
      |      "id": "/sparta/sparta-server/workflows/home/workflow-jc",
      |      "dependencies": [],
      |      "version": "2018-04-06T09:40:03.363Z",
      |      "apps": [{
      |      "id": "/sparta/sparta-server/workflows/home/workflow-jc/workflow-jc-v0"
      |      }],
      |      "groups": []
      |    }]
      |  }]
      |}
    """.stripMargin


}
