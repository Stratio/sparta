/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.dg.agent.models

case class MetadataPath(
                         service: String,
                         path: Option[String],
                         resource: Option[String],
                         fields: Option[String] = None
                       ) {
  import MetadataPath._

  override def toString: String = {
    val serviceRes = new StringBuffer(service).append(":")
    val pathRes = resource.map(_ => addLevelCharPathWithResource(path.get)).getOrElse(path.map(addLevelCharPathWithOutResource(_)).getOrElse(""))
    val resourceRes = resource.map(x => x.concat(SPLIT_CHAR)).getOrElse("")
    val fieldRes = fields.map(x => x.concat(SPLIT_CHAR)).getOrElse("")
    serviceRes.append(pathRes).append(resourceRes).append(fieldRes).toString
  }

  def addLevelCharPathWithResource(path: String): String = {
    path.equals(PROTOCOL_CHAR) match {
      case true => path.concat(LEVEL_CHAR).concat(SPLIT_CHAR)
      case false => PROTOCOL_CHAR.concat(path.concat(LEVEL_CHAR).concat(SPLIT_CHAR))
    }
  }

  def addLevelCharPathWithOutResource(path: String): String = {
    PROTOCOL_CHAR.concat(path.reverse.replaceFirst(PROTOCOL_CHAR, REVERSE_LEVEL_CHAR).reverse.concat(SPLIT_CHAR))
  }
}

object MetadataPath {

  val REVERSE_LEVEL_CHAR: String = "/>"
  val PROTOCOL_CHAR: String = "/"
  val LEVEL_CHAR: String = ">/"
  val SPLIT_CHAR: String = ":"

}