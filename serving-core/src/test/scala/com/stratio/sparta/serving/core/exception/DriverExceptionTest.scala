/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.exception

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DriverExceptionTest extends FlatSpec with ShouldMatchers {

  "DriverException" should "return a Throwable" in {

    val msg = "my custom exception"

    val ex = DriverException(msg)

    ex.getMessage should be(msg)
  }
  it should "return a exception with the msg and a cause" in {

    val msg = "my custom exception"

    val ex = DriverException(msg, new RuntimeException("cause"))

    ex.getCause.getMessage should be("cause")
  }

}
