/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import akka.stream.ActorMaterializer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MarathonApiUtilsTest extends BaseUtilsTest {

  val materializer = ActorMaterializer()
  val testMarathonApiUtils = new MarathonAPIUtils(system, materializer)

  "MarathonAPIUtils.extractWorkflowDeploymentsFromMarathonResponse" when {
    "Given a deployments response" should {
      "parse to map of appId -> deploymentId" in {
        val jsonDeployments="""[
                              |  {
                              |    "id": "19810f2f-2707-4088-b6fb-d464b8e40ec5",
                              |    "version": "2019-02-04T11:06:11.612Z",
                              |    "affectedApps": [
                              |      "/sparta/sparta-server/workflows/home/workflow-name/workflow-name-v0/12eec99a-bc68-48ef-aaa3-d5fed554423f",
                              |      "/sparta/sparta-server/workflows/home/workflow-name/workflow-name-st-v0/12eec99a-bc68-48ef-aaa3-d5fed554423d"
                              |    ],
                              |    "steps": [
                              |      {
                              |        "actions": [
                              |          {
                              |            "action": "StartApplication",
                              |            "app": "/sparta/sparta25011806/workflows/home/workflow-name/workflow-name-v0/12eec99a-bc68-48ef-aaa3-d5fed554423f"
                              |          }
                              |        ]
                              |      },
                              |      {
                              |        "actions": [
                              |          {
                              |            "action": "ScaleApplication",
                              |            "app": "/sparta/sparta25011806/workflows/home/workflow-name/workflow-name-v0/12eec99a-bc68-48ef-aaa3-d5fed554423f"
                              |          }
                              |        ]
                              |      }
                              |    ],
                              |    "currentActions": [
                              |      {
                              |        "action": "ScaleApplication",
                              |        "app": "/sparta/sparta25011806/workflows/home/workflow-name/workflow-name-v0/12eec99a-bc68-48ef-aaa3-d5fed554423f",
                              |        "readinessCheckResults": []
                              |      }
                              |    ],
                              |    "currentStep": 2,
                              |    "totalSteps": 2
                              |  }
                              |]""".stripMargin

        val result = testMarathonApiUtils.extractDeploymentsFromMarathonResponse(jsonDeployments)

        result should not be empty
        result should be equals Map(
          "/sparta/sparta-server/workflows/home/workflow-name/workflow-name-v0/12eec99a-bc68-48ef-aaa3-d5fed554423f" -> "19810f2f-2707-4088-b6fb-d464b8e40ec5",
          "/sparta/sparta-server/workflows/home/workflow-name/workflow-name-st-v0/12eec99a-bc68-48ef-aaa3-d5fed554423d" -> "19810f2f-2707-4088-b6fb-d464b8e40ec5"
        )
      }

      "parse to empty map a invalid reponse" in {
        val jsonDeployments="""wrong""".stripMargin

        val result = testMarathonApiUtils.extractDeploymentsFromMarathonResponse(jsonDeployments)

        result should be equals Map.empty
      }
    }
  }
}
