/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin

import java.net.URL

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Suite}

case class WireMockBaseUrl(value: URL)

trait WireMockSupport extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach{
  me: Suite =>

  val wireMockPort: Int = "20000".toInt
  val wireMockHost = "localhost"
  val wireMockBaseUrlAsString = s"http://$wireMockHost:$wireMockPort"
  val wireMockBaseUrl = new URL(wireMockBaseUrlAsString)

  protected[plugin] val wireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

  override def beforeEach(){
    wireMockServer.start()
    WireMock.configureFor(wireMockHost, wireMockPort)
  }

  override def afterEach {
    wireMockServer.resetAll()
    wireMockServer.stop()
  }

}
