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

package com.stratio.sparta.serving.core.models.dto

object LoggedUserConstant {
  val infoNameTag = "cn"
  val infoIdTag = "id"
  val infoMailTag = "mail"
  val infoRolesTag = "roles"
  val infoGroupIDTag= "gidNumber"
  val infoGroupsTag= "groups"

  val dummyMail = "email@email.com"

  val AnonymousUser = LoggedUser("*", "Anonymous", dummyMail,"0",Seq.empty[String],Seq.empty[String])

  val allowedRoles = Seq("FullAdministrator","management_admin", "sparta")
}
