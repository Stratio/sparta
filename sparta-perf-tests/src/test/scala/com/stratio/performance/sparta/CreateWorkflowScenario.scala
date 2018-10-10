/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.performance.sparta


import com.stratio.performance.common.Common
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.config.HttpProtocolBuilder
import com.stratio.performance.dsl._

class CreateWorkflowScenario extends Simulation with Common {

  val httpProtocolBuilder: HttpProtocolBuilder = http
    .baseURL(baseURL)
    .acceptHeader(acceptHeader)
    .acceptCharsetHeader(acceptCharsetHeader)
    .acceptEncodingHeader(acceptEncodingHeader)
    .contentTypeHeader(contentTypeHeader)

  val scenarioBuilder: ScenarioBuilder = scenario(scenarioName)
    .exec(
      http(scenarioName)
        .post(paths.workflow)
        .body{
          StringBody{ session =>
            workflow toJson
          }
        }.check(status.is(checkStatusPOST)))

  setUp(
    scenarioBuilder.inject(rampUsersPerSec(1) to 10 during 10)
  ).protocols(httpProtocolBuilder)

}