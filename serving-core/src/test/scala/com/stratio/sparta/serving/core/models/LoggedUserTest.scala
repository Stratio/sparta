/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models

import com.stratio.sparta.serving.core.models.authorization.{GosecUser, GosecUserConstants}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class LoggedUserTest extends WordSpec with Matchers {

  val dummyGroupID = "66"

  "An input String" when {
    "containing a well-formed JSON" should {
      "be correctly transformed into a LoggedUser" in {
        val objectUser = GosecUser("1234-qwerty", "user1",
          GosecUserConstants.DummyMail, dummyGroupID, Seq.empty[String], Seq("admin"), Some("NONE"))
        val stringJson =
          """
        {"id":"1234-qwerty",
        "attributes":[
          {"cn":"user1"},
          {"mail":"email@email.com"},
          {"gidNumber":"66"},
          {"groups":[]},
          {"roles":["admin"]},
          {"tenant": "NONE"}
        ]}"""

        val parsedUser = GosecUser.jsonToDto(stringJson)
        parsedUser shouldBe defined
        parsedUser.get should equal(objectUser)
      }
    }
  }


  "An input String" when {
    "has missing fields" should {
      "be correctly parsed " in {
        val stringSparta =
          """{"id":"sparta","attributes":[
          |{"cn":"sparta"},
          |{"mail":"sparta@demo.stratio.com"},
          |{"groups":["Developers"]},
          |{"roles":[]}]}""".stripMargin
        val parsedUser = GosecUser.jsonToDto(stringSparta)
        val objectUser = GosecUser("sparta", "sparta",
          "sparta@demo.stratio.com", "", Seq("Developers"), Seq.empty[String])
        parsedUser shouldBe defined
        parsedUser.get should equal (objectUser)
      }
    }
  }


  "An input String" when {
    "is empty" should {
      "be transformed into None" in {
        val stringJson = ""
        val parsedUser = GosecUser.jsonToDto(stringJson)
        parsedUser shouldBe None
      }
    }
  }

  "A user" when {
    "Oauth2 security is enabled" should {
      "be authorized only if one of its roles is contained inside allowedRoles" in {
        val objectUser = GosecUser("1234-qwerty", "user1",
          GosecUserConstants.DummyMail, dummyGroupID, Seq.empty[String], Seq("admin"))
        objectUser.isAuthorized(securityEnabled = true, allowedRoles = Seq("admin")) === true &&
          objectUser.isAuthorized(securityEnabled = true,
            allowedRoles = Seq("OtherAdministratorRole", "dummyUser")) === false
      }
    }
  }

  "A user" when {
    "Oauth2 security is disabled" should {
      "always be authorized" in {
        val objectUser = GosecUser("1234-qwerty", "user1",
          GosecUserConstants.DummyMail, dummyGroupID, Seq.empty[String], Seq("admin"))
        objectUser.isAuthorized(securityEnabled = false, allowedRoles = GosecUserConstants.AllowedRoles) === true
      }
    }
  }

}
