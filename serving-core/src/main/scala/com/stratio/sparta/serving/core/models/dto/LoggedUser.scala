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

import akka.event.slf4j.SLF4JLogging
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode

import scala.collection.JavaConverters._
import scala.util.Try


object LoggedUser extends SLF4JLogging{

  implicit def jsonToDto(stringJson: String): Option[LoggedUser] = {
    if (stringJson.trim.isEmpty) None
    else {
      implicit val json = new ObjectMapper().readTree(stringJson)
      Some(LoggedUser(getValue(LoggedUserConstant.infoIdTag), getValue(LoggedUserConstant.infoNameTag),
        getValue(LoggedUserConstant.infoMailTag, Some(LoggedUserConstant.dummyMail)),
        getValue(LoggedUserConstant.infoGroupIDTag), getArrayValues(LoggedUserConstant.infoGroupsTag),
        getArrayValues(LoggedUserConstant.infoRolesTag)))
    }
  }

  private def getValue(tag: String, defaultElse: Option[String]= None)(implicit json: JsonNode) : String = {
    Option(json.findValue(tag)) match {
      case Some(jsonValue) =>
        defaultElse match{
          case Some(value) => Try(jsonValue.asText()).getOrElse(value)
          case None => Try(jsonValue.asText()).get
        }
      case None =>
        defaultElse match {
          case Some(value) => value
          case None => ""
        }
    }
  }

  private def getArrayValues(tag:String)(implicit jsonNode: JsonNode): Seq[String] = {
    Option(jsonNode.findValue(tag)) match {
      case Some(roles: ArrayNode) => roles.asScala.map(x => x.asText()).toSeq
      case None => Seq.empty[String]
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
