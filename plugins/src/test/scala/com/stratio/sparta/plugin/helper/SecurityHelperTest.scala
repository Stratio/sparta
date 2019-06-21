/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.helper

import com.stratio.sparta.serving.core.constants.AppConstant
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class SecurityHelperTest extends WordSpec with Matchers {

  val urlWithUser = "jdbc:postgresql://dbserver:port/database?user=postgres"
  val urlWithoutUserAndParameters = "jdbc:postgresql://dbserver:port/database"
  val urlWithoutUserWithParameters = "jdbc:postgresql://dbserver:port/database?password=secret"
  val urlWithoutUserWithParametersEndingIncorrectly = "jdbc:postgresql://dbserver:port/database?password=secret&"
  val userTenant = AppConstant.spartaTenant

  "addUserToConnectionURI" should {

    "add user (the same as spartaTenant)" when {

      "the connectionURI does not contain user=VALUE but has connection parameters" in {
        val result = SecurityHelper.addUserToConnectionURI(userTenant, urlWithoutUserWithParameters)
        result should include (s"user=$userTenant")
      }

      "the connectionURI does not contain user=VALUE but has connection parameters ending with &" in {
        val result = SecurityHelper.addUserToConnectionURI(userTenant, urlWithoutUserWithParametersEndingIncorrectly)
        result should include (s"user=$userTenant")
        result should not endWith "&"
      }

      "the connectionURI does not contain user=VALUE and has no connection parameters" in {
        val result = SecurityHelper.addUserToConnectionURI(userTenant, urlWithoutUserAndParameters)
        result should include (s"?user=$userTenant")
      }
    }

    "not add user (the same as spartaTenant)" when {
      "the connectionURI does not contain user=VALUE but has connection parameters" in{
        val result = SecurityHelper.addUserToConnectionURI(userTenant, urlWithUser)
        result should not include (s"user=$userTenant")
      }
    }
  }
}

