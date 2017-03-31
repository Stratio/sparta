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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode

import scala.collection.JavaConverters._
import scala.util.Try


object LoggedUser{

  implicit def jsonToDto(stringJson: String): LoggedUser = {
    if (stringJson.trim.nonEmpty) {
      implicit val json = new ObjectMapper().readTree(stringJson)
      LoggedUser(getValue(LoggedUserConstant.infoIdTag), getValue(LoggedUserConstant.infoNameTag),
        getValue(LoggedUserConstant.infoMailTag, Some(LoggedUserConstant.dummyMail)),
        getValue(LoggedUserConstant.infoGroupIDTag), getArrayValues(LoggedUserConstant.infoGroupsTag),
        getArrayValues(LoggedUserConstant.infoRolesTag))
    }
    else LoggedUserConstant.AnonymousUser
  }

  private def getValue(tag: String, defaultElse: Option[String]= None)(implicit json: JsonNode) : String = {
    defaultElse match {
      case Some(value) => Try(json.findValue(tag).asText()).getOrElse(value)
      case None => Try(json.findValue(tag).asText()).get
    }
  }

  private def getArrayValues(tag:String)(implicit jsonNode: JsonNode): Seq[String] = {
    jsonNode.findValue(tag) match {
      case roles: ArrayNode => roles.asScala.map(x => x.asText()).toSeq
      case _ => Seq.empty[String]
    }
  }
}

case class LoggedUser(id: String, name: String, email: String, gid: String,
                      groups:Seq[String], roles: Seq[String]){

  def isAuthorized(securityEnabled: Boolean, allowedRoles: Seq[String] = LoggedUserConstant.allowedRoles): Boolean = {
    if(securityEnabled){
      roles.intersect(allowedRoles).nonEmpty
    } else true
  }

}
