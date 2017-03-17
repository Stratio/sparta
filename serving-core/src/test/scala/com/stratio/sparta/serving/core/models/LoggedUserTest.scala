/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.serving.core.models

import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class LoggedUserTest extends WordSpec with Matchers {

  val dummyGroupID = "66"

  "An input String" when {
    "containing a well-formed JSON" should {
      "be correctly transformed into a LoggedUser" in {
        val objectUser = LoggedUser("1234-qwerty", "user1",
          LoggedUserConstant.dummyMail, dummyGroupID, Seq.empty[String], Seq("admin"))
        val stringJson =
          """
        {"id":"1234-qwerty",
        "attributes":[
          {"cn":"user1"},
          {"mail":"email@email.com"},
          {"gidNumber":"66"},
          {"groups":[]},
          {"roles":["admin"]}
        ]}"""

        val parsedUser = LoggedUser.jsonToDto(stringJson)
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
        val parsedUser = LoggedUser.jsonToDto(stringSparta)
        val objectUser = LoggedUser("sparta", "sparta",
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
        val parsedUser = LoggedUser.jsonToDto(stringJson)
        parsedUser shouldBe None
      }
    }
  }

  "A user" when {
    "Oauth2 security is enabled" should {
      "be authorized only if one of its roles is contained inside allowedRoles" in {
        val objectUser = LoggedUser("1234-qwerty", "user1",
          LoggedUserConstant.dummyMail, dummyGroupID, Seq.empty[String], Seq("admin"))
        objectUser.isAuthorized(securityEnabled = true, allowedRoles = Seq("admin")) === true &&
          objectUser.isAuthorized(securityEnabled = true,
            allowedRoles = Seq("OtherAdministratorRole", "dummyUser")) === false
      }
    }
  }

  "A user" when {
    "Oauth2 security is disabled" should {
      "always be authorized" in {
        val objectUser = LoggedUser("1234-qwerty", "user1",
          LoggedUserConstant.dummyMail, dummyGroupID, Seq.empty[String], Seq("admin"))
        objectUser.isAuthorized(securityEnabled = false, allowedRoles = LoggedUserConstant.allowedRoles) === true
      }
    }
  }

}
