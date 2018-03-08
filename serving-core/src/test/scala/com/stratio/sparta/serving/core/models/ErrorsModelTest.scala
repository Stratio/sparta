/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class ErrorsModelTest extends WordSpec with Matchers {

  val error = new ErrorModel(100, "100", "Error 100", None, None)

  "ErrorModel" should {

    "toString method should return the number of the error and the error" in {
      val res = ErrorModel.toString(error)
      res should be ("""{"statusCode":100,"errorCode":"100","message":"Error 100"}""")
    }

    "toError method should return the number of the error and the error" in {
      val res = ErrorModel.toErrorModel(
        """
          |{
          | "statusCode": 100,
          | "errorCode": "100",
          | "message": "Error 100"
          |}
        """.stripMargin)
      res should be (error)
    }
  }
}
