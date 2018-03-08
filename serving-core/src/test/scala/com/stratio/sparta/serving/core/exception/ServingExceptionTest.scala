/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.exception

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class ServingExceptionTest extends WordSpec with Matchers {

  "A ServingException" should {
    "create an exception with message" in {
      ServerException.create("message").getMessage should be("message")
    }
    "create an exception with message and a cause" in {
      val cause = new IllegalArgumentException("any exception")
      val exception = ServerException.create("message", cause)
      exception.getMessage should be("message")
      exception.getCause should be theSameInstanceAs(cause)
    }
  }
}
