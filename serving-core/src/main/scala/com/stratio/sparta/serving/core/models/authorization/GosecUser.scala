/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.authorization

import akka.event.slf4j.SLF4JLogging
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import scala.collection.JavaConverters._
import scala.util.{Success, Try}


object GosecUser extends SLF4JLogging{

  implicit def jsonToDto(stringJson: String): Option[GosecUser] = {
    if (stringJson.trim.isEmpty) None
    else {
      implicit val json = new ObjectMapper().readTree(stringJson)
      Some(
        GosecUser(
          getValue(GosecUserConstants.InfoIdTag),
          getValue(GosecUserConstants.InfoNameTag),
          getValue(GosecUserConstants.InfoMailTag, Some(GosecUserConstants.DummyMail)),
          getValue(GosecUserConstants.InfoGroupIDTag),
          getArrayValues(GosecUserConstants.InfoGroupsTag),
          getArrayValues(GosecUserConstants.InfoRolesTag),
          get(GosecUserConstants.TenantTag)
        )
      )
    }
  }

  private def get(tag: String)(implicit json: JsonNode) : Option[String] =
    Option(json.findValue(tag)).flatMap(jsonValue => Try(jsonValue.asText()).toOption)


  private def getValue(tag: String, defaultElse: Option[String]= None)(implicit json: JsonNode) : String = {
    Option(json.findValue(tag)) match {
      case Some(jsonValue) =>
        Try(jsonValue.asText()).orElse(Try(defaultElse.get)).get
      case None =>
        defaultElse.getOrElse("")
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

case class GosecUser(override val id: String, override val name: String, email: String, override val gid: String,
                     groups: Seq[String], roles: Seq[String], tenant: Option[String] = None) extends LoggedUser {

  def isAuthorized(securityEnabled: Boolean, allowedRoles: Seq[String] = GosecUserConstants.AllowedRoles): Boolean = {

    def rolesWithSpartaPrefix : Boolean = roles.exists(roleName => roleName.startsWith("sparta"))
    def intersectionRoles: Boolean = roles.intersect(allowedRoles).nonEmpty

    (roles, securityEnabled) match {
      case (rolesNotEmpty, true) if rolesNotEmpty.nonEmpty => rolesWithSpartaPrefix || intersectionRoles
      case (rolesEmpty, _) if rolesEmpty.isEmpty => false
      case (_, false) => true
    }
  }
}
