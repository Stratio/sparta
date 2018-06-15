/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.gosec.dyplon.plugins.sparta.sparta

import com.stratio.gosec.dyplon.plugins.sparta.SpartaToGoSecConversions._
import com.stratio.sparta.security.{SuccessAR, Upload => SpartaUpload, Create => SpartaCreate, Delete => SpartaDelete, Download => SpartaDownload, Edit => SpartaEdit, Status => SpartaStatus, View => SpartaView, Describe => SpartaDescribe}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SpartaImplicitsConversionsTest extends FlatSpec with Matchers {

  behavior of "SpartaToGoSecConversions"

  it should "SpartaRead must be converted to GoSec Read" in {
    val expectedResult = com.stratio.gosec.dyplon.model.View
    val result = actionConversion(SpartaView)

    result shouldBe expectedResult
  }

  it should "SpartaCreate must be converted to GoSec Create" in {
    val expectedResult = com.stratio.gosec.dyplon.model.Create
    val result = actionConversion(SpartaCreate)

    result shouldBe expectedResult
  }

  it should "SpartaDelete must be converted to GoSec Delete" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Delete
    val result = actionConversion(SpartaDelete)

    result shouldBe expectedResult
  }

  it should "SpartaEdit must be converted to GoSec Edit" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Edit
    val result = actionConversion(SpartaEdit)

    result shouldBe expectedResult
  }

  it should "SpartaStatus must be converted to GoSec Status" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Status
    val result = actionConversion(SpartaStatus)

    result shouldBe expectedResult
  }

  it should "SpartaDownload must be converted to GoSec Download" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Download
    val result = actionConversion(SpartaDownload)

    result shouldBe expectedResult
  }

  it should "AuditResult must be converted to GoSec Result" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Success
    val result = resultConversion(SuccessAR)

    result shouldBe expectedResult
  }

  it should "SpartaUpload must be converted to GoSec Upload" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Upload
    val result = actionConversion(SpartaUpload)

    result shouldBe expectedResult
  }

  it should "SpartaDescribe must be converted to GoSec Describe" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Describe
    val result = actionConversion(SpartaDescribe)

    result shouldBe expectedResult
  }

  it should "SpartaExecute must be converted to GoSec Execute" in {

    val expectedResult = com.stratio.gosec.dyplon.model.Status
    val result = actionConversion(SpartaStatus)

    result shouldBe expectedResult
  }
}

