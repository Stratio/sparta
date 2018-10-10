/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.performance.common

trait Common {

  val baseURL: String = sys.env.getOrElse("BASE_URL", "http://localhost:9090/")

  val cookie: String = System.getProperty("COOKIE", "user=c7933bce-1e32-4289-b50b-bfe8182eb2c8")
  val checkStatus: Int = System.getProperty("CHECK_STATUS", "200").toInt
  val checkStatusGET: Int = System.getProperty("CHECK_STATUS_GET", "200").toInt
  val checkStatusPOST: Int = System.getProperty("CHECK_STATUS_POST", "200").toInt
  val checkStatusPATCH: Int = System.getProperty("CHECK_STATUS_PATCH", "200").toInt
  val acceptHeader: String = System.getProperty("ACCEPT_HEADER", "application/json")
  val acceptCharsetHeader: String = System.getProperty("ACCEPT_CHARSET_HEADER", "UTF-8")
  val acceptEncodingHeader: String = System.getProperty("ACCEPT_ENCODING_HEADER", "gzip")
  val contentTypeHeader: String = System.getProperty("CONTENT_TYPE_HEADER", "application/json")
  val durationInterval: Int = System.getProperty("DURATION_INTERVAL", "600").toInt

  val scenarioName: String = "Scenario " + this.getClass.getSimpleName

  object paths {
    val workflow = "workflows"
  }

}
