/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
      case Some(_) => Seq.empty[String]
      case None => Seq.empty[String]
    }
  }
}

case class LoggedUser(id: String, name: String, email: String, gid: String,
                      groups:Seq[String], roles: Seq[String]){

  def isAuthorized(securityEnabled: Boolean, allowedRoles: Seq[String] = LoggedUserConstant.allowedRoles): Boolean = {

    def rolesWithSpartaPrefix : Boolean = roles.exists(roleName => roleName.startsWith("sparta"))
    def intersectionRoles: Boolean = roles.intersect(allowedRoles).nonEmpty

    (roles, securityEnabled) match {
      case (rolesNotEmpty, true) if rolesNotEmpty.nonEmpty => rolesWithSpartaPrefix || intersectionRoles
      case (rolesEmpty, _) if rolesEmpty.isEmpty => false
      case (_, false) => true
    }
  }
}
