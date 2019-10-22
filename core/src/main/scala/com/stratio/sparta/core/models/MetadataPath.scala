/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum
import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum.QualityRuleResourceType

case class MetadataPath(service: String,
                         path: Option[String],
                         resource: Option[String],
                         fields: Option[String] = None) {
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
      case true => path.concat(LEVEL_CHAR).concat(SPLIT_CHAR) // />/:
      case false => PROTOCOL_CHAR.concat(path.concat(LEVEL_CHAR).concat(SPLIT_CHAR)) // /myStrangePath >/:
    }
  }

  def addLevelCharPathWithOutResource(path: String): String = {
    PROTOCOL_CHAR.concat(path.reverse.replaceFirst(PROTOCOL_CHAR, REVERSE_LEVEL_CHAR).reverse.concat(SPLIT_CHAR))
  }
}

object MetadataPath {
  // Service                :/   /pathRes                              >/:  resource              :      field
  // postgreseos            :/   /postgreseos                          >/:  dg_metadata.actor     :
  // tenant1-hdfs-example   :/   /user/tenant1-spartamig/sdasd/Test    >/:  Test                  :       raw
  //^([a-zA-Z_\\.\\-0-9]+)(?>\\:\\/)(\\/[a-zA-Z_\\.\\/-0-9]+)(?>\\>\\/\\:([a-zA-Z_\\.\\-1-9]+))(?>\\:)([a-zA-Z_\\.\\-0-9]*)$

  val REVERSE_LEVEL_CHAR: String = "/>"
  val PROTOCOL_CHAR: String = "/"
  val LEVEL_CHAR: String = ">/"
  val SPLIT_CHAR: String = ":"

  //Code from governance API
  def fromMetadataPathString(metadataPath: String): MetadataPath = {
    val splitted = metadataPath.split(SPLIT_CHAR)
    val service = splitted.head
    val path = splitted.lift(2).map(_ => splitted.lift(1).map(x=>x.replace(">/", "").replace("//", "/")))
      .getOrElse(splitted.lift(1).map(x=>x.replace(">/", "/").replace("//", "/")))
    val resource = splitted.lift(2)

    val field = splitted.lift(3)
    new MetadataPath(service, path, resource, field)
  }

  def getTableName(metadataPath: String, typeMetadata: Option[QualityRuleResourceType]): String = {
    typeMetadata match {
      case Some(QualityRuleResourceTypeEnum.XD) => fromMetadataPathString(metadataPath).resource.get
      case Some(QualityRuleResourceTypeEnum.HDFS) => fromMetadataPathString(metadataPath).resource.get
      case Some(QualityRuleResourceTypeEnum.JDBC) => getPostgresTableName(metadataPath)
      case _ => ""
    }
  }

  def getPostgresTableName(metadataPath: String): String = {
    fromMetadataPathString(metadataPath).resource.get.split("\\.").last
  }
}